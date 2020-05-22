package com.velozity.events;

import com.velozity.helpers.Parsers;
import com.velozity.types.LogType;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;
import com.velozity.helpers.Interactions;
import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;

import com.xorist.vshop.ShopGUI;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.io.Console;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class EventHandlers implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final ShopConfig shopConfig = Global.shopConfig;
    private static final Parsers parser = Global.parser;
    private static final Interactions interact = Global.interact;
    private static final ShopGUI shopgui = Global.shopgui;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        Block clickedBlock = e.getClickedBlock();
        if(action == Action.RIGHT_CLICK_BLOCK) {
            if (clickedBlock.getType().equals(Material.SIGN) || clickedBlock.getType().equals(Material.WALL_SIGN) || clickedBlock.getType().equals(Material.LEGACY_SIGN_POST)) {
                String signId = parser.locationToBase64(clickedBlock.getLocation());
                Set<String> signIds = shopConfig.getSignIds();

                // If sign being hit is in a registered sign shop & its a normal user
                if(signIds.contains(String.valueOf(signId)) /*&& !Global.editModeEnabled.contains(e.getPlayer().getUniqueId())*/) {
                    Shop shop = shopConfig.getShop(signId);
                    shopgui.openShopGUI(Material.getMaterial(shop.item.getType().toString()), e.getPlayer(), signId, shop.title, shop.item.getItemMeta().getLore(), shop.buyprice, shop.sellprice);
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) throws IOException {

        if (e.getBlock().getType().equals(Material.SIGN) || e.getBlock().getType().equals(Material.WALL_SIGN) || e.getBlock().getType().equals(Material.LEGACY_SIGN_POST)) {
            org.bukkit.block.Sign ws = (org.bukkit.block.Sign)e.getBlock().getState();
            String signId = parser.locationToBase64(e.getBlock().getLocation());

            // If sign being hit is in a registered sign shop & its a normal user
            if(shopConfig.signIdExists(signId) && !Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {
                Shop shop = shopConfig.getShops().get(signId);
                shopgui.openShopGUI(Material.getMaterial(shop.item.getType().toString()), e.getPlayer(), signId, shop.title, shop.item.getItemMeta().getLore(), shop.buyprice, shop.sellprice);
                e.setCancelled(true);
                return;
            }
            
            // If in editmode
            if(Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {
                    // If the sign is already armed
                    if(shopConfig.signIdExists(signId)) {
                        if(Global.pendingRemoveSigns.contains(signId)) {

                            // Does user have permission to destroy shops?
                            if(!e.getPlayer().hasPermission(Global._permDestroyShop)) {
                                interact.msgPlayer("You do not have permission to destroy shops!", e.getPlayer());
                                e.setCancelled(true);
                                return;
                            }

                            // REMOVE SIGN SHOP
                            interact.msgPlayer("Shop removed", e.getPlayer());
                            interact.logServer(LogType.info, "Shop removed by " + e.getPlayer().getDisplayName() + " [Item: " + shopConfig.getShop(signId).item.getType().toString() + "]");
                            shopConfig.removeShop(signId);
                            Global.pendingRemoveSigns.remove(signId);

                            e.setCancelled(false);
                            return;
                        } else {
                            // Does user have permission to destroy shops?
                            if(!e.getPlayer().hasPermission(Global._permDestroyShop)) {
                                interact.msgPlayer("You do not have permission to destroy shops!", e.getPlayer());
                                e.setCancelled(true);
                                return;
                            }

                            interact.msgPlayer("Hit sign again to remove shop", e.getPlayer());
                            Global.pendingRemoveSigns.add(signId);
                            new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            if(Global.pendingRemoveSigns.contains(signId)){
                                                Global.pendingRemoveSigns.remove(signId);
                                                interact.msgPlayer("No 2nd hit detected - timed out", e.getPlayer());
                                            }
                                        }
                                    },
                                    5000
                            );
                        }
                        e.setCancelled(true);
                        return;
                    }
                    // Does user have permission to create shops?
                    if(!e.getPlayer().hasPermission(Global._permCreateShop)) {
                        interact.msgPlayer("You do not have permission to create shops!", e.getPlayer());
                        e.setCancelled(true);
                        return;
                    }

                    String line3 = ws.getLine(2);
                    String line4 = ws.getLine(3);
                    if(line3.isEmpty() || line4.isEmpty()) {
                        interact.msgPlayer("You must specify either a buy or sell price", e.getPlayer());
                        e.setCancelled(false);
                        return;
                    }

                    ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

                    int parsedLine3 = parser.signPrice(ws.getLine(2));
                    int parsedLine4 = parser.signPrice(ws.getLine(3));

                    if(parsedLine3 == -1 || parsedLine4 == -1) {
                        interact.msgPlayer("Invalid buy/sell syntax", e.getPlayer());
                        e.setCancelled(false);
                        return;
                    }

                    boolean buyable = true;
                    boolean sellable = true;
                    String line3Res = Global.mainConfig.readSetting("shop", "buyprefix").toString() + " " + Global.mainConfig.readSetting("shop", "currencysymbol") + parsedLine3;
                    String line4Res = Global.mainConfig.readSetting("shop", "sellprefix").toString() + " " + Global.mainConfig.readSetting("shop", "currencysymbol") + parsedLine4;

                    if(parser.signPrice(line3).equals(-2)) {
                        buyable = false;
                    }

                    if(parser.signPrice(line4).equals(-2)) {
                        sellable = false;
                    }

                    String displayItemName = WordUtils.capitalizeFully(item.getType().toString().replace("_", " "));

                    Sign sign = ((Sign)e.getBlock().getState());

                    sign.setLine(0, Global.mainConfig.readSetting("shop", "signtitle").toString());

                    if(buyable)
                        sign.setLine(2, line3Res);

                    if(sellable)
                        sign.setLine(3, line4Res);

                    sign.update(true);

                    String title = ((String)Global.mainConfig.readSetting("shop", "guititle")).replace("{item}", displayItemName);
                    shopConfig.writeShop(parser.locationToBase64(e.getBlock().getLocation()), new Shop(title, item, parser.signPrice(ws.getLine(2)), parser.signPrice(ws.getLine(3)), buyable, sellable), true);
                    interact.msgPlayer("Sign armed and shop ready [Item: " + displayItemName + "]", e.getPlayer());
                    interact.logServer(LogType.info, "Shop created [Item: " + displayItemName + "]");
                    e.setCancelled(true);
            }
        }
    }
}

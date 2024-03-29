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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
    Parsers parser = Global.parser;
    Interactions interact = Global.interact;
    ShopGUI shopgui = Global.shopgui;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        Block clickedBlock = e.getClickedBlock();
        if(action == Action.RIGHT_CLICK_BLOCK) {
            if (Global.signTypes.contains(clickedBlock.getType())) {
                String signId = parser.locationToBase64(clickedBlock.getLocation());
                Set<String> signIds = Global.shopConfig.getSignIds();

                // If sign being hit is in a registered sign shop & its a normal user
                if(signIds.contains(String.valueOf(signId)) /*&& !Global.editModeEnabled.contains(e.getPlayer().getUniqueId())*/) {
                    Shop shop = Global.shopConfig.getShop(signId);
                    shopgui.openShopGUI(e.getPlayer(), signId, shop.title, shop.buyprice, shop.sellprice);
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) throws IOException {

        if (Global.signTypes.contains(e.getBlock().getType())) {
            org.bukkit.block.Sign ws = (org.bukkit.block.Sign)e.getBlock().getState();

            String signId = parser.locationToBase64(e.getBlock().getState().getLocation());

            Boolean signIdExists = Global.shopConfig.signIdExists(signId);
            // If sign being hit is in a registered sign shop & its a normal user
            if(signIdExists && !Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {
                Shop shop = Global.shopConfig.getShop(signId);
                shopgui.openShopGUI(e.getPlayer(), signId, shop.title, shop.buyprice, shop.sellprice);
                e.setCancelled(true);
                return;
            }
            
            // If in editmode
            if(Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {
                    // If the sign is already armed
                    if(signIdExists) {
                        if(Global.pendingRemoveSigns.contains(signId)) {

                            // Does user have permission to destroy shops?
                            if(!e.getPlayer().hasPermission(Global._permDestroyShop)) {
                                interact.msgPlayer("You do not have permission to destroy shops", e.getPlayer());
                                e.setCancelled(true);
                                return;
                            }

                            // REMOVE SIGN SHOP
                            interact.msgPlayer("Shop removed", e.getPlayer());
                            interact.logServer(LogType.info, "Shop removed by " + e.getPlayer().getDisplayName() + " [Item: " + WordUtils.capitalizeFully(Global.shopConfig.getShop(signId).item.getType().toString().replace("_", " ")) + "]");
                            shopConfig.removeShop(signId);
                            Global.pendingRemoveSigns.remove(signId);
                            e.setCancelled(false);
                            return;
                        } else {
                            // Does user have permission to destroy shops?
                            if(!e.getPlayer().hasPermission(Global._permDestroyShop)) {
                                interact.msgPlayer("You do not have permission to destroy shops", e.getPlayer());
                                e.setCancelled(true);
                                return;
                            }

                            interact.msgPlayer("Hit sign again to remove shop", e.getPlayer());
                            Global.pendingRemoveSigns.add(signId);
                            Bukkit.getScheduler().runTaskLaterAsynchronously(Global.getMainInstance, () -> {
                                if (Global.pendingRemoveSigns.contains(signId)) {
                                    Global.pendingRemoveSigns.remove(signId);
                                    interact.msgPlayer("No 2nd hit detected - timed out", e.getPlayer());
                                }
                            }, parser.secsToTicks(5));
                        }
                        e.setCancelled(true);
                        return;
                    }

                    // Check if sign is a sign that should be armed
                    if(!ws.getLine(0).equalsIgnoreCase("[shop]")) {
                        return;
                    }
                    // Check if item is not air
                    if(e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
                        interact.msgPlayer("You must have an item to sell in your hand", e.getPlayer());
                        e.setCancelled(true);
                        return;
                    }
                    // Does user have permission to create shops?
                    if(!e.getPlayer().hasPermission(Global._permCreateShop)) {
                        interact.msgPlayer("You do not have permission to create shops", e.getPlayer());
                        e.setCancelled(true);
                        return;
                    }

                    String line3 = ws.getLine(2);
                    String line4 = ws.getLine(3);
                    if(line3.isEmpty() && line4.isEmpty()) {
                        interact.msgPlayer("You must specify a valid buy or sell price", e.getPlayer());
                        e.setCancelled(false);
                        return;
                    }

                    ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

                    int parsedLine3 = parser.signPrice(ws.getLine(2));
                    int parsedLine4 = parser.signPrice(ws.getLine(3));

                    if(parsedLine3 == -1 || parsedLine4 == -1) {
                        interact.msgPlayer("Something went wrong parsing your buy/sell prices", e.getPlayer());
                        e.setCancelled(false);
                        return;
                    }

                    if(parsedLine3 == -2 && parsedLine4 == -2) {
                        Global.interact.msgPlayer("You must specify a valid buy or sell price", e.getPlayer());
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
                    Global.shopConfig.writeShop(parser.locationToBase64(e.getBlock().getLocation()), new Shop(title, item, parser.signPrice(ws.getLine(2)), parser.signPrice(ws.getLine(3)), buyable, sellable));
                    interact.msgPlayer("Sign armed and shop ready [Item: " + displayItemName + "]", e.getPlayer());
                    interact.logServer(LogType.info, "Shop created by " + e.getPlayer().getDisplayName() + " [Item: " + displayItemName + "]");
                    e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(Global.editModeEnabled.contains(event.getPlayer().getUniqueId())) {
            Global.interact.msgPlayer("Reminder: You are still in edit mode", event.getPlayer());
        }
    }
}

package com.velozity.events;

import com.velozity.types.LogType;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;
import com.velozity.helpers.Interactions;
import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;

import com.xorist.vshop.ShopGUI;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        Block clickedBlock = e.getClickedBlock();
        if(action == Action.RIGHT_CLICK_BLOCK) {
            if (clickedBlock.getType().equals(Material.SIGN) || clickedBlock.getType().equals(Material.WALL_SIGN) || clickedBlock.getType().equals(Material.LEGACY_SIGN_POST)) {
                org.bukkit.block.Sign ws = (org.bukkit.block.Sign)clickedBlock.getState();
                if(!ws.getLine(0).toLowerCase().equals("[shop]") || Global.parser.signPrice(ws.getLine(2)) == -1 || Global.parser.signPrice(ws.getLine(3)) == -1) {
                    e.setCancelled(false);
                    return;
                }
                Integer signId = e.getClickedBlock().hashCode();
                Set<String> signIds = Global.shopConfig.getSignIds();
                // If sign being hit is in a registered sign shop & its a normal user
                if(signIds.contains(String.valueOf(signId)) /*&& !Global.editModeEnabled.contains(e.getPlayer().getUniqueId())*/) {
                    Shop shop = Global.shopConfig.getShop(signId.toString());
                    Global.shopgui.openShopGUI(Material.getMaterial(shop.item.getType().toString()), e.getPlayer(), String.valueOf(e.getClickedBlock().hashCode()), shop.title, shop.item.getItemMeta().getLore(), shop.buyprice, shop.sellprice);
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
            Integer signId = e.getBlock().hashCode();

            Set<String> signIds = Global.shopConfig.getSignIds();

            // If sign being hit is in a registered sign shop & its a normal user
            if(signIds.contains(String.valueOf(signId)) && !Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {
                Shop shop = Global.shopConfig.getShops().get(signId.toString());

                Global.shopgui.openShopGUI(Material.getMaterial(shop.item.getType().toString()), e.getPlayer(), String.valueOf(e.getBlock().hashCode()), shop.title, shop.item.getItemMeta().getLore(), shop.buyprice, shop.sellprice);
                e.setCancelled(true);
                return;
            }

            // If in editmode
            if(Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {

                // If its a shop sign
                if(ws.getLine(0).toLowerCase().equals("[shop]")) {
                    // If the sign is already armed
                    if(Global.armedSigns.contains(signId)) {
                        if(Global.pendingRemoveSigns.contains(signId)) {
                            // REMOVE SIGN SHOP
                            Global.interact.msgPlayer("Shop removed", e.getPlayer());
                            Global.shopConfig.removeShop(signId.toString());
                            Global.armedSigns.remove(signId);
                            Global.pendingRemoveSigns.remove(signId);

                            e.setCancelled(false);
                            return;
                        } else {
                            Global.interact.msgPlayer("Hit sign again to remove shop", e.getPlayer());
                            Global.pendingRemoveSigns.add(signId);
                            new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            if(Global.pendingRemoveSigns.contains(signId)){
                                                Global.pendingRemoveSigns.remove(signId);
                                                Global.interact.msgPlayer("No 2nd hit detected - timed out", e.getPlayer());
                                            }
                                        }
                                    },
                                    5000
                            );
                        }
                        e.setCancelled(true);
                        return;
                    }
                    String line3 = ws.getLine(2);
                    String line4 = ws.getLine(3);
                    if(line3.isEmpty() && line4.isEmpty()) {
                        Global.interact.msgPlayer("You must specify either a buy or sell price", e.getPlayer());
                        e.setCancelled(false);
                        return;
                    }

                    ItemStack item = e.getPlayer().getInventory().getItemInMainHand();

                    int parsedLine3 = Global.parser.signPrice(ws.getLine(2));
                    int parsedLine4 = Global.parser.signPrice(ws.getLine(3));

                    if(parsedLine3 == -1 || parsedLine4 == -1) {
                        Global.interact.msgPlayer("Invalid buy/sell syntax", e.getPlayer());
                        e.setCancelled(false);
                        return;
                    }
                    boolean buyable = true;
                    boolean sellable = true;
                    String line3Res = "Buy: " + Global.parser.signPrice(ws.getLine(2)).toString();
                    String line4Res = "Sell: " + Global.parser.signPrice(ws.getLine(3)).toString();
                    List<String> lore = new ArrayList<>();
                    lore.add(line3Res);
                    lore.add(line4Res);

                    if(Global.parser.signPrice(line3).equals(-1)) {
                        buyable = false;
                    }

                    if(Global.parser.signPrice(line4).equals(-1)) {
                        sellable = false;
                    }

                    String displayItemName = WordUtils.capitalizeFully(item.getType().toString().replace("_", " "));
                    log.info("gsg: " + displayItemName);
                    LinkedHashMap<String, List<PotionEffect>> potionData = new LinkedHashMap<>();


                    String itemId = item.getType().toString();
                    if(item.getItemMeta() instanceof PotionMeta) {

                        PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
                        displayItemName = "Potion of " + WordUtils.capitalizeFully(potionMeta.getBasePotionData().getType().toString().replace("_", " "));

                        potionData.put(potionMeta.getBasePotionData().getType().toString(), potionMeta.getCustomEffects());

                    }


                    Global.shopConfig.writeShop(String.valueOf(e.getBlock().hashCode()), new Shop("Buy " + displayItemName, item, Global.parser.signPrice(ws.getLine(2)), Global.parser.signPrice(ws.getLine(3)), buyable, sellable), true);
                    Global.interact.msgPlayer("Sign armed and shop ready [Item: " + displayItemName + "]", e.getPlayer());
                    Global.armedSigns.add(signId);

                    Global.interact.logServer(LogType.info, "Shop created [Item: " + displayItemName + "]");
                    e.setCancelled(true);
                }
            }
        }
    }
}

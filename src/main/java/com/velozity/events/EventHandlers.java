package com.velozity.events;

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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class EventHandlers implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("vshop.createshop")) {
            if(e.getLine(0).toLowerCase().equals("[shop]")) {
                e.setLine(0, "[Shop]");
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

                Global.shopgui.openShopGUI(Material.getMaterial(shop.itemid), e.getPlayer(), e.getBlock().hashCode(), shop.title, shop.lore, shop.buyprice, shop.sellprice);
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
                            log.info("1");
                            Global.interact.msgPlayer("Shop removed", e.getPlayer());
                            Global.armedSigns.remove(signId);
                            Global.pendingRemoveSigns.remove(signId);

                            e.setCancelled(false);
                            return;
                        } else {
                            log.info("2");
                            Global.interact.msgPlayer("Hit sign again to remove shop", e.getPlayer());
                            Global.pendingRemoveSigns.add(signId);
                        }
                        log.info("3");
                        e.setCancelled(true);
                        return;
                    }
                    String line3 = ws.getLine(2);
                    String line4 = ws.getLine(3);
                    if(line3.isEmpty() && line4.isEmpty()) {
                        Global.interact.msgPlayer("You must specify either a buy or sell price", e.getPlayer());
                        e.setCancelled(true);
                        return;
                    }

                    Material item = e.getPlayer().getInventory().getItemInMainHand().getType();

                    boolean buyable = true;
                    boolean sellable = true;
                    if(Global.parser.signPrice(line3).equals(-1)) {
                        buyable = false;
                    }

                    if(Global.parser.signPrice(line4).equals(-1)) {
                        sellable = false;
                    }

                    Global.shopConfig.writeShop(String.valueOf(e.getBlock().hashCode()), new Shop("Buy " + WordUtils.capitalizeFully(item.toString().replace("_", " ")), item.toString(), Collections.emptyList(), Global.parser.signPrice(ws.getLine(2)), Global.parser.signPrice(ws.getLine(3)), buyable, sellable));
                    Global.interact.msgPlayer("Sign armed and shop ready", e.getPlayer());
                    Global.armedSigns.add(signId);

                    e.setCancelled(true);
                }
            }
        }
    }
}

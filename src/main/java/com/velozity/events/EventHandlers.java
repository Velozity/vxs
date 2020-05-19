package com.velozity.events;

import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;
import com.velozity.helpers.Interactions;
import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;

import com.xorist.vshop.ShopGUI;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class EventHandlers implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("vshop.createshop")) {
            if(e.getLine(0).toLowerCase().equals("[shop]")) {
                e.setLine(0, "[shop]");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) throws IOException {

        if (e.getBlock().getType().equals(Material.SIGN) || e.getBlock().getType().equals(Material.WALL_SIGN) || e.getBlock().getType().equals(Material.LEGACY_SIGN_POST)) {

            if(Global.editModeEnabled.isEmpty()) { return; }
            Integer signId = e.getBlock().hashCode();

            // If in editmode
            if(Global.editModeEnabled.contains(e.getPlayer().getUniqueId())) {
                org.bukkit.block.Sign ws = (org.bukkit.block.Sign)e.getBlock().getState();
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

                    Global.shopConfig.writeShop(String.valueOf(e.getBlock().hashCode()), new Shop("Wow so cool!", "PENIS", Collections.emptyList(), 80, 14, true, true));
                    Global.interact.msgPlayer("Sign armed and shop ready", e.getPlayer());
                    Global.armedSigns.add(signId);

                    e.setCancelled(true);
                }
            }
        }
    }
}

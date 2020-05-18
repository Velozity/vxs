package com.xorist.vshop;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUI implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");

    protected ItemStack createGuiItem(final Material material, final String name, final String lore) {
        log.info("CreateGUIItem");
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        log.info("Hit Inventory Click1");
        if(e.getInventory().getHolder() != null) {
            return;
        }

        e.setCancelled(true);
        final ItemStack clickedItem = e.getCurrentItem();

        if(clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        final Player p = (Player) e.getWhoClicked();

        p.sendMessage("You clicked at slot " + e.getRawSlot());
    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if(e.getInventory().getHolder() == null) {
            e.setCancelled(true);
        }
    }

    public void openShopGUI(Material material, HumanEntity player, String name, String lore) {
        player.openInventory(createInventory(material, name, lore));
    }

    public Inventory createInventory(Material material, String name, String lore) {
        Inventory inv = Bukkit.createInventory(null, 18, "Shop");
        inv.addItem(createGuiItem(material, name, lore));
        return inv;
    }
}

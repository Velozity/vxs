package com.xorist.vshop;

import java.util.Arrays;
import java.util.logging.Logger;

import com.velozity.vshop.Main;
import net.milkbowl.vault.economy.Economy;
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
    public void onInventoryClick(InventoryDragEvent e) {
        if(e.getInventory().getHolder() != null)
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getHolder() != null) {
            return;
        }
        if(e.isShiftClick()) {
            e.setCancelled(true);
        }

        // debugging prints
        log.info("Get Cursor: " + String.valueOf(e.getCursor().getType()));

        Inventory inventory = e.getInventory();
        int clickedItemSlot = e.getRawSlot();
        log.info(String.valueOf(clickedItemSlot));
        Player player = (Player) e.getWhoClicked();
        ItemStack cursorItem = new ItemStack(Material.AIR);
        ItemStack clickedItem = new ItemStack(Material.AIR);

        if(e.getCursor() == null) {
            log.info("cursor is null");
        } else {
            log.info("Cursor: " + String.valueOf(e.getCursor().getType()));
            cursorItem = e.getCursor();
        }

        if(e.getCurrentItem() == null) {
            log.info("current item is null");
        } else {
            log.info("Current Item: " + String.valueOf(e.getCurrentItem().getType()));
            clickedItem = e.getCurrentItem();
        }

        if(clickedItemSlot >= 0 && clickedItemSlot <= 2) {
            log.info("1");
            e.setCancelled(true);
            subtractItems(inventory, clickedItemSlot);
        } else if(clickedItemSlot >= 6 && clickedItemSlot <= 8) {
            log.info("2");
            e.setCancelled(true);
            addItems(inventory, clickedItemSlot);
        } else if(clickedItemSlot < 18) {
            e.setCancelled(true);
        }
    }

    public void addItems(Inventory inv, int slotClicked) {
        int addAmount = inv.getItem(slotClicked).getAmount();
        int currentAmount = inv.getItem(4).getAmount();
        int maxStack = inv.getItem(4).getMaxStackSize();
        if((addAmount + currentAmount) > maxStack) {
            inv.getItem(4).setAmount(64);
        } else {
            inv.getItem(4).setAmount(inv.getItem(4).getAmount() + addAmount);
        }
    }

    public void subtractItems(Inventory inv, int slotClicked) {
        int subAmount = inv.getItem(slotClicked).getAmount();
        int currentAmount = inv.getItem(4).getAmount();
        if((currentAmount - subAmount) < 1) {
            inv.getItem(4).setAmount(1);
        } else {
            inv.getItem(4).setAmount(inv.getItem(4).getAmount() - subAmount);
        }
    }

    public void openShopGUI(Material material, HumanEntity player, String name, String lore) {
        player.openInventory(createInventory(material, name, lore));
    }

    public Inventory createInventory(Material material, String name, String lore) {
        Inventory inv = Bukkit.createInventory(null, 18, "Shop");

        ItemStack toBuy = new ItemStack(material);

        ItemStack addOperator = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta addOperatorMeta = addOperator.getItemMeta();

        ItemStack subOperator = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta subOperatorMeta = subOperator.getItemMeta();

        if(material.getMaxStackSize() > 7) {
            subOperator.setAmount(material.getMaxStackSize());
            String subOperatorName = "SUBTRACT " + String.valueOf(material.getMaxStackSize());
            subOperatorMeta.setDisplayName(subOperatorName);
            subOperator.setItemMeta(subOperatorMeta);
            inv.setItem(0, subOperator);

            subOperator.setAmount(material.getMaxStackSize() / 4);
            subOperatorName = "SUBTRACT " + String.valueOf(material.getMaxStackSize() / 4);
            subOperatorMeta.setDisplayName(subOperatorName);
            subOperator.setItemMeta(subOperatorMeta);
            inv.setItem(1, subOperator);

            addOperator.setAmount(material.getMaxStackSize() / 4);
            String addOperatorName = "ADD " + String.valueOf(material.getMaxStackSize() / 4);
            addOperatorMeta.setDisplayName(addOperatorName);
            addOperator.setItemMeta(addOperatorMeta);
            inv.setItem(7, addOperator);

            addOperator.setAmount(material.getMaxStackSize());
            addOperatorName = "ADD " + String.valueOf(material.getMaxStackSize());
            addOperatorMeta.setDisplayName(addOperatorName);
            addOperator.setItemMeta(addOperatorMeta);
            inv.setItem(8, addOperator);
        }

        subOperator.setAmount(1);
        subOperatorMeta.setDisplayName("SUBTRACT 1");
        subOperator.setItemMeta(subOperatorMeta);
        inv.setItem(2, subOperator);

        inv.setItem(4, toBuy);

        addOperator.setAmount(1);
        addOperatorMeta.setDisplayName("ADD 1");
        addOperator.setItemMeta(addOperatorMeta);
        inv.setItem(6, addOperator);

        return inv;
    }
}

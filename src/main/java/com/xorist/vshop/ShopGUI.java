package com.xorist.vshop;

import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;
import com.velozity.events.EventHandlers;
import com.velozity.helpers.Interactions;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.xorist.vshop.ShopGUI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUI implements Listener {

    Interactions interact = Global.interact;
    Logger log = Global.log;
    Economy econ = Global.econ;

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
        if(e.getInventory().getHolder() != null) {
            return;
        }
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
        if (e.getClick() == ClickType.DOUBLE_CLICK) {
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
            log.info("subtracting..");
            e.setCancelled(true);
            subtractItems(inventory, clickedItemSlot);
        } else if(clickedItemSlot >= 6 && clickedItemSlot <= 8) {
            log.info("adding..");
            e.setCancelled(true);
            addItems(inventory, clickedItemSlot);
        } else if(Global.editModeEnabled.contains(player.getUniqueId()) && clickedItemSlot == 18) {
            e.setCancelled(true);
            disableBuyItems();
        } else if(Global.editModeEnabled.contains(player.getUniqueId()) && clickedItemSlot == 26) {
            e.setCancelled(true);
            disableSellItems();
        } else if(Global.editModeEnabled.contains(player.getUniqueId()) && clickedItemSlot < 27) {
            e.setCancelled(true);
        } else if(clickedItemSlot < 18) {
            e.setCancelled(true);
        }
    }

    public void disableBuyItems() {
        log.info("Disabling Buy Items");
    }

    public void disableSellItems() {
        log.info("Disabling Sell Items");
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

    public void openShopGUI(Material material, HumanEntity player, String name, String[] lore, String title, int buyPrice, int sellPrice) {
            player.openInventory(createInventory(material, name, lore, title, buyPrice, sellPrice, player));
    }

    public Inventory createInventory(Material material, String name, String[] lore, String title, int buyPrice, int sellPrice, HumanEntity player) {

        Inventory inv;

        if(!Global.editModeEnabled.contains(player.getUniqueId())) {
            inv = Bukkit.createInventory(null, 18, title);
        } else {
            inv = Bukkit.createInventory(null, 27, title + " (admin mode)");
        }

        ItemStack toBuy = new ItemStack(material);

        ItemStack addOperator = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta addOperatorMeta = addOperator.getItemMeta();

        ItemStack subOperator = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta subOperatorMeta = subOperator.getItemMeta();

        ItemStack buyOperator = new ItemStack(Material.GREEN_STAINED_GLASS);
        ItemMeta buyOperatorMeta = buyOperator.getItemMeta();

        ItemStack sellOperator = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta sellOperatorMeta = sellOperator.getItemMeta();

        ItemStack sellAllOperator = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta sellAllOperatorMeta = sellAllOperator.getItemMeta();

        ItemStack adminBuyOperator = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta adminBuyOperatorMeta = adminBuyOperator.getItemMeta();

        ItemStack adminSellOperator = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta adminSellOperatorMeta = adminSellOperator.getItemMeta();


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

        if(buyPrice > 0) {
            buyOperator.setAmount(1);
            buyOperatorMeta.setDisplayName("BUY");
            buyOperator.setItemMeta(buyOperatorMeta);
            inv.setItem(12, buyOperator);
        }

        if(sellPrice > 0) {
            sellOperator.setAmount(1);
            sellOperatorMeta.setDisplayName("SELL");
            sellOperator.setItemMeta(sellOperatorMeta);
            inv.setItem(13, sellOperator);

            sellAllOperator.setAmount(1);
            sellAllOperatorMeta.setDisplayName("SELL ALL");
            sellAllOperator.setItemMeta(sellAllOperatorMeta);
            inv.setItem(14, sellAllOperator);
        }

        if(Global.editModeEnabled.contains(player.getUniqueId())) {
            adminBuyOperator.setAmount(1);
            if(buyPrice > 0) {
                adminBuyOperatorMeta.setDisplayName("BUY: ON");
            } else {
                adminBuyOperatorMeta.setDisplayName("BUY: OFF");
            }
            adminBuyOperator.setItemMeta(adminBuyOperatorMeta);
            inv.setItem(18, adminBuyOperator);

            adminSellOperator.setAmount(1);
            if(sellPrice > 0) {
                adminSellOperatorMeta.setDisplayName("SELL: ON");
            } else {
                adminSellOperatorMeta.setDisplayName("SELL: OFF");
            }
            adminSellOperator.setItemMeta(adminSellOperatorMeta);
            inv.setItem(26, adminSellOperator);
        }

        return inv;
    }
}
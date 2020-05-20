package com.xorist.vshop;

import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;
import com.velozity.events.EventHandlers;
import com.velozity.helpers.Interactions;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.xorist.vshop.ShopGUI;

import java.util.*;
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
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class ShopGUI implements Listener {

    Interactions interact = Global.interact;
    Logger log = Global.log;
    Economy econ = Global.econ;
    ShopConfig shopConfig = new ShopConfig();

    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        if(e.getInventory().getHolder() != null) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Inventory inventory = e.getInventory();
        int clickedItemSlot = e.getRawSlot();
        log.info(String.valueOf(clickedItemSlot));
        Player player = (Player) e.getWhoClicked();
        ItemStack cursorItem = new ItemStack(Material.AIR);
        ItemStack clickedItem = new ItemStack(Material.AIR);

        if(e.getInventory().getHolder() != null) {
            return;
        }

        if((clickedItemSlot < 18 && !Global.editModeEnabled.contains(player.getUniqueId())) || (clickedItemSlot < 27 && Global.editModeEnabled.contains(player.getUniqueId())) || e.isShiftClick() || (e.getClick() == ClickType.DOUBLE_CLICK)) {
            e.setCancelled(true);
        }

        if(e.getCursor() != null) {
            cursorItem = e.getCursor();
        }

        if(e.getCurrentItem() != null) {
            clickedItem = e.getCurrentItem();
        }

        if(clickedItemSlot >= 0 && clickedItemSlot <= 2) {
            subtractItems(inventory, clickedItemSlot);
        } else if(clickedItemSlot >= 6 && clickedItemSlot <= 8) {
            addItems(inventory, clickedItemSlot);
        } else if(clickedItemSlot == 12) {
            int buyValue = Integer.parseInt(inventory.getItem(4).getItemMeta().getLore().get(0).toLowerCase().split(": ")[1]);
            buyItem(inventory, player, buyValue);
        } else if(clickedItemSlot == 13) {
            int sellValue = Integer.parseInt(inventory.getItem(4).getItemMeta().getLore().get(1).toLowerCase().split(": ")[1]);
            sellItem(inventory, player, sellValue);
        } else if(clickedItemSlot == 14) {
            int sellValue = Integer.parseInt(inventory.getItem(4).getItemMeta().getLore().get(1).toLowerCase().split(": ")[1]);
            sellAllItems(inventory, player, sellValue);
        } else if(Global.editModeEnabled.contains(player.getUniqueId()) && clickedItemSlot == 18) {
            List<String> clickedItemLore = clickedItem.getItemMeta().getLore();
            log.info(String.valueOf(clickedItemLore.get(0)));
            toggleBuyItems(inventory, clickedItemLore.get(0));
            log.info(String.valueOf(clickedItem.getItemMeta().getLore()));
        } else if(Global.editModeEnabled.contains(player.getUniqueId()) && clickedItemSlot == 26) {
            List<String> clickedItemLore = clickedItem.getItemMeta().getLore();
            log.info(String.valueOf(clickedItemLore.get(0)));
            toggleSellItems(inventory, clickedItemLore.get(0));
            log.info(String.valueOf(clickedItem.getItemMeta().getLore()));
        }
    }

    public void buyItem(Inventory inventory, Player player, int buyValue) {
        int cost = buyValue * inventory.getItem(4).getAmount();

        Inventory playerInventory = player.getInventory();

        if(playerInventory.firstEmpty() != -1) {
            log.info(String.valueOf("Empty Slot: " + playerInventory.firstEmpty()));
            if(Global.econ.getBalance(player.getName()) > cost) {

                Global.econ.withdrawPlayer(player.getName(), cost);

                ItemStack buyingItem = inventory.getItem(4);
                ItemMeta buyingItemMeta = buyingItem.getItemMeta();

                if(buyingItemMeta instanceof PotionMeta) {
                    log.info("Is potion!");
                } else {
                    log.info("Not a potion.");
                }

                Material buyingItemMaterial = buyingItem.getType();
                ItemStack itemToDeliver = new ItemStack(buyingItemMaterial, buyingItem.getAmount());
                playerInventory.setItem(playerInventory.firstEmpty(), itemToDeliver);

            } else {
                Global.interact.msgPlayer("You do not have enough money!", player);
            }
        } else {
            Global.interact.msgPlayer("You need at least one free inventory slot!", player);
        }
    }

    public void sellItem(Inventory inventory, Player player, int sellValue) {
        log.info("You hit sellItem");
        Inventory playerInventory = player.getInventory();
        List<Integer> itemsToSell = new ArrayList<>();

        // find sell items in player's inventory
        for(int i = 0; i < 36; i++) {
            if(playerInventory.getItem(i) != null) {
                if(playerInventory.getItem(i).getType() == inventory.getItem(4).getType()) {
                    log.info("Apple found at: " + String.valueOf(i));
                    itemsToSell.add(i);
                }
            }
        }

        // get count of items sellable
        int numItemsToSell = inventory.getItem(4).getAmount();

        if(itemsToSell.size() == 0) {
            Global.interact.msgPlayer("There are no items to sell.", player);
        }

        /* could also do, if amount of items in slot in playerinventory is less than numitemstosell, delete that slot and give the amount of items
        that was in it, then repeat for next slot. Doing it this way may be more efficient.
         */

        for(int i: itemsToSell) {
            for(int j = playerInventory.getItem(i).getAmount(); j > 0; j--) {
                playerInventory.getItem(i).setAmount(playerInventory.getItem(i).getAmount() - 1);
                Global.econ.depositPlayer(player.getName(), sellValue);
                numItemsToSell--;
                if(numItemsToSell == 0) {
                    break;
                }
            }
            if(numItemsToSell == 0) {
                break;
            }
        }
    }

    public void sellAllItems(Inventory inventory, Player player, int sellValue) {
        log.info("You hit sellAllItems");
        Inventory playerInventory = player.getInventory();
        List<Integer> itemsToSell = new ArrayList<>();

        // find sell items in player's inventory
        for(int i = 0; i < 36; i++) {
            if(playerInventory.getItem(i) != null) {
                if(playerInventory.getItem(i).getType() == inventory.getItem(4).getType()) {
                    log.info("Apple found at: " + String.valueOf(i));
                    itemsToSell.add(i);
                }
            }
        }

        if(itemsToSell.size() == 0) {
            Global.interact.msgPlayer("There are no items to sell.", player);
        }

        for(int i: itemsToSell) {
            Global.econ.depositPlayer(player.getName(), sellValue*playerInventory.getItem(i).getAmount());
            playerInventory.setItem(i, null);
        }
    }

    public void toggleBuyItems(Inventory inventory, String signID) {
        log.info("Disabling Buy Items");
        Shop shop = Global.shopConfig.getShop(signID);
        if(shop != null) {
            if(shop.buyable == true) {
                shop.buyable = false;
            } else {
                shop.buyable = true;
            }
        }
    }

    public void toggleSellItems(Inventory inventory, String signID) {
        log.info("Disabling Sell Items");
        Shop shop = Global.shopConfig.getShop(signID);
        if(shop != null) {
            shop.sellable = false;
        } else {
            shop.sellable = true;
        }

    }

    public void addItems(Inventory inv, int slotClicked) {
        int addAmount = inv.getItem(slotClicked).getAmount();
        int currentAmount = inv.getItem(4).getAmount();
        int maxStack = inv.getItem(4).getMaxStackSize();
        if((addAmount + currentAmount) > maxStack) {
            inv.getItem(4).setAmount(inv.getItem(4).getMaxStackSize());
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

    public void openShopGUI(Material material, HumanEntity player, String signID, String title, List<String> lore, int buyPrice, int sellPrice) {
            player.openInventory(createInventory(material, player, signID, title, lore, buyPrice, sellPrice));
    }

    public Inventory createInventory(Material material,  HumanEntity player,  String signID, String title, List<String> lore, int buyPrice, int sellPrice) {

        Inventory inv;

        Boolean isBuyable = false;
        Boolean isSellable = false;

        Shop shop = Global.shopConfig.getShop(signID);
        if(shop != null) {
            isBuyable = shop.buyable;
            isSellable = shop.sellable;
        }

        List<String> adminSignID = new ArrayList<String>();
        adminSignID.add(signID);

        if(!Global.editModeEnabled.contains(player.getUniqueId())) {
            inv = Bukkit.createInventory(null, 18, title);
        } else {
            inv = Bukkit.createInventory(null, 27, title + " (edit mode)");
        }

        ItemStack toBuy = new ItemStack(material);
        ItemMeta toBuyMeta = toBuy.getItemMeta();
        toBuyMeta.setLore(lore);
        toBuy.setItemMeta(toBuyMeta);

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

        if(isBuyable) {
            buyOperator.setAmount(1);
            buyOperatorMeta.setDisplayName("BUY");
            buyOperator.setItemMeta(buyOperatorMeta);
            inv.setItem(12, buyOperator);
        }

        if(isSellable) {
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

            adminBuyOperatorMeta.setLore(adminSignID);
            adminBuyOperator.setItemMeta(adminBuyOperatorMeta);
            inv.setItem(18, adminBuyOperator);

            adminSellOperator.setAmount(1);
            if(sellPrice > 0) {
                adminSellOperatorMeta.setDisplayName("SELL: ON");
            } else {
                adminSellOperatorMeta.setDisplayName("SELL: OFF");
            }
            adminSellOperatorMeta.setLore(adminSignID);
            adminSellOperator.setItemMeta(adminSellOperatorMeta);
            inv.setItem(26, adminSellOperator);
        }

        return inv;
    }
}
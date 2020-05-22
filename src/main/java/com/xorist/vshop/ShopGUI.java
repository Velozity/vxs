package com.xorist.vshop;

import com.velozity.helpers.Interactions;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
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

    @EventHandler
    public void onInventoryClick(InventoryDragEvent e) {
        if(e.getInventory().getHolder() != null) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) throws IOException {
        if(e.getInventory().getHolder() != null) {
            return;
        }

        Inventory shopInventory = e.getInventory();
        int clickedItemSlot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = new ItemStack(Material.AIR);
        Inventory playerInventory = player.getInventory();
        boolean editModeEnabled = Global.editModeEnabled.contains(player.getUniqueId());

        if((clickedItemSlot < 18 && !editModeEnabled) || (clickedItemSlot < 27 && editModeEnabled) || e.isShiftClick() || (e.getClick() == ClickType.DOUBLE_CLICK)) {
            e.setCancelled(true);
        }

        if(e.getCurrentItem() != null) {
            clickedItem = e.getCurrentItem();
        }

        // Handle button clicks
        if(clickedItemSlot >= 0 && clickedItemSlot <= 2) {
            // SUBTRACT BUTTONS
            subtractFromShopItem(shopInventory, clickedItemSlot);
            updateButtonTotalValues(shopInventory, playerInventory);
        } else if(clickedItemSlot >= 6 && clickedItemSlot <= 8) {
            // ADD BUTTONS
            addToShopItem(shopInventory, clickedItemSlot);
            updateButtonTotalValues(shopInventory, playerInventory);
        } else if(clickedItemSlot == 12) {
            // BUY BUTTON
            if(clickedItem.getType() == Material.BARRIER) {
                interact.msgPlayer("You cannot buy this item.", player);
            } else {
                buyItem(player, shopInventory);
                updateButtonTotalValues(shopInventory, playerInventory);
            }
        } else if(clickedItemSlot == 13) {
            // SELL BUTTON
            if(clickedItem.getType() == Material.BARRIER) {
                interact.msgPlayer("You cannot sell this item.", player);
            } else {
                sellItem(player, shopInventory);
                updateButtonTotalValues(shopInventory, playerInventory);
            }
        } else if(clickedItemSlot == 14) {
            // SELL ALL BUTTON
            if(clickedItem.getType() == Material.BARRIER) {
                interact.msgPlayer("You cannot sell this item.", player);
            } else {
                sellAllItems(player, shopInventory);
                updateButtonTotalValues(shopInventory, playerInventory);
            }
        } else if((Global.editModeEnabled.contains(player.getUniqueId())) && (clickedItemSlot == 18)) {
            // BUY TOGGLE BUTTON
            String signID = clickedItem.getItemMeta().getLocalizedName();
            boolean toggleValue = toggleBuyItems(shopInventory, signID);
            if(toggleValue) {
                updateButtonTotalValues(shopInventory, playerInventory);
            }

        } else if((Global.editModeEnabled.contains(player.getUniqueId())) && (clickedItemSlot == 19)) {
            // SELL TOGGLE BUTTON
            String signID = clickedItem.getItemMeta().getLocalizedName();
            boolean toggleValue = toggleSellItems(shopInventory, signID);
            if(toggleValue) {
                updateButtonTotalValues(shopInventory, playerInventory);
            }
        }
    }

    public void buyItem(Player player, Inventory shopInventory) {
        int buyPrice = Integer.parseInt(shopInventory.getItem(2).getItemMeta().getLocalizedName());
        int cost = buyPrice * shopInventory.getItem(4).getAmount();
        Inventory playerInventory = player.getInventory();

        // rework to fill inventory with bought item until full, then send player message saying no space is available

        if(playerInventory.firstEmpty() != -1) {
            if(Global.econ.getBalance(player.getName()) > cost) {
                Global.econ.withdrawPlayer(player.getName(), cost);
                playerInventory.setItem(playerInventory.firstEmpty(), shopInventory.getItem(4));
                interact.msgPlayer("You bought " + shopInventory.getItem(4).getAmount() + " item(s) for " + cost, player);
            } else {
                interact.msgPlayer("You do not have enough money.", player);
            }
        } else {
            interact.msgPlayer("Your inventory is full.", player);
        }
    }

    public void sellItem(Player player, Inventory shopInventory) {
        int sellPrice = Integer.parseInt(shopInventory.getItem(6).getItemMeta().getLocalizedName());
        Inventory playerInventory = player.getInventory();
        List<Integer> itemsToSell = new ArrayList<>();
        boolean haveSold = false;

        // find sell items in player's inventory
        for(int i = 0; i < 36; i++) {
            if(playerInventory.getItem(i) != null) {
                if(playerInventory.getItem(i).isSimilar(shopInventory.getItem(4))) {
                    itemsToSell.add(i);
                }
            }
        }

        // get count of items sellable
        int numItemsToSell = shopInventory.getItem(4).getAmount();

        if(itemsToSell.size() == 0) {
            interact.msgPlayer("You have no items to sell.", player);
        }

        int itemsSoldCounter = 0;
        int totalMoneyGiven = 0;
        for(int i: itemsToSell) {
            for(int j = playerInventory.getItem(i).getAmount(); j > 0; j--) {
                playerInventory.getItem(i).setAmount(playerInventory.getItem(i).getAmount() - 1);
                Global.econ.depositPlayer(player.getName(), sellPrice);
                totalMoneyGiven += sellPrice;
                numItemsToSell--;
                itemsSoldCounter++;
                haveSold = true;
                if(numItemsToSell == 0) {
                    break;
                }
            }
            if(numItemsToSell == 0) {
                break;
            }
        }

        if(haveSold) {
            interact.msgPlayer("You sold " + itemsSoldCounter + " item(s) for " + totalMoneyGiven, player);
        }
    }

    public void sellAllItems(Player player, Inventory shopInventory) {
        int sellPrice = Integer.parseInt(shopInventory.getItem(6).getItemMeta().getLocalizedName());
        Inventory playerInventory = player.getInventory();
        List<Integer> itemsToSell = new ArrayList<>();
        boolean haveSold = false;

        // find sell items in player's inventory
        for(int i = 0; i < 36; i++) {
            if(playerInventory.getItem(i) != null) {
                if(playerInventory.getItem(i).isSimilar(shopInventory.getItem(4))) {
                    itemsToSell.add(i);
                }
            }
        }

        if(itemsToSell.size() == 0) {
            interact.msgPlayer("There are no items to sell.", player);
        }

        int itemsSoldCounter = 0;
        int totalMoneyGiven = 0;

        for(int i: itemsToSell) {
            haveSold = true;
            Global.econ.depositPlayer(player.getName(), sellPrice*playerInventory.getItem(i).getAmount());
            totalMoneyGiven += sellPrice * playerInventory.getItem(i).getAmount();
            itemsSoldCounter += playerInventory.getItem(i).getAmount();
            playerInventory.setItem(i, null);
        }

        if(haveSold) {
            interact.msgPlayer("You sold " + itemsSoldCounter + " item(s) for " + totalMoneyGiven, player);
        }
    }

    // To-Do: Change toggle button display name to opposite of what it was
    public boolean toggleBuyItems(Inventory shopInventory, String signID) throws IOException {
        Shop shop = Global.shopConfig.getShop(signID);
        List<String> emptyLore = new ArrayList<>();
        if(shop != null) {
            ItemStack buyToggleButton = shopInventory.getItem(18);
            ItemMeta buyToggleButtonMeta = buyToggleButton.getItemMeta();
            ItemStack buyButton = shopInventory.getItem(12);
            ItemMeta buyButtonMeta = buyButton.getItemMeta();
            if(shop.buyable) {
                shop.buyable = false;
                buyToggleButtonMeta.setDisplayName("BUY: OFF");
                shopInventory.getItem(12).setType(Material.BARRIER);
                buyButtonMeta.setDisplayName("CANNOT BUY");
                Location signLocation = Global.parser.base64ToLocation(signID);
                if(!signLocation.getBlock().isEmpty() && signLocation.getBlock().getType().equals(Material.OAK_SIGN)) {
                    if(Global.shopConfig.signIdExists(Global.parser.locationToBase64(signLocation))) {
                    }
                }
            } else  {
                shop.buyable = true;
                buyToggleButtonMeta.setDisplayName("BUY: ON");
                shopInventory.getItem(12).setType(Material.GREEN_STAINED_GLASS);
                buyButtonMeta.setDisplayName("BUY");
            }
            buyButtonMeta.setLore(emptyLore);
            shopInventory.getItem(12).setItemMeta(buyButtonMeta);
            buyToggleButton.setItemMeta(buyToggleButtonMeta);
            Global.shopConfig.writeShop(signID, shop, true);
            return shop.buyable;
        }
        return false;
    }

    // To-Do: Change toggle button display name to opposite of what it was
    public boolean toggleSellItems(Inventory shopInventory, String signID) throws IOException {
        Shop shop = Global.shopConfig.getShop(signID);
        List<String> emptyLore = new ArrayList<>();
        if(shop != null) {
            ItemStack sellToggleButton = shopInventory.getItem(19);
            ItemMeta sellToggleButtonMeta = sellToggleButton.getItemMeta();
            ItemStack sellButton = shopInventory.getItem(13);
            ItemStack sellAllButton = shopInventory.getItem(14);
            ItemMeta sellButtonMeta = sellButton.getItemMeta();
            ItemMeta sellAllButtonMeta = sellAllButton.getItemMeta();
            if(shop.sellable) {
                shop.sellable = false;
                sellToggleButtonMeta.setDisplayName("SELL: OFF");
                shopInventory.getItem(13).setType(Material.BARRIER);
                shopInventory.getItem(14).setType(Material.BARRIER);
                sellButtonMeta.setDisplayName("CANNOT SELL");
                sellAllButtonMeta.setDisplayName("CANNOT SELL");
            } else  {
                shop.sellable = true;
                sellToggleButtonMeta.setDisplayName("SELL: ON");
                shopInventory.getItem(13).setType(Material.RED_STAINED_GLASS);
                shopInventory.getItem(14).setType(Material.RED_STAINED_GLASS);
                sellButtonMeta.setDisplayName("SELL");
                sellAllButtonMeta.setDisplayName("SELL ALL");
            }
            sellAllButtonMeta.setLore(emptyLore);
            sellButtonMeta.setLore(emptyLore);
            shopInventory.getItem(13).setItemMeta(sellButtonMeta);
            shopInventory.getItem(14).setItemMeta(sellAllButtonMeta);
            sellToggleButton.setItemMeta(sellToggleButtonMeta);
            Global.shopConfig.writeShop(signID, shop, true);
            return shop.sellable;
        }
        return false;
    }

    public int sumShopItemsInPlayerInventory(Inventory shopInventory, Inventory playerInventory) {
        int shopItemsInPlayerInventory = 0;
        for(int i = 0; i < 36; i++) {
            if(playerInventory.getItem(i) != null) {
                if(playerInventory.getItem(i).isSimilar(shopInventory.getItem(4))) {
                    shopItemsInPlayerInventory += (playerInventory.getItem(i).getAmount());
                }
            }
        }
        return shopItemsInPlayerInventory;
    }

    public void updateButtonTotalValues(Inventory shopInventory, Inventory playerInventory) {
        int buyPrice = Integer.parseInt(shopInventory.getItem(2).getItemMeta().getLocalizedName());
        int sellPrice = Integer.parseInt(shopInventory.getItem(6).getItemMeta().getLocalizedName());
        int totalSellableItems = sumShopItemsInPlayerInventory(shopInventory, playerInventory);
        int sellAllPrice = totalSellableItems * sellPrice;
        int shopItemCount = shopInventory.getItem(4).getAmount();
        List<String> buyLore = new ArrayList<>();
        List<String> sellLore = new ArrayList<>();
        List<String> sellAllLore = new ArrayList<>();
        ItemMeta buyButtonMeta = shopInventory.getItem(12).getItemMeta();
        ItemMeta sellButtonMeta = shopInventory.getItem(13).getItemMeta();
        ItemMeta sellAllButtonMeta = shopInventory.getItem(14).getItemMeta();

        buyLore.add("Total: " + shopItemCount * buyPrice);
        buyButtonMeta.setLore(buyLore);

        if(totalSellableItems <= shopItemCount) {
            sellLore.add("Total: " + totalSellableItems * sellPrice);
        } else {
            sellLore.add("Total: " + shopItemCount * sellPrice);
        }
        sellButtonMeta.setLore(sellLore);

        sellAllLore.add("Total: " + sellAllPrice);
        sellAllButtonMeta.setLore(sellAllLore);

        shopInventory.getItem(12).setItemMeta(buyButtonMeta);
        shopInventory.getItem(13).setItemMeta(sellButtonMeta);
        shopInventory.getItem(14).setItemMeta(sellAllButtonMeta);
    }

    public void addToShopItem(Inventory shopInventory, int slotClicked) {
        int addAmount = shopInventory.getItem(slotClicked).getAmount();
        int currentAmount = shopInventory.getItem(4).getAmount();
        int maxStack = shopInventory.getItem(4).getMaxStackSize();

        if((addAmount + currentAmount) > maxStack) {
            shopInventory.getItem(4).setAmount(shopInventory.getItem(4).getMaxStackSize());
        } else {
            shopInventory.getItem(4).setAmount(shopInventory.getItem(4).getAmount() + addAmount);
        }
    }

    public void subtractFromShopItem(Inventory shopInventory, int slotClicked) {
        int subAmount = shopInventory.getItem(slotClicked).getAmount();
        int currentAmount = shopInventory.getItem(4).getAmount();

        if((currentAmount - subAmount) < 1) {
            shopInventory.getItem(4).setAmount(1);
        } else {
            shopInventory.getItem(4).setAmount(shopInventory.getItem(4).getAmount() - subAmount);
        }
    }

    public void openShopGUI(HumanEntity player, String signID, String title, int buyPrice, int sellPrice) {
        player.openInventory(createInventory(player, signID, title, buyPrice, sellPrice));
    }

    // To-Do: Set localized name of item 2 as buyPrice, set localized name of item 6 as sellPrice
    public Inventory createInventory(HumanEntity player,  String signID, String title, int buyPrice, int sellPrice) {
        Inventory shopInventory;
        Inventory playerInventory = player.getInventory();
        ItemStack shopItem = Global.shopConfig.getShop(signID).item;
        int totalSellableItems = getTotalItems(playerInventory, shopItem);
        int shopItemMaxStack = shopItem.getMaxStackSize();
        Boolean isBuyable= false;
        Boolean isSellable = false;
        Shop shop = Global.shopConfig.getShop(signID);

        shopItem.setAmount(1);

        if(shop != null) {
            isBuyable = shop.buyable;
            isSellable = shop.sellable;
        }

        if(!Global.editModeEnabled.contains(player.getUniqueId())) {
            shopInventory = Bukkit.createInventory(null, 18, title);
        } else {
            shopInventory = Bukkit.createInventory(null, 27, title + " (edit mode)");
        }

        // ADD BUTTONS
        ItemStack addOperator = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta addOperatorMeta = addOperator.getItemMeta();

        // SUBTRACT BUTTONS
        ItemStack subtractOperator = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta subOperatorMeta = subtractOperator.getItemMeta();

        // BUY BUTTON
        ItemStack buyOperator = new ItemStack(Material.GREEN_STAINED_GLASS);
        ItemMeta buyOperatorMeta = buyOperator.getItemMeta();
        List<String> buyOperatorLore = new ArrayList<>();
        buyOperatorLore.add("Total: " + buyPrice * shopItem.getAmount());
        buyOperatorMeta.setLore(buyOperatorLore);

        // SELL BUTTON
        ItemStack sellOperator = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta sellOperatorMeta = sellOperator.getItemMeta();
        List<String> sellOperatorLore = new ArrayList<>();
        if(totalSellableItems > shopItem.getAmount()) {
            sellOperatorLore.add("Total: " + sellPrice * shopItem.getAmount());
        } else {
            sellOperatorLore.add("Total: " + sellPrice * totalSellableItems);
        }
        sellOperatorMeta.setLore(sellOperatorLore);

        // SELL ALL BUTTON
        ItemStack sellAllOperator = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta sellAllOperatorMeta = sellAllOperator.getItemMeta();
        List<String> sellAllOperatorLore = new ArrayList<>();
        sellAllOperatorLore.add("Total: " + totalSellableItems * sellPrice);
        sellAllOperatorMeta.setLore(sellAllOperatorLore);

        // EDIT MODE BUY TOGGLE BUTTON
        ItemStack adminBuyToggleOperator = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta adminBuyToggleOperatorMeta = adminBuyToggleOperator.getItemMeta();

        // EDIT MODE SELL TOGGLE BUTTON
        ItemStack adminSellToggleOperator = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta adminSellToggleOperatorMeta = adminSellToggleOperator.getItemMeta();

        // add/subtract button count logic for showing 2 outermost buttons on either side
        if(shopItem.getMaxStackSize() >= 8) {
            subtractOperator.setAmount(shopItemMaxStack);
            String subtractOperatorName = "SUBTRACT " + shopItemMaxStack;
            subOperatorMeta.setDisplayName(subtractOperatorName);
            subtractOperator.setItemMeta(subOperatorMeta);
            shopInventory.setItem(0, subtractOperator);

            subtractOperator.setAmount(shopItemMaxStack / 4);
            subtractOperatorName = "SUBTRACT " + shopItemMaxStack / 4;
            subOperatorMeta.setDisplayName(subtractOperatorName);
            subtractOperator.setItemMeta(subOperatorMeta);
            shopInventory.setItem(1, subtractOperator);

            addOperator.setAmount(shopItemMaxStack / 4);
            String addOperatorName = "ADD " + shopItemMaxStack / 4;
            addOperatorMeta.setDisplayName(addOperatorName);
            addOperator.setItemMeta(addOperatorMeta);
            shopInventory.setItem(7, addOperator);

            addOperator.setAmount(shopItemMaxStack);
            addOperatorName = "ADD " + shopItemMaxStack;
            addOperatorMeta.setDisplayName(addOperatorName);
            addOperator.setItemMeta(addOperatorMeta);
            shopInventory.setItem(8, addOperator);
        }

        // 2 innermost add/subtract buttons
        subtractOperator.setAmount(1);
        subOperatorMeta.setDisplayName("SUBTRACT 1");
        subOperatorMeta.setLocalizedName(String.valueOf(buyPrice));
        subtractOperator.setItemMeta(subOperatorMeta);
        shopInventory.setItem(2, subtractOperator);
        shopInventory.setItem(4, shopItem);

        addOperator.setAmount(1);
        addOperatorMeta.setDisplayName("ADD 1");
        addOperatorMeta.setLocalizedName(String.valueOf(sellPrice));
        addOperator.setItemMeta(addOperatorMeta);
        shopInventory.setItem(6, addOperator);

        // logic for showing buy/sell buttons pre-shop load

        if(isBuyable) {
            buyOperatorMeta.setDisplayName("BUY");
        } else {
            List<String> emptyLore = new ArrayList<>();
            buyOperator.setType(Material.BARRIER);
            buyOperatorMeta.setDisplayName("CANNOT BUY");
            buyOperatorMeta.setLore(emptyLore);
        }
        buyOperator.setAmount(1);
        buyOperator.setItemMeta(buyOperatorMeta);
        shopInventory.setItem(12, buyOperator);

        if(isSellable) {
            sellOperatorMeta.setDisplayName("SELL");
            sellAllOperatorMeta.setDisplayName("SELL ALL");
        } else {
            List<String> emptyLore = new ArrayList<>();
            sellOperator.setType(Material.BARRIER);
            sellAllOperator.setType(Material.BARRIER);
            sellOperatorMeta.setDisplayName("CANNOT SELL");
            sellAllOperatorMeta.setDisplayName("CANNOT SELL");
            sellOperatorMeta.setLore(emptyLore);
            sellAllOperatorMeta.setLore(emptyLore);
        }
        sellOperator.setAmount(1);
        sellOperator.setItemMeta(sellOperatorMeta);
        sellAllOperator.setAmount(1);
        sellAllOperator.setItemMeta(sellAllOperatorMeta);
        shopInventory.setItem(13, sellOperator);
        shopInventory.setItem(14, sellAllOperator);

        if(Global.editModeEnabled.contains(player.getUniqueId())) {
            if(isBuyable) {
                adminBuyToggleOperatorMeta.setDisplayName("BUY: ON");
            } else {
                adminBuyToggleOperatorMeta.setDisplayName("BUY: OFF");
            }
            adminBuyToggleOperator.setAmount(1);
            adminBuyToggleOperatorMeta.setLocalizedName(signID);
            adminBuyToggleOperator.setItemMeta(adminBuyToggleOperatorMeta);
            shopInventory.setItem(18, adminBuyToggleOperator);

            if(isSellable) {
                adminSellToggleOperatorMeta.setDisplayName("SELL: ON");
            } else {
                adminSellToggleOperatorMeta.setDisplayName("SELL: OFF");
            }
            adminSellToggleOperator.setAmount(1);
            adminSellToggleOperatorMeta.setLocalizedName(signID);
            adminSellToggleOperator.setItemMeta(adminSellToggleOperatorMeta);
            shopInventory.setItem(19, adminSellToggleOperator);
        }
        // Hi Cassandra
        return shopInventory;
    }

    public int getTotalItems(Inventory inventory, ItemStack item) {
        int totalItems = 0;
        for(int i = 0; i < 36; i++) {
            if(inventory.getItem(i) != null) {
                if(inventory.getItem(i).isSimilar(item)) {
                    totalItems += (inventory.getItem(i).getAmount());
                }
            }
        }
        return totalItems;
    }
}
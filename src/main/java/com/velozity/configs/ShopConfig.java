package com.velozity.configs;

import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class ShopConfig {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VShop";
    @Getter
    private FileConfiguration shopsConfig;
    private File shopsConfigFile;

    public Boolean setupWorkspace() throws IOException {
        createShopsConfig();
        return true;
    }

    private void createShopsConfig() throws IOException {
        shopsConfigFile = new File(workPath, "shops.yml");
        if (!shopsConfigFile.exists()) {
            shopsConfigFile.getParentFile().mkdirs();
            shopsConfigFile.createNewFile();
        }

        shopsConfig= new YamlConfiguration();
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void writeShop(String signId, Shop shop) throws IOException {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if(signIdExists(signId)) {
            removeShop(signId);
        }

        shopsConfig.set("shops." + signId, shop.serialize());
        shopsConfig.options().copyDefaults(true);
        shopsConfig.save(shopsConfigFile);

        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void removeShop(String signId) throws IOException {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
            shopsConfig.set("shops." + signId, null);
            shopsConfig.save(shopsConfigFile);
    }

    public Set<String> getSignIds() {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if(shopsConfig.saveToString().trim().equals("")) {
            return Collections.emptySet();
        }

        return shopsConfig.getConfigurationSection("shops").getKeys(false);
    }

    public Boolean signIdExists(String signId) {

        if(shopsConfig.saveToString().trim().equals("")) {
            return false;
        }

        return shopsConfig.getConfigurationSection("shops").getKeys(false).contains(signId);
    }

    public Shop getShop(String signId) {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Shop shop = new Shop();
        shop.title = shopsConfig.getConfigurationSection("shops." + signId).getString("title");
        shop.item = shopsConfig.getConfigurationSection("shops." + signId).getItemStack("item");
        shop.buyprice = shopsConfig.getConfigurationSection("shops." + signId).getInt("buyprice");
        shop.sellprice = shopsConfig.getConfigurationSection("shops." + signId).getInt("sellprice");
        shop.buyable = shopsConfig.getConfigurationSection("shops." + signId).getBoolean("buyable");
        shop.sellable = shopsConfig.getConfigurationSection("shops." + signId).getBoolean("sellable");

        return shop;
    }

    public Map<String, Shop> getShops() {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Map<String, Shop> shops = new LinkedHashMap<>();
        for (String key : shopsConfig.getConfigurationSection("shops").getKeys(false)) {
            Shop shop = new Shop();

            shop.title = shopsConfig.getConfigurationSection("shops." + key).getString("title");
            shop.buyprice = shopsConfig.getConfigurationSection("shops." + key).getInt("buyprice");
            shop.sellprice = shopsConfig.getConfigurationSection("shops." + key).getInt("sellprice");
            shop.buyable = shopsConfig.getConfigurationSection("shops." + key).getBoolean("buyable");
            shop.sellable = shopsConfig.getConfigurationSection("shops." + key).getBoolean("sellable");
            shop.item = shopsConfig.getConfigurationSection("shops." + key).getItemStack("item");

            shops.put(key, shop);
        }

        return shops;
    }

    public void initiateDescChangeProcess(String signId, Player player) {
        if(player == null) {
            return;
        }

        if(player.hasPermission("vshop.editormode")) {
            if(signIdExists(signId)) {
                Global.pendingNewDesc.remove(player);
                Global.pendingNewDesc.put(player, signId);
                Global.interact.msgPlayer("Type /vs desc <new desc> - To change this items sign description.", player);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if(Global.pendingNewBuyPrice.containsKey(player)){
                                    Global.pendingNewBuyPrice.remove(player);
                                    Global.interact.msgPlayer("No desc entered - Timed out", player);
                                }
                            }
                        },
                        30000
                );
            } else {
                Global.interact.msgPlayer("That shop cannot be found", player);
            }
        } else {
            Global.interact.msgPlayer("You don't have permission to do this!", player);
        }
    }

    public void initiateBuyPriceChangeProcess(String signId, Player player) {
        if(player == null) {
            return;
        }

        if(player.hasPermission("vshop.editormode")) {
            if(signIdExists(signId)) {
                Global.pendingNewDesc.remove(player);
                Global.pendingNewBuyPrice.put(player, signId);
                Global.interact.msgPlayer("Type /vs buy <amount> - To change this items buy price.", player);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if(Global.pendingNewBuyPrice.containsKey(player)){
                                    Global.pendingNewBuyPrice.remove(player);
                                    Global.interact.msgPlayer("No buy price entered - Timed out", player);
                                }
                            }
                        },
                        20000
                );
            } else {
                Global.interact.msgPlayer("That shop cannot be found", player);
            }
        } else {
            Global.interact.msgPlayer("You don't have permission to do this!", player);
        }
    }

    public void initiateSellPriceChangeProcess(String signId, Player player) {
        if(player == null) {
            return;
        }

        if(player.hasPermission("vshop.editormode")) {
            if(signIdExists(signId)) {
                Global.pendingNewDesc.remove(player);
                Global.pendingNewSellPrice.put(player, signId);
                Global.interact.msgPlayer("Type /vs sell <amount> - To change this items sell price.", player);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if(Global.pendingNewSellPrice.containsKey(player)){
                                    Global.pendingNewSellPrice.remove(player);
                                    Global.interact.msgPlayer("No sell price entered - Timed out", player);
                                }
                            }
                        },
                        30000
                );
            } else {
                Global.interact.msgPlayer("That shop cannot be found!", player);
            }
        } else {
            Global.interact.msgPlayer("You don't have permission to do this!", player);
        }
    }
}


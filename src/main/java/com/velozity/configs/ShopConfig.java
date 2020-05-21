package com.velozity.configs;

import com.velozity.types.Shop;
import com.velozity.vshop.Main;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
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

    public FileConfiguration getShopsConfig() {
        return this.shopsConfig;
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

    public void writeShop(String signId, Shop shop, Boolean overwrite) throws IOException {
        if(overwrite) {
            if(getSignIds().contains(signId)) {
                removeShop(signId);
            }
        }

        getShopsConfig().addDefault("shops." + signId, shop.serialize());
        getShopsConfig().options().copyDefaults(true);
        getShopsConfig().save(shopsConfigFile);
    }

    public void removeShop(String signId) throws IOException {
        getShopsConfig().set("shops." + signId, null);
        getShopsConfig().save(shopsConfigFile);
    }

    public Set<String> getSignIds() {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if(getShopsConfig().saveToString().trim().equals("")) {
            return Collections.emptySet();
        }

        return getShopsConfig().getConfigurationSection("shops").getKeys(false);
    }

    public Boolean signIdExists(Integer signId) {

        if(getShopsConfig().saveToString().trim().equals("")) {
            return false;
        }

        return getShopsConfig().getConfigurationSection("shops").getKeys(false).contains(signId.toString());
    }

    public Shop getShop(String signId) {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Shop shop = new Shop();
        shop.title = getShopsConfig().getConfigurationSection("shops." + signId).getString("title");
        shop.buyprice = getShopsConfig().getConfigurationSection("shops." + signId).getInt("buyprice");
        shop.sellprice = getShopsConfig().getConfigurationSection("shops." + signId).getInt("sellprice");
        shop.buyable = getShopsConfig().getConfigurationSection("shops." + signId).getBoolean("buyable");
        shop.sellable = getShopsConfig().getConfigurationSection("shops." + signId).getBoolean("sellable");
        shop.item = getShopsConfig().getConfigurationSection("shops." + signId).getItemStack("item");

        return shop;
    }

    public Map<String, Shop> getShops() {
        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Map<String, Shop> shops = new LinkedHashMap<>();
        for (String key : getShopsConfig().getConfigurationSection("shops").getKeys(false)) {
            Shop shop = new Shop();

            shop.title = getShopsConfig().getConfigurationSection("shops." + key).getString("title");
            shop.buyprice = getShopsConfig().getConfigurationSection("shops." + key).getInt("buyprice");
            shop.sellprice = getShopsConfig().getConfigurationSection("shops." + key).getInt("sellprice");
            shop.buyable = getShopsConfig().getConfigurationSection("shops." + key).getBoolean("buyable");
            shop.sellable = getShopsConfig().getConfigurationSection("shops." + key).getBoolean("sellable");
            shop.item = getShopsConfig().getConfigurationSection("shops." + key).getItemStack("item");
            
            shops.put(key, shop);
        }

        return shops;
    }
}


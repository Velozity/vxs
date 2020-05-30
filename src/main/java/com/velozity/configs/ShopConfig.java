package com.velozity.configs;

import com.velozity.helpers.DatabaseHelper;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class ShopConfig {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VXS";
    @Getter
    private FileConfiguration shopsConfig;
    private File shopsConfigFile;

    public Boolean setupWorkspace() throws IOException, SQLException, ClassNotFoundException {
        if(!(Boolean) Global.mainConfig.readSetting("mysql", "enabled")) {
            createShopsConfig();
            Global.metrics.addCustomChart(new Metrics.SingleLineChart("shops_created", () -> getSignIds().size()));
        } else {
            Global.database.connect.runTaskAsynchronously(Global.getMainInstance);
        }


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

    public void writeShop(String signId, Shop shop) {
        if((Boolean)Global.mainConfig.readSetting("mysql", "enabled")) {
            Global.database.writeShop(signId, shop);
            return;
        }

        try {
            shopsConfig.load(shopsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if(signIdExists(signId)) {
            removeShop(signId);
        }

        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            shopsConfig.set("shops." + signId, shop.serialize());
            shopsConfig.options().copyDefaults(true);
            try {
            shopsConfig.save(shopsConfigFile);
                shopsConfig.load(shopsConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });
    }

    public void removeShop(String signId) {
        if((Boolean)Global.mainConfig.readSetting("mysql", "enabled")) {
            Global.database.removeShop(signId);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            try {
                shopsConfig.load(shopsConfigFile);
                shopsConfig.set("shops." + signId, null);
                shopsConfig.save(shopsConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });
    }

    public Set<String> getSignIds() {
        if((Boolean) Global.mainConfig.readSetting("mysql", "enabled")) {
            return Global.database.getSignIds();
        }

        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            try {
                shopsConfig.load(shopsConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });

        if (shopsConfig.saveToString().trim().equals("")) {
            return Collections.emptySet();
        }
        return shopsConfig.getConfigurationSection("shops").getKeys(false);
    }

    public Boolean signIdExists(String signId) {
        if((Boolean)Global.mainConfig.readSetting("mysql", "enabled")) {
            return Global.database.signIdExists(signId);
        }

        if(shopsConfig.saveToString().trim().equals("")) {
            return false;
        }

        return shopsConfig.getConfigurationSection("shops").getKeys(false).contains(signId);
    }

    public Shop getShop(String signId) {
        if((Boolean)Global.mainConfig.readSetting("mysql", "enabled")) {
            return Global.database.getShop(signId);
        }

        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            try {
                shopsConfig.load(shopsConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });

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
        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            try {
                shopsConfig.load(shopsConfigFile);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        });

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

        if(player.hasPermission("vxs.editmode")) {
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

        if(player.hasPermission("vxs.editmode")) {
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

        if(player.hasPermission("vxs.editmode")) {
            if(signIdExists(signId)) {
                Global.pendingNewDesc.remove(player);
                Global.pendingNewSellPrice.put(player, signId);
                Global.interact.msgPlayer("Type /vs sell <amount> - To change this items sell price.", player);
                Bukkit.getScheduler().runTaskLaterAsynchronously(Global.getMainInstance, () -> {
                    if (Global.pendingNewSellPrice.containsKey(player)) {
                        Global.pendingNewSellPrice.remove(player);
                        Global.interact.msgPlayer("No sell price entered - Timed out", player);
                    }
                }, Global.parser.secsToTicks(30));
            } else {
                Global.interact.msgPlayer("That shop cannot be found!", player);
            }
        } else {
            Global.interact.msgPlayer("You don't have permission to do this!", player);
        }
    }

    // MySQL
    public static String encodeItem(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return Base64.getEncoder().encodeToString(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    public static ItemStack decodeItem(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(Base64.getDecoder().decode((string))));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }
}


package com.velozity.configs;

import com.velozity.types.LogType;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class StatsWriter {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VShop";
    @Getter
    private FileConfiguration statsConfig;
    private File statsConfigFile;

    public Boolean setupWorkspace() throws IOException {
        createStatsConfig();
        return true;
    }

    public FileConfiguration getStatsConfig() {
        return this.statsConfig;
    }

    private void createStatsConfig() throws IOException {
        statsConfigFile = new File(workPath, "stats.yml");
        if (!statsConfigFile.exists()) {
            statsConfigFile.getParentFile().mkdirs();
            statsConfigFile.createNewFile();
        }

        statsConfig = new YamlConfiguration();
        try {
            statsConfig.load(statsConfigFile);
            writeDefaultStats();
        } catch (IOException | InvalidConfigurationException e) {
            Global.interact.logServer(LogType.error, "Could not load statsConfig file! Try restarting or checking file permissions");
        }
    }

    public void addTotalIncome(Long income) throws IOException {

        if(!(Boolean)Global.mainConfig.readSetting("system", "stats")) {
            return;
        }

        getStatsConfig().set("stats.totalincome", Long.parseLong(readStat("totalincome")) + income);
        getStatsConfig().set("stats.transactions", Long.parseLong(readStat("transactions")) + 1);
        getStatsConfig().set("stats.buycount", Long.parseLong(readStat("buycount")) + 1);
        getStatsConfig().save(statsConfigFile);

        try {
            statsConfig.load(statsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            Global.interact.logServer(LogType.error,"Something went wrong when trying to add stats in your stats.yml");
        }
    }

    public void addTotalExpenditure(Integer expenditure) throws IOException {

        if(!(Boolean)Global.mainConfig.readSetting("system", "stats")) {
            return;
        }

        getStatsConfig().set("stats.totalexpenditure", Long.parseLong(readStat("totalexpenditure")) + expenditure);
        getStatsConfig().set("stats.transactions", Long.parseLong(readStat("transactions")) + 1);
        getStatsConfig().set("stats.sellcount", Long.parseLong(readStat("sellcount")) + 1);
        getStatsConfig().options().copyDefaults(true);
        getStatsConfig().save(statsConfigFile);

        try {
            statsConfig.load(statsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            Global.interact.logServer(LogType.error,"Something went wrong when trying to add stats in your stats.yml");
        }
    }

    public String readStat(String key) {

        if(!(Boolean)Global.mainConfig.readSetting("system", "stats")) {
            return "Stats is disabled on this server";
        }

        return getStatsConfig().getConfigurationSection("stats").get(key).toString();
    }

    public void writeDefaultStats() throws IOException {
        // Shop settings
        getStatsConfig().addDefault("stats.transactions", 0);
        getStatsConfig().addDefault("stats.buycount", 0);
        getStatsConfig().addDefault("stats.sellcount", 0);

        getStatsConfig().addDefault("stats.totalincome", 0);
        getStatsConfig().addDefault("stats.totalexpenditure", 0);
        getStatsConfig().options().copyDefaults(true);
        getStatsConfig().save(statsConfigFile);

        try {
            statsConfig.load(statsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            Global.interact.logServer(LogType.error,"Something went wrong when trying to add stats in your stats.yml");
        }
    }
}


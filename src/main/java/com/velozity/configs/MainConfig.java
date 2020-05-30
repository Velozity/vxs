package com.velozity.configs;

import com.velozity.types.LogType;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MainConfig {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VXS";
    @Getter
    private FileConfiguration mainConfig;
    private File mainConfigFile;

    public Boolean setupWorkspace() throws IOException {
        createMainConfig();
        return true;
    }

    public FileConfiguration getMainConfig() {
        return this.mainConfig;
    }

    private void createMainConfig() throws IOException {
        mainConfigFile = new File(workPath, "config.yml");
        if (!mainConfigFile.exists()) {
            mainConfigFile.getParentFile().mkdirs();
            mainConfigFile.createNewFile();
        }

        mainConfig = new YamlConfiguration();
        try {
            mainConfig.load(mainConfigFile);
            writeDefaultSettings();
        } catch (IOException | InvalidConfigurationException e) {
            Global.interact.logServer(LogType.error, "Could not load mainConfig file! Try restarting or checking file permissions");
        }
    }

    public Object readSetting(String section, String key) {
        try {
            mainConfig.load(mainConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            Global.interact.logServer(LogType.error,"Something went wrong when trying to read setting: " + key + "in your config.yml");
        }

        return getMainConfig().getConfigurationSection("settings." + section).get(key);
    }

    public void updateSetting(String key, Object value) throws IOException {
        if(!getMainConfig().getConfigurationSection("settings").isConfigurationSection(key)) {
            getMainConfig().set("settings." + key, null);
            getMainConfig().save(mainConfigFile);
        }

        getMainConfig().set("settings." + key, value);
        getMainConfig().options().copyDefaults(true);
        getMainConfig().save(mainConfigFile);
    }

    public void writeDefaultSettings() throws IOException {
        // Shop settings
        getMainConfig().addDefault("settings.shop.signtitle", "§b[shop]");
        getMainConfig().addDefault("settings.shop.currencysymbol", "$");
        getMainConfig().addDefault("settings.shop.buyprefix", "§4B");
        getMainConfig().addDefault("settings.shop.sellprefix", "§2S");
        getMainConfig().addDefault("settings.shop.soldout", "§4Sold Out");
        getMainConfig().addDefault("settings.shop.guititle", "Buy {item}");

        // System settings
        getMainConfig().addDefault("settings.system.filelogging", false);
        getMainConfig().addDefault("settings.system.maxloggingsize", 10);
        getMainConfig().addDefault("settings.system.stats", true);

        // MySQL settings
        getMainConfig().addDefault("settings.mysql.enabled", false);
        getMainConfig().addDefault("settings.mysql.host", "127.0.0.1");
        getMainConfig().addDefault("settings.mysql.port", 3306);
        getMainConfig().addDefault("settings.mysql.usessl", false);
        getMainConfig().addDefault("settings.mysql.database", "db");
        getMainConfig().addDefault("settings.mysql.username", "root");
        getMainConfig().addDefault("settings.mysql.password", "1234");
        getMainConfig().options().copyDefaults(true);
        getMainConfig().save(mainConfigFile);
    }
}


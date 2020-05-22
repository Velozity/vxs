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

public class MainConfig {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VShop";
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

        getMainConfig().addDefault("settings." + key, value);
        getMainConfig().options().copyDefaults(true);
        getMainConfig().save(mainConfigFile);
    }

    public void writeDefaultSettings() throws IOException {
        // Shop settings
        getMainConfig().addDefault("settings.shop.signtitle", "§b[shop]");
        getMainConfig().addDefault("settings.shop.currencysymbol", "$");
        getMainConfig().addDefault("settings.shop.buyprefix", "§4B");
        getMainConfig().addDefault("settings.shop.sellprefix", "§2S");
        getMainConfig().addDefault("settings.shop.sellmultiple", false);

        getMainConfig().addDefault("settings.shop.guititle", "Buy {item}");

        // System settings
        getMainConfig().addDefault("settings.system.filelogging", false);
        getMainConfig().addDefault("settings.system.maxloggingsize", "10");
        getMainConfig().addDefault("settings.system.stats", true);

        // Command customization
        getMainConfig().addDefault("settings.commands.editormode", Arrays.asList("editmode", "em", "edit"));

        getMainConfig().options().copyDefaults(true);
        getMainConfig().save(mainConfigFile);
    }
}


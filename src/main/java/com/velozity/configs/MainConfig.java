package com.velozity.configs;

import com.velozity.types.Shop;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MainConfig {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String workPath = "plugins/VShop";
    @Getter
    private FileConfiguration mainConfig;
    private File mainConfigFile;

    public Boolean setupWorkspace() throws IOException {
        createShopsConfig();
        return true;
    }

    public FileConfiguration getMainConfig() {
        return this.mainConfig;
    }

    private void createShopsConfig() throws IOException {
        mainConfigFile = new File(workPath, "config.yml");
        if (!mainConfigFile.exists()) {
            mainConfigFile.getParentFile().mkdirs();
            mainConfigFile.createNewFile();
        }

        mainConfig = new YamlConfiguration();
        try {
            mainConfig.load(mainConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public Object readSetting(String key) {
        try {
            mainConfig.load(mainConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return getMainConfig().getConfigurationSection("settings").get(key);
    }

    public void updateSetting(String key, Object value) throws IOException {
        getMainConfig().addDefault("settings." + key, value);
        getMainConfig().options().copyDefaults(true);
        getMainConfig().save(mainConfigFile);

        createShopsConfig();
    }
}


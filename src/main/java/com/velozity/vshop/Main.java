package com.velozity.vshop;

import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;
import com.velozity.events.EventHandlers;

import com.velozity.expansions.PlaceholderAPIExpansion;
import com.velozity.helpers.Interactions;
import com.velozity.types.LogType;
import com.velozity.types.Shop;
import com.xorist.vshop.ShopGUI;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

import lombok.Getter;

public class Main extends JavaPlugin {

    Logger log = Global.log;
    MainConfig mainConfig = Global.mainConfig;
    ShopConfig shopConfig = Global.shopConfig;
    Interactions interact = Global.interact;

    @Override
    public void onDisable() {
        Global.log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        Global.getMainInstance = this;
        registerCommands();

        getServer().getPluginManager().registerEvents(new EventHandlers(), this);
        getServer().getPluginManager().registerEvents(new ShopGUI(), this);

        try {
            if(!mainConfig.setupWorkspace()) {
                log.severe(String.format("[%s] - Disabled due to insufficient file privileges!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
            }

            if(!shopConfig.setupWorkspace()) {
                log.severe(String.format("[%s] - Disabled due to insufficient file privileges!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
            }
            if((Boolean)mainConfig.readSetting("system", "stats"))
                Global.statsWriter.setupWorkspace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlaceholderAPIExpansion(this).register();
            interact.logServer(LogType.info, "Hooked to PlaceholderAPI");
        }

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();

        validateSigns();
        interact.logServer(LogType.info,"Loaded " + Global.shopConfig.getSignIds().size() + " shop signs");
        interact.logServer(LogType.info,"Ready to rock and roll");
    }

    public void registerCommands() {
        this.getCommand("vshop").setExecutor(new Commands());
    }

    public void validateSigns() {
        Global.shopConfig.getSignIds()
                .forEach(signId -> {
                   if(Global.parser.base64ToLocation(signId).getBlock().isEmpty()) {
                       try {
                           interact.logServer(LogType.info, "Detected missing sign from world! Deleting its shop [Item: " + Global.shopConfig.getShop(signId).item.getType().name() + "]");
                           Global.shopConfig.removeShop(signId);
                       } catch (IOException e) {
                           interact.logServer(LogType.error, "Failed to remove a shop that was attached to a missing sign");
                       }
                   }
                });
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        Global.econ = rsp.getProvider();
        return Global.econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        Global.chat = rsp.getProvider();
        return Global.chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        Global.perms = rsp.getProvider();
        return Global.perms != null;
    }
}

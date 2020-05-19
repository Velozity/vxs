package com.velozity.vshop;

import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;
import com.velozity.events.EventHandlers;
import com.velozity.helpers.Interactions;
import com.velozity.types.Shop;
import com.xorist.vshop.ShopGUI;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
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

    MainConfig mainConfig = Global.mainConfig;
    ShopConfig shopConfig = Global.shopConfig;
    Interactions interact = Global.interact;
    ShopGUI shopgui = Global.shopgui;

    @Override
    public void onDisable() {
        Global.log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EventHandlers(), this);
        getServer().getPluginManager().registerEvents(new ShopGUI(), this);

        if (!setupEconomy() ) {
            Global.log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();

        try {
            if(!shopConfig.setupWorkspace()) {
                Global.log.severe(String.format("[%s] - Disabled due to insufficient file privileges!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


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

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            Global.log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if(command.getLabel().equals("vshop") || command.getLabel().equals("vs")) {
            if (args[0].equals("get")) {
                Global.shopConfig.getShops();
            }

            if (args[0].equals("write")) {
                try {
                    shopConfig.writeShop("5000", new Shop("Awesome shop", "REDSTONE",Collections.emptyList(), 50, 10, true, true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (args[0].equals("editmode")) {
                if(player.hasPermission("vshop.createshop")) {

                    if(Global.editModeEnabled.contains(player.getUniqueId())) {
                        interact.msgPlayer("Editor mode disabled!", player);
                        Global.editModeEnabled.remove(player.getUniqueId());
                        return true;
                    }
                    Global.editModeEnabled.add(player.getUniqueId());
                    interact.msgPlayer("Editor mode enabled!", player);
                    interact.msgPlayer("Spawn a sign and title it [shop] on the first line", player);
                    interact.msgPlayer("Put the price per item on the bottom line", player);
                    interact.msgPlayer("Hit the sign with your item to sell!", player);
                }
            }

            if (args[0].equals("openshop")) {
                shopgui.openShopGUI(Material.APPLE, (HumanEntity)player, "Apple", "A very nice apple");
            }
        }

        if(command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", Global.econ.format(Global.econ.getBalance(player.getName()))));
            EconomyResponse r = Global.econ.depositPlayer(player, 1.05);
            if(r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", Global.econ.format(r.amount), Global.econ.format(r.balance)));
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        } else if(command.getLabel().equals("test-permission")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(Global.perms.has(player, "example.plugin.awesome")) {
                sender.sendMessage("You are awesome!");
            } else {
                sender.sendMessage("You suck!");
            }
            return true;
        } else {
            return false;
        }
    }

    public static Economy getEconomy() {
        return Global.econ;
    }

    public static Permission getPermissions() {
        return Global.perms;
    }

    public static Chat getChat() {
        return Global.chat;
    }

}

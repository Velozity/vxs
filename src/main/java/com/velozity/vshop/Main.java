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
    ShopGUI shopgui = Global.shopgui;

    @Override
    public void onDisable() {
        Global.log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        Global.getMainInstance = this;
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

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            Global.log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if (command.getLabel().equals("vshop") || command.getLabel().equals("vs")) {
            if (args[0].equalsIgnoreCase("editmode") || args[0].equalsIgnoreCase("em") || args[0].equalsIgnoreCase("edit")) {
                if (player.hasPermission(Global._permEditorMode)) {
                    if (Global.editModeEnabled.contains(player.getUniqueId())) {
                        interact.msgPlayer("Editor mode disabled!", player);
                        Global.editModeEnabled.remove(player.getUniqueId());
                        Global.interact.logServer(LogType.info, player.getDisplayName() + " exited editor mode");
                        return true;
                    }
                    Global.editModeEnabled.add(player.getUniqueId());
                    Global.interact.logServer(LogType.info, player.getDisplayName() + " entered editor mode");
                    interact.msgPlayer("Editor mode enabled!", player);
                    interact.msgPlayer("Spawn a sign and title it [shop] on the first line", player);
                    interact.msgPlayer("Put the price per item on the bottom line", player);
                    interact.msgPlayer("Hit the sign with your item to sell!", player);
                    interact.msgPlayer("Exit editor mode by typing /vs em again", player);
                } else {
                    interact.msgPlayer("You do not have access to this command", player);
                }
            }
            else if (args[0].equalsIgnoreCase("buy")) {
                if (Global.pendingNewBuyPrice.containsKey(player)) {
                    if (Global.parser.signPrice(args[1]) > -1) {
                        String signId = Global.pendingNewBuyPrice.get(player);
                        if (!Global.parser.base64ToLocation(signId).getBlock().isEmpty()) {
                            Sign sign = (Sign) Global.parser.base64ToLocation(signId).getBlock().getState();
                            Integer price = Global.parser.signPrice(args[1]);

                            Shop shop = Global.shopConfig.getShop(signId);
                            shop.buyprice = price;

                            try {
                                Global.shopConfig.writeShop(signId, shop);
                            } catch (IOException e) {
                                interact.msgPlayer("An error occured making this change", player);
                                return true;
                            }

                            sign.setLine(2, Global.mainConfig.readSetting("shop", "buyprefix") + " " + Global.mainConfig.readSetting("shop", "currencysymbol") + price);
                            sign.update(true);
                            Global.pendingNewBuyPrice.remove(player);
                            interact.msgPlayer("You have changed the buy price to " + (String)Global.mainConfig.readSetting("shop", "currencysymbol") + price, player);
                        }
                    } else {
                        interact.msgPlayer("You entered an invalid amount", player);
                        Global.pendingNewBuyPrice.remove(player);
                        return true;
                    }
                } else {
                    interact.msgPlayer("You have not selected a shop to change the buy price for. Try to right click a sign shop in editor mode", player);
                    return true;
                }
            }
            else if (args[0].equalsIgnoreCase("sell")) {
                if (Global.pendingNewSellPrice.containsKey(player)) {
                    if (Global.parser.signPrice(args[1]) > -1) {
                        String signId = Global.pendingNewSellPrice.get(player);
                        if (!Global.parser.base64ToLocation(signId).getBlock().isEmpty()) {
                            Sign sign = (Sign)Global.parser.base64ToLocation(signId).getBlock().getState();
                            Integer price = Global.parser.signPrice(args[1]);

                            Shop shop = Global.shopConfig.getShop(signId);
                            shop.sellprice = price;

                            try {
                                Global.shopConfig.writeShop(signId, shop);
                            } catch (IOException e) {
                                interact.msgPlayer("An error occurred making this change", player);
                                Global.pendingNewSellPrice.remove(player);
                                return true;
                            }

                            sign.setLine(3, Global.mainConfig.readSetting("shop", "sellprefix") + " " + Global.mainConfig.readSetting("shop", "currencysymbol") + price);
                            sign.update(true);
                            Global.pendingNewSellPrice.remove(player);
                            interact.msgPlayer("You have changed the sell price to " + (String)Global.mainConfig.readSetting("shop", "currencysymbol") + price, player);
                        }
                    } else {
                        interact.msgPlayer("You entered an invalid amount", player);
                        Global.pendingNewSellPrice.remove(player);
                        return true;
                    }
                } else {
                    interact.msgPlayer("You have not selected a shop to change the sell price for. Try to right click a sign shop in editor mode", player);
                    return true;
                }
            }
            else if (args[0].equalsIgnoreCase("desc")) {
                if (Global.pendingNewDesc.containsKey(player)) {
                    if (Global.parser.signPrice(args[1]) > -1) {
                        String signId = Global.pendingNewDesc.get(player);
                        if (!Global.parser.base64ToLocation(signId).getBlock().isEmpty()) {
                            Sign sign = (Sign)Global.parser.base64ToLocation(signId).getBlock().getState();
                            String desc = Arrays.stream(args)
                                    .skip(1)
                                    .collect(Collectors.joining());

                            if(desc.length() <= 15) {
                                sign.setLine(1, desc);
                                sign.update(true);
                                Global.pendingNewDesc.remove(player);
                                interact.msgPlayer("You have changed the desc of this shop to '" + desc + "'", player);
                            } else {
                                interact.msgPlayer("The character length of a new desc must be less than 15 characters", player);
                                Global.pendingNewDesc.remove(player);
                            }
                        }
                    } else {
                        interact.msgPlayer("You entered an invalid amount", player);
                        Global.pendingNewSellPrice.remove(player);
                        return true;
                    }
                } else {
                    interact.msgPlayer("You have not selected a shop to change the sell price for. Try to right click a sign shop in editor mode", player);
                    return true;
                }
            }
            else if(args[0].equals("stats")) {
                if(player.hasPermission(Global._permStats)) {
                    if(!(Boolean)mainConfig.readSetting("system", "stats")) {
                        interact.msgPlayer("Stats is disabled on this server", player);
                        return true;
                    }

                    interact.msgPlayer(
                            new String[] {
                                    "Shop Count: " + Global.shopConfig.getSignIds().size(),
                                    "Total Buys: " + Global.statsWriter.readStat("buycount"),
                                    "Total Sells: " + Global.statsWriter.readStat("sellcount"),
                                    "Total Transactions: " + Global.statsWriter.readStat("transactions"),
                                    "",
                                    "Total Income: " + Global.statsWriter.readStat("totalincome"),
                                    "Total Expenditure: " + Global.statsWriter.readStat("totalexpenditure")
                            }, player
                    );
                } else {
                    interact.msgPlayer("You do not have access to this command", player);
                }
            }
            else if (args[0].equals("help")) {
                List<String> toPrint = new ArrayList<>();

                if (player.hasPermission(Global._permEditorMode))
                    toPrint.add("/vs <editmode/edit/em> - Toggle editor mode");

                if (player.hasPermission(Global._permStats))
                    toPrint.add("/vs stats - Show some neat statistics");

                toPrint.add("/vs help - Show this message");
                interact.msgPlayer(toPrint.toArray(new String[0]), player);
            }

            else {
                interact.msgPlayer("Invalid command! Use /help", player);
            }
        }
        return false;
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

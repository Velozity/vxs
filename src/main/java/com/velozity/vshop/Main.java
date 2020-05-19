package com.velozity.vshop;

import com.velozity.helpers.FileIO;
import com.velozity.types.Shop;
import com.xorist.vshop.ShopGUI;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

public class Main extends JavaPlugin implements Listener {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    private final FileIO fileio = new FileIO();
    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();

        try {
            if(!fileio.setupWorkspace()) {
                log.severe(String.format("[%s] - Disabled due to insufficient file privileges!", getDescription().getName()));
                getServer().getPluginManager().disablePlugin(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("vshop.createshop")) {
            if(e.getLine(0).toLowerCase().equals("[shop]")) {
                e.setLine(0, "[shop]");
            }
        }
    }

    List<UUID> modeEnabled = new ArrayList<>();
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        if(modeEnabled.isEmpty()) { return; }
        log.info("1");
        if(modeEnabled.contains(e.getPlayer().getUniqueId())) {
            log.info("2");
            log.info(e.getBlock().getType().toString());
            if (e.getBlock().getType().equals(Material.SIGN) || e.getBlock().getType().equals(Material.WALL_SIGN) || e.getBlock().getType().equals(Material.LEGACY_SIGN_POST)) {

                org.bukkit.block.Sign ws = (org.bukkit.block.Sign)e.getBlock().getState();
                if(ws.getLine(0).toLowerCase().equals("[shop]")) {
                    e.setCancelled(true);
                    ws.setLine(1, "Nice one,");
                    ws.setLine(2, "Gazza!");
                }
            }
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
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if(!(sender instanceof Player)) {
            log.info("Only players are supported for this Example Plugin, but you should not do this!!!");
            return true;
        }

        Player player = (Player) sender;

        if(command.getLabel().equals("vshop") || command.getLabel().equals("vs")) {
            if (args[0].equals("writeitem")) {

            }

            if (args[0].equals("editmode")) {
                if(player.hasPermission("vshop.createshop")) {

                    if(modeEnabled.contains(player.getUniqueId())) {
                        player.sendMessage("Editor mode disabled!");
                        modeEnabled.remove(player.getUniqueId());
                        return true;
                    }
                    modeEnabled.add(player.getUniqueId());
                    player.sendMessage("Editor mode enabled!");
                    player.sendMessage("Spawn a sign and title it [shop] on the first line");
                    player.sendMessage("Put the price per item on the bottom line");
                    player.sendMessage("Hit the sign with your item to sell!");
                }
            }


        }

        if(command.getLabel().equals("test-economy")) {
            // Lets give the player 1.05 currency (note that SOME economic plugins require rounding!)
            sender.sendMessage(String.format("You have %s", econ.format(econ.getBalance(player.getName()))));
            EconomyResponse r = econ.depositPlayer(player, 1.05);
            if(r.transactionSuccess()) {
                sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
            } else {
                sender.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
            return true;
        } else if(command.getLabel().equals("test-permission")) {
            // Lets test if user has the node "example.plugin.awesome" to determine if they are awesome or just suck
            if(perms.has(player, "example.plugin.awesome")) {
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
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

}

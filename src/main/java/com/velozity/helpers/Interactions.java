package com.velozity.helpers;

import com.velozity.types.LogType;
import com.velozity.vshop.Global;
import com.velozity.vshop.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Interactions {
    public void logServer(LogType type, String msg) {
        String logType = "";
        switch(type) {
            case info:
                logType = "INFO: ";
                break;
            case error:
                logType = "ERROR: ";
                break;
            case warning:
                logType = "WARNING: ";
                break;
            default:
                logType = "UNKNOWN: ";
                break;
        }

        Global.getMainInstance.getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "[VShop] " + logType + msg);
    }
    public void msgPlayer(String msg, Player player) {
        player.sendMessage("§4[VShop]§r " + msg);
    }
}

package com.velozity.helpers;

import com.velozity.vshop.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Interactions {
    public void msgPlayer(String msg, Player player) {
        player.sendMessage("§4[VShop]§r " + msg);
    }
}

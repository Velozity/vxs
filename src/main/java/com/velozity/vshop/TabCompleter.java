package com.velozity.vshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        return Arrays.asList("editmode", "em", "edit", "buy", "sell", "desc", "stats", "help");
    }
}

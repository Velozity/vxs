package com.velozity.vshop;

import com.velozity.types.LogType;
import com.velozity.types.Shop;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            Global.log.info("Only players are supported for VXS");
            return true;
        }

        Player player = (Player) sender;

        if (commandLabel.equals("vxs") || commandLabel.equals("vs")) {
            if(args.length == 0) {
                Global.interact.msgPlayer("Type /vs help for more information", player);
                return true;
            }

            if (args[0].equalsIgnoreCase("editmode") || args[0].equalsIgnoreCase("em") || args[0].equalsIgnoreCase("edit")) {
                if (player.hasPermission(Global._permEditorMode)) {
                    if (Global.editModeEnabled.contains(player.getUniqueId())) {
                        Global.interact.msgPlayer("Edit mode disabled", player);
                        Global.editModeEnabled.remove(player.getUniqueId());
                        Global.interact.logServer(LogType.info, player.getDisplayName() + " left edit mode");
                        return true;
                    }
                    Global.editModeEnabled.add(player.getUniqueId());
                    Global.interact.logServer(LogType.info, player.getDisplayName() + " entered edit mode");
                    Global.interact.msgPlayer("Edit mode enabled", player);
                } else {
                    Global.interact.msgPlayer("You do not have access to this command", player);
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

                            Global.shopConfig.writeShop(signId, shop);

                            if(shop.buyable) {
                                sign.setLine(2, Global.mainConfig.readSetting("shop", "buyprefix") + " " + Global.mainConfig.readSetting("shop", "currencysymbol") + price);
                                sign.update(true);
                            }
                            Global.pendingNewBuyPrice.remove(player);
                            Global.interact.msgPlayer("You have changed the buy price to " + (String)Global.mainConfig.readSetting("shop", "currencysymbol") + price, player);
                        }
                    } else {
                        Global.interact.msgPlayer("You entered an invalid amount", player);
                        return true;
                    }
                } else {
                    Global.interact.msgPlayer("You have not selected a shop to change the buy price for. Try to right click a sign shop in editor mode", player);
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

                            Global.shopConfig.writeShop(signId, shop);
                            if(shop.sellable) {
                                sign.setLine(3, Global.mainConfig.readSetting("shop", "sellprefix") + " " + Global.mainConfig.readSetting("shop", "currencysymbol") + price);
                                sign.update(true);
                            }
                            Global.pendingNewSellPrice.remove(player);
                            Global.interact.msgPlayer("You have changed the sell price to " + (String)Global.mainConfig.readSetting("shop", "currencysymbol") + price, player);
                        }
                    } else {
                        Global.interact.msgPlayer("You entered an invalid amount", player);
                        return true;
                    }
                } else {
                    Global.interact.msgPlayer("You have not selected a shop to change the sell price for. Try to right click a sign shop in editor mode", player);
                    return true;
                }
            }
            else if (args[0].equalsIgnoreCase("desc")) {
                if (Global.pendingNewDesc.containsKey(player)) {
                    String signId = Global.pendingNewDesc.get(player);
                    if (!Global.parser.base64ToLocation(signId).getBlock().isEmpty()) {
                        Sign sign = (Sign)Global.parser.base64ToLocation(signId).getBlock().getState();
                        String desc = Arrays.stream(args)
                                .skip(1)
                                .collect(Collectors.joining(" "));

                        if(desc.length() <= 15) {
                            sign.setLine(1, desc);
                            sign.update(true);
                            Global.pendingNewDesc.remove(player);
                            Global.interact.msgPlayer("You have changed the desc of this shop to '" + desc + "'", player);
                        } else {
                            Global.interact.msgPlayer("The character length of a new desc must be less than 15 characters", player);
                            return true;
                        }
                    }
                } else {
                    Global.interact.msgPlayer("You have not selected a shop to change the desc for. Try to right click a sign shop in edit mode", player);
                    return true;
                }
            }
            else if(args[0].equals("stats")) {
                if(player.hasPermission(Global._permStats)) {
                    if(!(Boolean)Global.mainConfig.readSetting("system", "stats")) {
                        Global.interact.msgPlayer("Stats is disabled on this server", player);
                        return true;
                    }

                    Global.interact.msgPlayer(
                            new String[] {
                                    "Shop Count: " + Global.shopConfig.getSignIds().size(),
                                    "",
                                    "Total Transactions: " + Global.statsWriter.readStat("transactions"),
                                    "Total Buys: " + Global.statsWriter.readStat("buycount"),
                                    "Total Sells: " + Global.statsWriter.readStat("sellcount"),
                                    "",
                                    "Total Income: " + Global.mainConfig.readSetting("shop", "currencysymbol") + Global.statsWriter.readStat("totalincome"),
                                    "Total Expenditure: " + Global.mainConfig.readSetting("shop", "currencysymbol") + Global.statsWriter.readStat("totalexpenditure")
                            }, player
                    );
                } else {
                    Global.interact.msgPlayer("You do not have access to this command", player);
                }
            }
            else if (args[0].equals("help")) {
                Global.interact.msgPlayer(new String[] {
                        "Help:",
                        "/vs <editmode/edit/em> - Toggle edit mode",
                        "/vs buy <price> - Right click a sign in edit mode and change the buy price for more info",
                        "/vs sell <price> - Right click a sign in edit mode and change the sell price for more info",
                        "/vs title <title> - Right click a sign in edit mode and change the title for more info",
                        "/vs stats - Show some neat statistics",
                        "/vs help - Show this message"
                }, player);
            }

            else {
                Global.interact.msgPlayer("Invalid command! Use /vs help", player);
            }
        }
        return false;
    }
}

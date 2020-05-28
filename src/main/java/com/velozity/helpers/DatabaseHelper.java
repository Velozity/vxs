package com.velozity.helpers;

import com.velozity.configs.ShopConfig;
import com.velozity.types.LogType;
import com.velozity.types.Shop;
import com.velozity.vshop.Global;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

public class DatabaseHelper {
    private Connection connection;
    private String host, database, username, password;
    private int port;
    private Boolean ssl;

    public boolean openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return true;
        }

        host = (String)Global.mainConfig.readSetting("mysql", "host");
        port = (int)Global.mainConfig.readSetting("mysql", "port");
        database = (String)Global.mainConfig.readSetting("mysql", "database");
        username = (String)Global.mainConfig.readSetting("mysql", "username");
        password = (String)Global.mainConfig.readSetting("mysql", "password");
        ssl = (Boolean)Global.mainConfig.readSetting("mysql", "usessl");
        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return true;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database + "?&useSSL=" + ssl.toString(), this.username, this.password);

        }

        return true;
    }

    public BukkitRunnable connect = new BukkitRunnable() {
        @Override
        public void run() {
            try {
                openConnection();

                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + database + "`.`vxshop_shops` (\n" +
                        "  `id` varchar(500) NOT NULL,\n" +
                        "  `item` varchar(500) NOT NULL,\n" +
                        "  `title` varchar(200) NOT NULL,\n" +
                        "  `buyprice` int DEFAULT '0',\n" +
                        "  `sellprice` int DEFAULT '0',\n" +
                        "  `buyable` tinyint NOT NULL DEFAULT '0',\n" +
                        "  `sellable` tinyint NOT NULL DEFAULT '0',\n" +
                        "  PRIMARY KEY (`id`),\n" +
                        "  UNIQUE KEY `id_UNIQUE` (`id`)\n" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

                statement.closeOnCompletion();
                Global.metrics.addCustomChart(new Metrics.SingleLineChart("shops_created", () -> getSignIds().size()));
            } catch(ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                Global.interact.logServer(LogType.error, "COULD NOT MAKE CONNECTION TO MYSQL, CHECK VSX/CONFIG.YML");
            }
        }
    };

    public void writeShop(String signId, Shop shop) {
        removeShop(signId);
        Bukkit.getScheduler().runTaskAsynchronously(Global.getMainInstance, () -> {
            try {
                openConnection();

                Statement statement = connection.createStatement();
                statement.executeUpdate("INSERT INTO `" + database + "`.`vxshop_shops` (id, item, title, buyprice, sellprice, buyable, sellable) VALUES ('" + signId + "', '" + ShopConfig.encodeItem(shop.item) + "', '" + shop.title + "', " + shop.buyprice + ", " + shop.sellprice + ", " + shop.buyable + ", " + shop.sellable + ");");
                statement.closeOnCompletion();
            } catch (SQLException | ClassNotFoundException throwables) {
                throwables.printStackTrace();
                Global.interact.logServer(LogType.error, "COULD NOT MAKE CONNECTION TO MYSQL, CHECK VSX/CONFIG.YML");
            }
        });
    }

    public void removeShop(String signId) {
            try {
                openConnection();

                Statement statement = connection.createStatement();
                statement.executeUpdate("DELETE FROM `" + database + "`.`vxshop_shops` WHERE (`id` = '" + signId + "');");
                statement.closeOnCompletion();
            } catch (SQLException | ClassNotFoundException throwables) {

            }
    }

    public Set<String> getSignIds() {
            try {
                openConnection();

                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id FROM `" + database + "`.`vxshop_shops`");
                Set<String> set = new LinkedHashSet<>();

                while(rs.next()) {
                    set.add(rs.getString(1));
                }

                statement.closeOnCompletion();
                return set;
            } catch (SQLException | ClassNotFoundException throwables) {
                return Collections.emptySet();
            }
    }

    public Boolean signIdExists(String signId) {
        try {
            openConnection();

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT id FROM `" + database + "`.`vxshop_shops` WHERE id = " + "'" + signId + "'");

            if(rs.next()) {
                statement.closeOnCompletion();
                return true;
            }

            statement.closeOnCompletion();
            return false;
        } catch (SQLException | ClassNotFoundException throwables) {
            return false;
        }
    }

    public Shop getShop(String signId) {
        try {
            openConnection();

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `" + database + "`.`vxshop_shops` WHERE id = " + "'" + signId + "'");

            if(rs.next()) {
                Shop shop = new Shop();
                shop.item = ShopConfig.decodeItem(rs.getString("item"));
                shop.title = rs.getString("title");
                shop.buyprice = rs.getInt("buyprice");
                shop.sellprice = rs.getInt("sellprice");
                shop.buyable = rs.getBoolean("buyable");
                shop.sellable = rs.getBoolean("sellable");

                statement.closeOnCompletion();
                return shop;
            }

            return null;
        } catch (SQLException | ClassNotFoundException throwables) {
            return null;
        }
    }
}

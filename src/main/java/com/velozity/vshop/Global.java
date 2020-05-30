package com.velozity.vshop;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;
import com.velozity.configs.StatsWriter;
import com.velozity.configs.StatsWriter;
import com.velozity.helpers.DatabaseHelper;
import com.velozity.helpers.Interactions;
import com.velozity.helpers.Parsers;
import com.xorist.vshop.ShopGUI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

public class Global {

    public static final int projectId = 4;
    public static Main getMainInstance;

    public static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public static Metrics metrics = null;

    public static final MainConfig mainConfig = new MainConfig();
    public static final ShopConfig shopConfig = new ShopConfig();
    public static final DatabaseHelper database = new DatabaseHelper();
    public static final StatsWriter statsWriter = new StatsWriter();
    public static final ShopGUI shopgui = new ShopGUI();
    public static final Interactions interact = new Interactions();
    public static final Parsers parser = new Parsers();

    public static List<Material> signTypes = Arrays.asList(
            Material.OAK_SIGN,
            Material.OAK_WALL_SIGN,
            Material.SPRUCE_SIGN,
            Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_SIGN,
            Material.BIRCH_WALL_SIGN,
            Material.JUNGLE_SIGN,
            Material.JUNGLE_WALL_SIGN,
            Material.ACACIA_SIGN,
            Material.ACACIA_WALL_SIGN,
            Material.DARK_OAK_SIGN,
            Material.DARK_OAK_WALL_SIGN
    );

    public static List<UUID> editModeEnabled = new ArrayList<>();
    public static List<String> pendingRemoveSigns = new ArrayList<>();
    public static Map<Player, String> pendingNewBuyPrice = new HashMap<>();
    public static Map<Player, String> pendingNewSellPrice = new HashMap<>();
    public static Map<Player, String> pendingNewDesc = new HashMap<>();

    public static final String _permCreateShop = "vshop.createshop";
    public static final String _permDestroyShop = "vshop.destroyshop";
    public static final String _permEditorMode = "vshop.editormode";
    public static final String _permStats = "vshop.stats";

}

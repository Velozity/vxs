package com.velozity.vshop;

import com.velozity.configs.MainConfig;
import com.velozity.configs.ShopConfig;
import com.velozity.configs.StatsWriter;
import com.velozity.configs.StatsWriter;
import com.velozity.helpers.Interactions;
import com.velozity.helpers.Parsers;
import com.xorist.vshop.ShopGUI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Global {

    public static Main getMainInstance;

    public static final Logger log = Logger.getLogger("Minecraft");
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;

    public static final MainConfig mainConfig = new MainConfig();
    public static final ShopConfig shopConfig = new ShopConfig();
    public static final StatsWriter statsWriter = new StatsWriter();
    public static final ShopGUI shopgui = new ShopGUI();
    public static final Interactions interact = new Interactions();
    public static final Parsers parser = new Parsers();

    public static List<UUID> editModeEnabled = new ArrayList<>();
    public static List<String> pendingRemoveSigns = new ArrayList<>();

    public static final String _permCreateShop = "vshop.createshop";
    public static final String _permDestroyShop = "vshop.destroyshop";
    public static final String _permEditorMode = "vshop.editormode";
    public static final String _permStats = "vshop.stats";

}

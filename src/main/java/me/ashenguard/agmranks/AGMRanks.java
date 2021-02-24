package me.ashenguard.agmranks;

import me.ashenguard.agmranks.commands.CommandAGMRanks;
import me.ashenguard.agmranks.commands.CommandRanks;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.gui.GUI;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.spigot.SpigotPlugin;

import java.io.File;

public final class AGMRanks extends SpigotPlugin {
    private static AGMRanks instance = null;
    public static AGMRanks getInstance() {
        return instance;
    }

    public static GUI getGUI() {
        return getInstance().GUI;
    }
    public static UserManager getUsers() {
        return getInstance().userManager;
    }
    public static RankManager getRankManager() {
        return getInstance().rankManager;
    }
    public static Vault getVault() {
        return getInstance().vault;
    }
    public static Messenger getMessenger() {
        return getInstance().messenger;
    }

    public GUI GUI = null;
    public UserManager userManager = null;
    public RankManager rankManager = null;
    public Vault vault = null;

    @Override
    public int getBStatsID() {
        return 7704;
    }

    @Override
    public int getSpigotID() {
        return 75787;
    }

    public void loadPlugin() {
        saveDefaultConfig();
        reloadConfig();

        updateNotification = getConfig().getBoolean("Check.PluginUpdates", true);

        vault = new Vault();
        rankManager = new RankManager();
        userManager = new UserManager();
        GUI = new GUI(this);
        rankManager.loadRanks();
    }

    @Override
    public void onEnable() {
        instance = this;

        if (getServer().getPluginManager().getPlugin("AGMCore") == null) {
            messenger.Warning("AGMCore is not installed. Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        File pluginFolder = getDataFolder();
        if (!pluginFolder.exists() && pluginFolder.mkdirs()) messenger.Debug("General", "Plugin folder wasn't found, A new one created");
        if (isLegacy()) messenger.Debug("General", "Legacy version detected");

        loadPlugin();

        if (PHManager.enable) new Placeholders().register();
        messenger.Info("Plugin has been enabled successfully");

        // ---- Setup data ---- //
        new CommandAGMRanks();
        new CommandRanks();

        // ---- Login all players ---- //
        for (User user: UserManager.getOnlineUsers()) user.login();
    }

    @Override
    public void onDisable() {
        if (GUI != null) GUI.closeAll();
        for (User user: UserManager.getOnlineUsers()) user.logout();
        messenger.Info("Plugin has been disabled");
    }
}

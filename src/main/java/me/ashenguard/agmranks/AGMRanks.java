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
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

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
    public @NotNull List<String> getRequirements() {
        return Collections.singletonList("AGMCore");
    }

    @Override
    public int getBStatsID() {
        return 7704;
    }

    @Override
    public int getSpigotID() {
        return 75787;
    }

    @Override
    public void onPluginEnable() {
        instance = this;

        vault = new Vault();
        rankManager = new RankManager();
        userManager = new UserManager();
        GUI = new GUI(this);
        rankManager.loadRanks();

        if (PHManager.enable) new Placeholders().register();

        new CommandAGMRanks();
        new CommandRanks();

        for (User user: UserManager.getOnlineUsers()) user.login();
    }

    @Override
    public void onPluginDisable() {
        if (GUI != null) GUI.closeAll();
        for (User user: UserManager.getOnlineUsers()) user.logout();
    }
}

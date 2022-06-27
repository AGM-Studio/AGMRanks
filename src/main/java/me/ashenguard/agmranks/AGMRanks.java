package me.ashenguard.agmranks;

import me.ashenguard.agmranks.commands.CommandAGMRanks;
import me.ashenguard.agmranks.commands.CommandRanks;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PlaceholderManager;
import me.ashenguard.api.spigot.SpigotPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public final class AGMRanks extends SpigotPlugin {
    private static AGMRanks instance = null;

    public static AGMRanks getInstance() {
        return instance;
    }
    public static Messenger getMessenger() {
        return getInstance().messenger;
    }

    public RankManager rankManager = null;

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

        new RankManager().loadRanks();

        if (PlaceholderManager.enable) new Placeholders().register();

        new CommandAGMRanks();
        new CommandRanks();

        // TODO Login Players
    }
}

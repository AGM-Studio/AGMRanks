package me.ashenguard.agmranks.api;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.track.Track;
import net.luckperms.api.track.TrackManager;
import org.jetbrains.annotations.NotNull;

public class LuckPermsAPI {
    private static LuckPerms instance;

    public static LuckPerms getInstance() {
        if (LuckPermsAPI.instance == null) LuckPermsAPI.instance = LuckPermsProvider.get();
        return instance;
    }

    public static TrackManager getTrackManager() {
        return getInstance().getTrackManager();
    }

    public static Track getTrack(@NotNull String name) {
        return getTrackManager().getTrack(name);
    }
}

package me.ashenguard.agmranks.api;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankBatch;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.track.Track;
import net.luckperms.api.track.TrackManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class LuckPermsAPI {
    private static LuckPerms instance;

    public static LuckPerms getInstance() {
        if (LuckPermsAPI.instance == null) LuckPermsAPI.instance = LuckPermsProvider.get();
        return instance;
    }

    public static UserManager getUserManager() {
        return getInstance().getUserManager();
    }

    public static User getUser(UUID uuid) {
        return getUserManager().getUser(uuid);
    }

    public static void saveUser(User user) {
        getUserManager().saveUser(user);
    }

    public static TrackManager getTrackManager() {
        return getInstance().getTrackManager();
    }

    public static Track getTrack(@NotNull String name) {
        return getTrackManager().getTrack(name);
    }

    public static GroupManager getGroupManager() {
        return getInstance().getGroupManager();
    }

    public static Group createAndLoadGroup(String name) {
        try {
            return getGroupManager().createAndLoadGroup(name).get();
        } catch (InterruptedException | ExecutionException ignored) {
            return null;
        }
    }

    public static Group addGroupToTrack(Rank rank, int prestige, Track track) {
        RankBatch batch = rank.getBatch();
        String name = String.format("agmranks.%s.%d_%d", batch.getID(), prestige, rank.getID());
        String pre = rank.getID() > 0 ? String.format("agmranks.%s.%d_%d", batch.getID(), prestige, rank.getID()) : null;
        Group group = createAndLoadGroup(name);
        assert group != null : String.format("Unable to create group \"%s\"", name);
        if (pre != null) group.data().add(InheritanceNode.builder(pre).build());

        if (track.getGroups().size() == 0 || track.getGroups().size() < rank.getID()) track.appendGroup(group);
        else track.insertGroup(group, rank.getID());

        return group;
    }
}

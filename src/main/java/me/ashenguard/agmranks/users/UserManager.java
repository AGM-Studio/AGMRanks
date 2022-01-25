package me.ashenguard.agmranks.users;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.api.messenger.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class UserManager implements Listener {
    private static final AGMRanks plugin = AGMRanks.getInstance();
    private static final Messenger messenger = AGMRanks.getMessenger();

    private static final LinkedHashMap<OfflinePlayer, User> cache = new LinkedHashMap<>();
    static class CacheCleaner implements Runnable {
        @Override
        public void run() {
            List<OfflinePlayer> outdated = new ArrayList<>();
            for (Map.Entry<OfflinePlayer, User> temp: cache.entrySet()) {
                temp.getValue().cache -= 1;
                if (temp.getValue().cache <= 0) {
                    temp.getValue().save();
                    outdated.add(temp.getKey());
                }
            }
            for (OfflinePlayer player: outdated) cache.remove(player);
        }
    }
    public static void cleanCache() {
        List<OfflinePlayer> outdated = new ArrayList<>();
        for (Map.Entry<OfflinePlayer, User> temp: cache.entrySet()) {
            if (temp.getValue().cache <= 0) {
                temp.getValue().save();
                outdated.add(temp.getKey());
            }
        }
        for (OfflinePlayer player: outdated) cache.remove(player);
    }
    static {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new CacheCleaner(), 10, 10);
    }

    public static User getUser(OfflinePlayer player) {
        if (player == null) return null;
        User user = cache.getOrDefault(player, null);
        if (user == null) {
            user = new User(player);
            cache.putIfAbsent(player, user);
        }
        user.cache += 6;
        return user;
    }

    public static List<User> getOnlineUsers() {
        return Bukkit.getOnlinePlayers().stream().map(UserManager::getUser).collect(Collectors.toList());
    }

    public static List<User> getOfflineUsers() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).map(UserManager::getUser).collect(Collectors.toList());
    }

    public UserManager() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        messenger.Debug("Users", "Listener has been registered");
    }

    @EventHandler public void onJoin(PlayerJoinEvent event) {
        if (AGMRanks.areRanksLoaded()) getUser(event.getPlayer()).login();
    }

    @EventHandler public void onLeave(PlayerQuitEvent event) {
        if (AGMRanks.areRanksLoaded()) getUser(event.getPlayer()).logout();
    }
}

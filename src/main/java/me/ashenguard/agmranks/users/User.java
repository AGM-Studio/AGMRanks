package me.ashenguard.agmranks.users;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.systems.RankingSystem;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.lib.events.agmranks.RankUpEvent;
import me.ashenguard.lib.statistics.Playtime;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;

public class User {
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    private static final AGMRanks plugin = AGMRanks.getInstance();
    private static final Messenger messenger = AGMRanks.getMessenger();

    private final RankingSystem system = plugin.rankManager.rankingSystem;
    private final Configuration config;

    public final OfflinePlayer player;
    public OfflinePlayer getPlayer() {
        return player;
    }

    protected int cache = 6;

    private int rank;
    private int highestRank;

    public long getPlaytime() {
        return Playtime.getPlaytime(player);
    }
    public double getExperience() {
        return config.getDouble("Experience", 0);
    }

    public @NotNull Rank getRank() {
        Rank rank = plugin.rankManager.getRank(this.rank);
        if (rank == null) {
            this.rank = 1;
            // Todo - CRITICAL Message
            messenger.Warning("Something is went wrong... Unable to get the rank of player!", String.format("Player= §6%s(%s)", player.getName(), player.getUniqueId()), String.format("Rank Number= §6%d", this.rank), "Their rank is set to 1 to prevent further issues!");
            return plugin.rankManager.getRank(1);
        }
        return rank;
    }
    public int getRankID() {
        return rank;
    }

    public @NotNull Rank getHighestRank() {
        Rank highest_rank = plugin.rankManager.getRank(this.highestRank);
        if (highest_rank == null) {
            Rank rank = getRank();
            this.highestRank = rank.id;
            // Todo - CRITICAL Message
            messenger.Warning("Something is went wrong... Unable to get the highest rank of player!", String.format("Player= §6%s(%s)", player.getName(), player.getUniqueId()), String.format("Rank Number= §6%d", this.highestRank), "Their highest rank is set to current one to prevent further issues!");
            return rank;
        }
        return highest_rank;
    }
    public int getHighestRankID() {
        return highestRank;
    }

    public void setHighestRank(int highestRank) {
        this.highestRank = highestRank;
    }

    public Configuration getConfig() {
        return config;
    }

    public double getScore() {
        return system.getScore(this);
    }

    public User(OfflinePlayer player) {
        this.player = player;

        config = new Configuration(plugin, String.format("Users/%s.yml", player.getUniqueId()), "Examples/user.yml", (string -> string.replace("NAME", String.valueOf(player.getName()))));
        config.loadConfig();

        this.rank = config.getInt("Rank", -1);
        this.highestRank = config.getInt("HighestRank", -1);

        if (rank < 1) setRank(1);
    }

    public void login() {
        messenger.Debug("Users", "Player login has been detected", "Player= §6" + player.getName(), "Rank= §6" + getRank().getTranslatedName(player));
    }

    public void logout() {
        messenger.Debug("Users", "Player logout has been detected", "Player= §6" + player.getName(), "Rank= §6" + getRank().getTranslatedName(player));
        save();
    }

    public void save() {
        config.set("Name", player.getName());
        config.set("Rank", getRankID());
        config.set("HighestRank", getHighestRankID());
        config.set("Experience", getExperience());
        config.saveConfig();
    }

    public void removeCache() {
        this.cache = 0;
        UserManager.cleanCache();
    }

    public void setRank(int rank) {
        this.setRank(plugin.rankManager.getRank(rank));
    }
    public void setRank(@NotNull Rank rank) {
        // For the first login!
        if (rank.id == 1 && getRankID() < 1) {
            this.changePermissionGroup(null, rank.group);
            this.highestRank = 1;
            this.rank = 1;
            return;
        }

        Rank current = getRank();
        double cost = system.getCost(this, rank);
        RankUpEvent event = new RankUpEvent(this, current, rank, cost);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            messenger.Debug("Ranks", "Ranking event has been canceled", String.format("Canceller= §6%s", event.getCanceller() == null ? "UNKNOWN": event.getCanceller().getName()));
            return;
        }

        RankingSystem.PaymentResponse response = system.payCost(this, event.getCost());
        if (!response.isSuccess()) {
            messenger.Debug("Ranks", "Ranking failed due unsuccessful payment", String.format("Message= §6%s", response.getError()));
            return;
        }

        for (Rank temp:plugin.rankManager.getRankingOrder(current, rank)) {
            if (highestRank < temp.id) Bukkit.getScheduler().runTaskLater(plugin, () -> rank.runCommands(this), 10);

            this.changePermissionGroup(getRank().group, temp.group);
            this.highestRank = Math.max(highestRank, temp.id);
            this.rank = temp.id;
        }
    }

    private void changePermissionGroup(String from, String to) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (from != null) plugin.vault.removePlayerGroup(player, from);
            if (to != null) plugin.vault.addPlayerGroup(player, to);
        });
    }

    public Rank getBestAvailableRank() {
        Rank temp = getRank();
        while (temp.hasNextRank() && system.isRankAvailable(this, temp.getNext())) temp = temp.getNext();
        return temp;
    }

    public void setExperience(double amount) {
        config.set("Experience", amount);
    }
}

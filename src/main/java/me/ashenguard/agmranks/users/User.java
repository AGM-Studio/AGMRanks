package me.ashenguard.agmranks.users;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.systems.RankingSystem;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.lib.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

    private final AGMRanks plugin = AGMRanks.getInstance();
    private final Messenger messenger = AGMRanks.getMessenger();
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
        return PlaytimeManager.getPlaytime(player);
    }
    public double getExperience() {
        return config.getDouble("Experience", 0);
    }

    public Rank getRank() {
        return plugin.rankManager.getRank(rank);
    }

    public Rank getHighestRank() {
        return plugin.rankManager.getRank(highestRank);
    }
    public void setHighestRank(int highestRank) {
        this.highestRank = highestRank;
        config.set("HighestRank", this.highestRank);
        config.saveConfig();
    }

    public Configuration getConfig() {
        return config;
    }

    public double getScore() {
        return system.getScore(this);
    }

    public User(OfflinePlayer player) {
        this.player = player;

        config = new Configuration(plugin, String.format("Users/%s.yml", player.getUniqueId()), "Examples/user.yml", (string -> string.replace("NAME", player.getName()).replace("JOIN", String.format("\"%s\"", DATE_FORMAT.format(new Date())))));
        config.loadConfig();

        this.rank = config.getInt("Rank", -1);
        this.highestRank = config.getInt("HighestRank", -1);

        if (rank < 1) setRank(plugin.rankManager.getRank(1));
    }

    public void login() {
        messenger.Debug("Users", "Player login has been detected", "Player= §6" + player.getName(), "Rank= §6" + getRank().getTranslatedName(player));
    }

    public void logout() {
        messenger.Debug("Users", "Player logout has been detected", "Player= §6" + player.getName(), "Rank= §6" + getRank().getTranslatedName(player));
    }

    public void setRank(Rank rank) {
        if (!system.isRankAvailable(this, rank) && this.rank > 0) return;

        Rank current = getRank();
        if (current == null || rank.isHigherThan(current)) {
            Rank temp = current == null ? plugin.rankManager.getRank(1) : current.getNext();
            while (temp.hasNextRank() && !temp.isHigherThan(rank)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> rank.runCommands(this), 20);
                temp = temp.getNext();
            }
        }

        double cost = system.getCost(this, rank);
        system.payCost(this, cost);

        this.changePermissionGroup(rank);
        this.rank = rank.id;

        config.set("Rank", rank.id);
        config.saveConfig();
    }

    private void changePermissionGroup(Rank rank) {
        plugin.vault.addPlayerGroup(player, rank.group);
        if (getRank() != null) plugin.vault.removePlayerGroup(player, getRank().group);
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

package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.systems.RankingSystem;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUI;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.utils.encoding.Alphabetic;
import me.ashenguard.api.utils.encoding.Ordinal;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class Rank {
    private final AGMRanks plugin = AGMRanks.getInstance();
    private final RankManager rankManager = AGMRanks.getRankManager();
    private final RankingSystem rankingSystem = rankManager.rankingSystem;

    public final int id;
    public final String group;
    public final String name;

    public final double cost;

    private final Configuration config;

    public Rank(int id) {
        this.id = id;

        Configuration config = null;
        File template = new File(AGMRanks.getInstance().getDataFolder(), "rank_template.yml");
        try {
            InputStream stream = new FileInputStream(template);
            config = new Configuration(plugin, String.format("Ranks/%s.yml", Ordinal.to(id)), stream, (string -> string.replace("NNN", String.valueOf(id)).replace("ONN", Ordinal.to(id)).replace("ANN", Alphabetic.to(id))));
        } catch (FileNotFoundException ignored) { }
        if (config == null){
            config = new Configuration(plugin, String.format("Ranks/%s.yml", Ordinal.to(id)), "Examples/rank.yml", (string -> string.replace("NNN", String.valueOf(id)).replace("ONN", Ordinal.to(id)).replace("ANN", Alphabetic.to(id))));
        }
        this.config = config;
        this.config.loadConfig();

        this.group = this.config.getString("Group", "default");
        this.name = this.config.getString("Name", "&cNOT_FOUND");
        this.cost = this.config.getDouble("Cost", 0);
    }

    // ---- Getters & Checks ---- //
    public boolean hasNextRank() {
        return getNext() != null;
    }
    public Rank getNext() {
        return rankManager.getRank(id + 1);
    }

    public boolean hasPreviousRank() {
        return getPrevious() != null;
    }
    public Rank getPrevious() {
        return rankManager.getRank(id - 1);
    }

    public String getTranslatedName() {
        return PHManager.translate(name);
    }
    public String getTranslatedName(OfflinePlayer player) {
        return PHManager.translate(player, name);
    }

    public boolean isHigherThan(Rank rank) {
        if (rank == null) return true;
        return this.id > rank.id;
    }
    public boolean isLowerThan(Rank rank) {
        if (rank == null) return false;
        return this.id < rank.id;
    }

    public RankType getType(OfflinePlayer player) {
        return getType(UserManager.getUser(player));
    }
    public RankType getType(User user) {
        Rank current = user.getRank();
        if (current.isHigherThan(this) || current.equals(this)) return RankType.Active;
        if (rankingSystem.isRankAvailable(user, this)) return RankType.Available;
        return RankType.Unavailable;
    }

    public ItemStack getItem(OfflinePlayer player) {
        return getItem(player, getType(player));
    }
    public ItemStack getItem(OfflinePlayer player, RankType rankType) {
        ConfigurationSection section = config.getConfigurationSection("Items." + rankType.name());
        if (section == null) return GUI.NULL.clone();
        return GUI.getItemStack(player, section);
    }

    public List<ItemStack> getRewards(OfflinePlayer player) {
        List<ItemStack> rewards = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("Rewards");
        if (section == null) return rewards;
        List<String> keys = section.getKeys(false).stream().sorted().collect(Collectors.toList());
        for (String key: keys) {
            ConfigurationSection item = section.getConfigurationSection(key);
            if (item != null) rewards.add(GUI.getItemStack(player, item));
        }
        return rewards;
    }

    public void runCommands(User user) {
        if (!user.player.isOnline()) return;
        Player player = (Player) user.player;
        List<String> commands = PHManager.translate(player, config.getStringList("Commands"));
        for (String command:commands) {
            String[] args = command.split("\\s+");
            if (args[0].startsWith("perm:")) {
                if (!player.hasPermission(args[0].substring(6))) continue;
                command = command.replace(args[0] + " ", "");
            }

            args = command.split("\\s+");
            if (args[0].equalsIgnoreCase("sudo"))
                Bukkit.dispatchCommand(player, command.substring(5));
            else
                Bukkit.dispatchCommand(plugin.commandSender, command);
        }
    }

    public String getOrdinal() {
        return Ordinal.to(id);
    }

    public enum RankType {
        Active,
        Available,
        Unavailable
    }
}

package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.commands.CommandExecutor;
import me.ashenguard.agmranks.ranks.requirements.LivetimeRequirement;
import me.ashenguard.agmranks.ranks.requirements.MoneyRequirement;
import me.ashenguard.agmranks.ranks.requirements.PlaytimeRequirement;
import me.ashenguard.agmranks.ranks.requirements.ScoreRequirement;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Alphabetic;
import me.ashenguard.api.utils.encoding.Ordinal;
import me.ashenguard.exceptions.RankLoadingException;
import net.luckperms.api.track.Track;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Rank {
    private final int id;
    private final String name;
    private final RankingInstance instance;
    private final List<Requirement> requirements = new ArrayList<>();

    private final PlaceholderItemStack activeItem;
    private final PlaceholderItemStack availableItem;
    private final PlaceholderItemStack unavailableItem;

    private final List<PlaceholderItemStack> rewards = new ArrayList<>();
    private final List<CommandExecutor> commands = new ArrayList<>();

    private final List<Placeholder> placeholders = new ArrayList<>();

    public Rank(RankingInstance instance, int id, Track track) {
        List<Placeholder> placeholders = Collections.singletonList(
                new Placeholder("NAME", (p, s) -> String.format("Rank %s", Alphabetic.to(id)))
        );
        Configuration config = new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/%s.yml", instance.getFilename(), Ordinal.to(id)),
                AGMRanks.getInstance().getResource("templates/rank-template.yml"),
                string -> {
                    for (Placeholder placeholder: placeholders) string = placeholder.apply(string, null);
                    return string;
                }
        );

        if (track != null && track.getGroups().size() < id) throw new RankLoadingException("Track do not accept anymore groups");

        this.id = id;
        this.instance = instance;
        this.name = config.getString("Name", "UNNAMED");

        this.activeItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("ActiveItem"));
        this.availableItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("AvailableItem"));
        this.unavailableItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("UnavailableItem"));

        List<?> rewards = config.getList("Rewards", Collections.EMPTY_LIST);
        for (Object reward:rewards) AGMRanks.getMessenger().info(reward.getClass().getSimpleName());

        config.getStringList("Commands").stream().map(CommandExecutor::new).forEach(commands::add);

        double cost = config.getDouble(Arrays.asList("Cost", "Money"), 0);
        if (cost > 0) requirements.add(new MoneyRequirement(cost));

        long playtime = config.getLong("Playtime", 0);
        if (playtime > 0) requirements.add(new PlaytimeRequirement(playtime));

        long livetime = config.getLong("Livetime", 0);
        if (livetime > 0) requirements.add(new LivetimeRequirement(this, livetime));

        long score = config.getLong("Score", 0);
        if (score > 0) requirements.add(new ScoreRequirement(this, score));
    }

    public boolean areRequirementsMet(Player player) {
        for (Requirement requirement:requirements)
            if (!requirement.isMet(player)) return false;
        return true;
    }

    public ItemStack getActiveItem(Player player) {
        return activeItem.getItem(player, placeholders);
    }
    public ItemStack getAvailableItem(Player player) {
        return availableItem.getItem(player, placeholders);
    }
    public ItemStack getUnavailableItem(Player player) {
        return unavailableItem.getItem(player, placeholders);
    }
    public List<PlaceholderItemStack> getRewards() {
        return rewards;
    }

    public RankingInstance getRankingInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void executeCommands(Player player) {
        instance.executeCommands(player);
        commands.forEach(command -> command.execute(player));
    }
}
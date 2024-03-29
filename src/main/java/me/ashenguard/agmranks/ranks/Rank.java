package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.commands.CommandReward;
import me.ashenguard.agmranks.player.PlayerBatchInfo;
import me.ashenguard.agmranks.player.RankedPlayer;
import me.ashenguard.agmranks.ranks.requirements.LivetimeRequirement;
import me.ashenguard.agmranks.ranks.requirements.MoneyRequirement;
import me.ashenguard.agmranks.ranks.requirements.PlaytimeRequirement;
import me.ashenguard.agmranks.ranks.requirements.ScoreRequirement;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import me.ashenguard.api.utils.encoding.Ordinal;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Rank {
    private final int id;
    private final String name;
    private final RankBatch batch;

    private final MoneyRequirement moneyRequirement;
    private final List<Requirement> requirements = new ArrayList<>();

    private final PlaceholderItemStack activeItem;
    private final PlaceholderItemStack availableItem;
    private final PlaceholderItemStack unavailableItem;

    private final List<PlaceholderItemStack> rewards = new ArrayList<>();
    private final List<CommandReward> commands = new ArrayList<>();

    private final List<Placeholder> placeholders = new ArrayList<>();

    public Rank(RankBatch batch, int id) {
        this.id = id;
        this.batch = batch;

        Configuration config = new Configuration(AGMRanks.getInstance(), String.format("ranks/%s/%s.yml", batch.getID(), Ordinal.to(id + 1)));

        this.name = config.getString("Name", "UNNAMED");
        this.activeItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("ActiveItem"));
        this.availableItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("AvailableItem"));
        this.unavailableItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("UnavailableItem"));

        List<?> rewards = config.getList("Rewards", Collections.EMPTY_LIST);
        for (Object reward:rewards) AGMRanks.getMessenger().info(reward.getClass().getSimpleName());

        config.getStringList("Commands").forEach(cmd -> commands.add(new CommandReward(batch, cmd)));

        // Allowing auto rank up give the first rank to all players
        if (id == 0) moneyRequirement = null;
        else {
            double cost = config.getDouble(Arrays.asList("Cost", "Money"), 0);
            moneyRequirement = cost > 0 ? new MoneyRequirement(this, cost) : null;

            long playtime = config.getLong("Playtime", 0);
            if (playtime > 0) requirements.add(new PlaytimeRequirement(this, playtime));

            long livetime = config.getLong("Livetime", 0);
            if (livetime > 0) requirements.add(new LivetimeRequirement(this, livetime));

            long score = config.getLong("Score", 0);
            if (score > 0) requirements.add(new ScoreRequirement(this, score));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Requirement> T getRequirement(Class<T> cls) {
        if (MoneyRequirement.class == cls) return (T) moneyRequirement;
        for (Requirement requirement: requirements)
            if (requirement.getClass() == cls) return (T) requirement;

        return null;
    }

    public void rankup(Player player) {
        PlayerBatchInfo info = RankedPlayer.get(player).getBatchInfo(batch);

        Rank previous = getPrevious();
        if (previous != null && info.getRank() != previous) getPrevious().rankup(player);
        if (previous != null && info.getRank() != getPrevious()) return;
        if (this.areRequirementsMet(player, true)) {
            moneyRequirement.effect(player);
            requirements.forEach(requirement -> requirement.effect(player));

            executeCommands(player);
            info.setRank(this);
            info.setHighRank(this, false);
        }
    }

    public boolean areRequirementsMet(Player player) {
        for (Requirement requirement:requirements)
            if (!requirement.isMet(player)) return false;
        return true;
    }
    public boolean areRequirementsMet(Player player, boolean costCovered) {
        if (costCovered && hasCost())
            return moneyRequirement.isMet(player) && areRequirementsMet(player);
        return areRequirementsMet(player);
    }
    public boolean hasCost() {
        return moneyRequirement != null;
    }

    public PlaceholderItemStack getActiveItem() {
        return activeItem;
    }
    public PlaceholderItemStack getAvailableItem() {
        return availableItem;
    }
    public PlaceholderItemStack getUnavailableItem() {
        return unavailableItem;
    }
    public List<PlaceholderItemStack> getRewards() {
        return rewards;
    }

    public RankBatch getBatch() {
        return batch;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public void executeCommands(Player player) {
        if (RankedPlayer.get(player).getBatchInfo(batch).getHighRank().getID() >= this.getID()) return;

        batch.executeCommands(player);
        commands.forEach(command -> command.execute(player));
    }

    public Rank getNext() {
        return batch.getRank(id + 1);
    }
    public Rank getPrevious() {
        return batch.getRank(id - 1);
    }
}
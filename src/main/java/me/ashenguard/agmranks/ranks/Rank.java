package me.ashenguard.agmranks.ranks;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.api.LuckPermsAPI;
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
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
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
    private final List<CommandExecutor> commands = new ArrayList<>();

    private final List<Placeholder> placeholders = new ArrayList<>();

    public Rank(RankBatch batch, int id, Track track) {
        List<Placeholder> placeholders = Collections.singletonList(
                new Placeholder("NAME", (p, s) -> String.format("Rank %s", Alphabetic.to(id)))
        );
        Configuration config = new Configuration(
                AGMRanks.getInstance(),
                String.format("ranks/%s/%s.yml", batch.getID(), Ordinal.to(id)),
                AGMRanks.getInstance().getResource("templates/rank-template.yml"),
                string -> {
                    for (Placeholder placeholder: placeholders) string = placeholder.apply(string, null);
                    return string;
                }
        );

        if (track != null && track.getGroups().size() < id) throw new RankLoadingException("Track do not accept anymore groups");

        this.id = id;
        this.batch = batch;
        this.name = config.getString("Name", "UNNAMED");

        this.activeItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("ActiveItem"));
        this.availableItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("AvailableItem"));
        this.unavailableItem = PlaceholderItemStack.fromSection(config.getConfigurationSection("UnavailableItem"));

        List<?> rewards = config.getList("Rewards", Collections.EMPTY_LIST);
        for (Object reward:rewards) AGMRanks.getMessenger().info(reward.getClass().getSimpleName());

        config.getStringList("Commands").stream().map(CommandExecutor::new).forEach(commands::add);

        // Allowing auto rank up give the first rank to all players
        if (id == 0) moneyRequirement = null;
        else {
            double cost = config.getDouble(Arrays.asList("Cost", "Money"), 0);
            moneyRequirement = cost > 0 ? new MoneyRequirement(cost) : null;

            long playtime = config.getLong("Playtime", 0);
            if (playtime > 0) requirements.add(new PlaytimeRequirement(playtime));

            long livetime = config.getLong("Livetime", 0);
            if (livetime > 0) requirements.add(new LivetimeRequirement(this, livetime));

            long score = config.getLong("Score", 0);
            if (score > 0) requirements.add(new ScoreRequirement(this, score));
        }
    }

    public void rankup(Player player) {
        if (batch.getPlayerRank(player) != getPrevious()) return;
        if (this.areRequirementsMet(player, true)) {
            moneyRequirement.effect(player);
            requirements.forEach(requirement -> requirement.effect(player));

            executeCommands(player);
            updatePermissionGroup(player);
            batch.setPlayerRank(player, this);
            batch.setPlayerHighRank(player, this, false);
        }
    }

    public void updatePermissionGroup(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(AGMRanks.getInstance(), () -> {
            List<String> groups = this.batch.getTrack().getGroups();
            User user = LuckPermsAPI.getUser(player.getUniqueId());
            for (int i = 0; i < groups.size(); i++) {
                Node node = Node.builder(String.format("group.%s", groups.get(i))).build();
                if (i == this.getID()) user.data().add(node);
                else user.data().remove(node);
            }
            LuckPermsAPI.saveUser(user);
        });
    }

    public boolean areRequirementsMet(Player player) {
        for (Requirement requirement:requirements)
            if (!requirement.isMet(player)) return false;
        return true;
    }
    public boolean areRequirementsMet(Player player, boolean costCovered) {
        if (costCovered && hasCost()) return moneyRequirement.isMet(player) && areRequirementsMet(player);
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
        if (batch.getPlayerHighRank(player).getID() >= this.getID()) return;

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
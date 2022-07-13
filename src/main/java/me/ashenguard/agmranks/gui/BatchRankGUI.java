package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.function.Function;

public class BatchRankGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMRanks.getInstance(), "GUI/batch.yml", true);
    private static final List<Integer> rewardSlots = config.getIntegerList("EmptySlots");
    private static final List<Integer> rankSlots = config.getIntegerList("RankIconSlots");

    private final Rank rank;

    protected static void showBatchRank(Player player, Rank rank) {
        new BatchRankGUI(player, rank).show();
    }

    protected BatchRankGUI(Player player, Rank rank) {
        super(player, config);
        this.rank = rank;

        placeholders.add(new Placeholder("rank_name", (p, s) -> this.rank.getName()));

        List<PlaceholderItemStack> rewards = rank.getRewards();
        for (int i = 0; i < rewardSlots.size() && i < rewards.size(); i++) {
            int slot = rewardSlots.get(i);
            setSlot(slot, new GUIInventorySlot(slot).addItem(rewards.get(i)));
        }

        for (int slot: rankSlots) {
            setSlot(slot, new GUIInventorySlot(slot).addItem(rank.getPlayerItem(player)));
        }
    }

    @Override
    protected Function<InventoryClickEvent, Boolean> getSlotActionByKey(String key) {
        return switch (key) {
            case "Menu", "Cancel" -> (event) -> {
                BatchGUI.showBatch(player, this.rank.getBatch());
                return true;
            };
            case "Accept" -> (event) -> {
                if (this.rank.areRequirementsMet(player, true)); // Todo rank up
                return true;
            };
            default -> null;
        };
    }
}

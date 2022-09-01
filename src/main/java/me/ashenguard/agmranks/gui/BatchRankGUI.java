package me.ashenguard.agmranks.gui;


import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.Messages;
import me.ashenguard.agmranks.player.PlayerBatchInfo;
import me.ashenguard.agmranks.player.RankedPlayer;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankStatus;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.gui.GUIManager;
import me.ashenguard.api.gui.GUIPlayerInventory;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

public class BatchRankGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMRanks.getInstance(), "GUI/batch-rank.yml", true);

    private static BatchRankGUI instance;
    public static BatchRankGUI getInstance() {
        if (instance == null) instance = new BatchRankGUI();
        return instance;
    }

    public static void show(Player player, Rank rank) {
        GUIManager.open(getInstance(), player, rank);
    }

    private final List<Integer> rewardSlots;
    private final List<Integer> rankSlots;

    protected BatchRankGUI() {
        super(config.getInt("Size"), config.getString("Title"));

        rewardSlots = config.getIntegerList("EmptySlots");
        rankSlots = config.getIntegerList("RankIconSlots");
    }

    @Override protected GUIInventorySlot.Action getSlotActionByKey(String key) {
        return switch (key) {
            case "Menu" -> GUIInventorySlot.Action.fromConsumer((inv, event) -> {
                RankInventory inventory = (RankInventory) inv;
                BatchGUI.show((Player) event.getWhoClicked(), inventory.rank.getBatch());
            });
            case "Cancel" -> GUIInventorySlot.Action.fromConsumer((inv, event, alt) -> {
                if (alt) return;
                RankInventory inventory = (RankInventory) inv;
                BatchGUI.show((Player) event.getWhoClicked(), inventory.rank.getBatch());
            });
            case "Accept" -> GUIInventorySlot.Action.fromConsumer((inv, event, alt) -> {
                if (alt) return;
                Player player = (Player) event.getWhoClicked();
                RankInventory inventory = (RankInventory) inv;
                inventory.rank.rankup(player);
                AGMRanks.getMessenger().response(player, Messages.RankedUp);
                BatchGUI.show(player, inventory.rank.getBatch());
            });
            default -> null;
        };
    }

    @Override
    protected GUIInventorySlot.Check getSlotCheckByKey(String key) {
        return switch (key) {
            case "Accept", "Cancel" -> inv -> {
                RankInventory inventory = (RankInventory) inv;
                PlayerBatchInfo info = RankedPlayer.get(inv.getPlayer()).getBatchInfoOf(inventory.rank);
                return RankStatus.Affordable == info.getRankStatus(inventory.rank);
            };
            default -> null;
        };
    }

    public class RankInventory extends GUIPlayerInventory {
        private final Rank rank;

        public RankInventory(GUIInventory gui, Player player, Object... extras) {
            super(gui, player, extras);

            this.rank = (Rank) extras[0];

            List<PlaceholderItemStack> rewards = rank.getRewards();
            for (int i = 0; i < rewardSlots.size() && i < rewards.size(); i++) {
                int slot = rewardSlots.get(i);
                slots.put(slot, new GUIInventorySlot("Reward", slot).addItem(rewards.get(i)));
            }

            PlayerBatchInfo info = RankedPlayer.get(player).getBatchInfo(rank.getBatch());
            for (int slot: rankSlots) slots.put(slot, new GUIInventorySlot("Rank", slot).addItem(info.getRankItem(rank)));

            placeholders.add(new Placeholder("rank_name", (p, s) -> this.rank.getName()));
        }
    }
}
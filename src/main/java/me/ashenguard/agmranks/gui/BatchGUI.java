package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.player.PlayerBatchInfo;
import me.ashenguard.agmranks.player.RankedPlayer;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.gui.GUIUpdater;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BatchGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMRanks.getInstance(), "GUI/batch.yml", true);
    private static final List<Integer> slots = config.getIntegerList("EmptySlots");

    private final RankBatch batch;
    private final PlayerBatchInfo info;
    private Rank center;

    public static void show(Player player, RankBatch batch) {
        new BatchGUI(player, batch).show();
    }

    protected BatchGUI(Player player, RankBatch batch) {
        super(player, config);
        this.batch = batch;
        this.info = RankedPlayer.get(player).getBatchInfo(batch);
        this.center = info.getRank();

        placeholders.add(new Placeholder("batch_name", (p, s) -> this.batch.getName()));
        placeholders.add(new Placeholder("count_ranks", (p, s) -> String.valueOf(this.batch.getRanks().size())));
        placeholders.add(new Placeholder("current_rank", (p, s) -> this.info.getRank().getName()));

        update(this.info.getRank());
    }

    final Map<Integer, GUIInventorySlot> defaults = new HashMap<>();
    protected void update(Rank center) {
        if (center == null) return;

        this.center = center;
        int start = center.getID() - ((slots.size() + 1) / 2 - 1);
        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);
            defaults.putIfAbsent(slot, getSlot(slot));
            Rank rank = this.batch.getRank(i + start);
            if (rank == null) setSlot(slot, defaults.get(slot));
            else {
                GUIInventorySlot inventorySlot = new GUIInventorySlot(slot);
                inventorySlot.addItem(info.getRankItem(rank));
                inventorySlot.setAction((Consumer<InventoryClickEvent>) event -> BatchRankGUI.show(player, rank));
                setSlot(slot, inventorySlot);
            }
        }
    }

    @Override
    protected Function<InventoryClickEvent, Boolean> getSlotActionByKey(String key) {
        return switch (key) {
            case "Menu" -> (event) -> {
                BatchMenuGUI.show(player);
                return true;
            };
            case "Next" -> (event) -> {
                update(center.getNext());
                GUIUpdater.update(this);
                return true;
            };
            case "Previous" -> (event) -> {
                update(center.getPrevious());
                GUIUpdater.update(this);
                return true;
            };
            default -> null;
        };
    }
}

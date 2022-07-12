package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public class BatchMenuGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMRanks.getInstance(), "GUI/batch-menu.yml", true);

    protected static void showBatchMenu(Player player) {
        new BatchMenuGUI(player).show();
    }

    public BatchMenuGUI(Player player) {
        super(player, config);

        AtomicInteger count = new AtomicInteger();
        List<Integer> emptySlots = config.getIntegerList("EmptySlots");
        List<RankBatch> batches = RankManager.getInstance().getRankingBatches().values().stream().filter(batch -> {
            if (!batch.hasPermission(player)) return false;
            count.getAndIncrement();
            return count.get() <= emptySlots.size();
        }).sorted(Comparator.comparingInt(RankBatch::getSortingKey).thenComparing(RankBatch::getID)).toList();

        int start = (emptySlots.size() - batches.size()) / 2;
        int end = (emptySlots.size() - batches.size()) / 2 + batches.size();
        // If the size is even (and slots are odd) removing the middle slot will center it
        if (batches.size() % 2 == 0 && emptySlots.size() % 2 == 1) emptySlots.remove((emptySlots.size() + 1) / 2);
        List<Integer> slots = emptySlots.subList(Math.max(0, start), Math.min(emptySlots.size(), end));

        for (int i = 0; i < slots.size(); i++) {
            int index = emptySlots.get(i);
            RankBatch batch = batches.get(i);
            GUIInventorySlot slot = new GUIInventorySlot(index);
            slot.addItem(batch.getIcon()).setAction((Consumer<InventoryClickEvent>) event -> BatchGUI.showBatch(player, batch));
            setSlot(index, slot);
        }
    }

    @Override
    protected Function<InventoryClickEvent, Boolean> getSlotActionByKey(String key) {
        return null;
    }
}

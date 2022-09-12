package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.gui.GUIManager;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchMenuGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMRanks.getInstance(), "GUI/batch-menu.yml", true);

    private static BatchMenuGUI instance;
    public static BatchMenuGUI getInstance() {
        if (instance == null) instance = new BatchMenuGUI();
        return instance;
    }

    public static void show(Player player) {
        GUIManager.open(getInstance(), player);
    }

    private final List<Integer> emptySlots;

    public BatchMenuGUI() {
        super(config.getInt("Size"), config.getString("Title"));

        this.emptySlots = config.getIntegerList("EmptySlots");
    }

    @Override public Map<Integer, GUIInventorySlot> getSlotMapFor(Player player, Object... extras) {
        AtomicInteger count = new AtomicInteger();
        List<RankBatch> batches = AGMRanks.getBatches().stream().filter(batch -> {
            if (!batch.hasPermission(player)) return false;
            count.getAndIncrement();
            return count.get() <= emptySlots.size();
        }).sorted(Comparator.comparingInt(RankBatch::getSortingKey).thenComparing(RankBatch::getID)).toList();

        int start = (emptySlots.size() - batches.size()) / 2;
        int end = (emptySlots.size() - batches.size()) / 2 + batches.size();
        // If the size is even (and slots are odd) removing the middle slot will center it
        if (batches.size() % 2 == 0 && emptySlots.size() % 2 == 1) emptySlots.remove((emptySlots.size() + 1) / 2);
        List<Integer> slots = emptySlots.subList(Math.max(0, start), Math.min(emptySlots.size(), end));

        HashMap<Integer, GUIInventorySlot> map = new HashMap<>(SLOT_MAP);
        for (int i = 0; i < slots.size(); i++) {
            int index = slots.get(i);
            RankBatch batch = batches.get(i);
            GUIInventorySlot slot = new GUIInventorySlot("Batch", index).addItem(batch.getIcon());
            slot.setAction(GUIInventorySlot.Action.fromConsumer((event) -> BatchGUI.show(player, batch)));
            map.put(index, slot);
        }

        return map;
    }

    @Override
    protected GUIInventorySlot.Action getSlotActionByKey(String key) {
        return null;
    }
}

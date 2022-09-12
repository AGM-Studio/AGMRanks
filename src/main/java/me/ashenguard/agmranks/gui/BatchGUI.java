package me.ashenguard.agmranks.gui;


import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.player.PlayerBatchInfo;
import me.ashenguard.agmranks.player.RankedPlayer;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.api.Configuration;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.gui.GUIInventorySlot;
import me.ashenguard.api.gui.GUIManager;
import me.ashenguard.api.gui.GUIPlayerInventory;
import me.ashenguard.api.placeholder.Placeholder;
import org.bukkit.entity.Player;

import java.util.List;

public class BatchGUI extends GUIInventory {
    private static final Configuration config = new Configuration(AGMRanks.getInstance(), "GUI/batch.yml", true);

    private static BatchGUI instance;
    public static BatchGUI getInstance() {
        if (instance == null) instance = new BatchGUI();
        return instance;
    }

    public static void show(Player player, RankBatch batch) {
        GUIManager.open(getInstance(), player, batch);
    }

    private final List<Integer> emptySlots;
    protected BatchGUI() {
        super(config.getInt("Size"), config.getString("Title"));

        this.emptySlots = config.getIntegerList("EmptySlots");
    }

    @Override protected GUIInventorySlot.Action getSlotActionByKey(String key) {
        return switch (key) {
            case "Menu" -> GUIInventorySlot.Action.fromConsumer(event -> BatchMenuGUI.show((Player) event.getWhoClicked()));
            case "Next" -> GUIInventorySlot.Action.fromConsumer((inv, event) -> {
                BatchInventory inventory = (BatchInventory) inv;
                inventory.setCenter(inventory.center.getNext());
                GUIManager.update(inventory);
            });
            case "Previous" -> GUIInventorySlot.Action.fromConsumer((inv, event) -> {
                BatchInventory inventory = (BatchInventory) inv;
                inventory.setCenter(inventory.center.getPrevious());
                GUIManager.update(inventory);
            });
            default -> null;
        };
    }

    @Override
    public GUIPlayerInventory getGUIPlayerInventory(Player player, Object... extras) {
        return new BatchInventory(this, player, extras);
    }

    public class BatchInventory extends GUIPlayerInventory {
        private final RankBatch batch;
        private final PlayerBatchInfo info;
        private Rank center = null;

        public BatchInventory(GUIInventory gui, Player player, Object... extras) {
            super(gui, player, extras);

            this.batch = (RankBatch) extras[0];
            this.info = RankedPlayer.get(player).getBatchInfo(batch);

            placeholders.add(new Placeholder("batch_name", (p, s) -> this.batch.getName()));
            placeholders.add(new Placeholder("count_ranks", (p, s) -> String.valueOf(this.batch.getRanks().size())));
            placeholders.add(new Placeholder("current_rank", (p, s) -> this.info.getRank().getName()));

            update();
        }

        public void setCenter(Rank center) {
            if (center == null) return;
            this.center = center;
            update();
        }

        private void update() {
            if (center == null) center = this.info.getRank();

            this.slots.clear();
            this.slots.putAll(getSlotMapFor(getPlayer()));

            int start = center.getID() - ((emptySlots.size() + 1) / 2 - 1);
            for (int i = 0; i < emptySlots.size(); i++) {
                int slot = emptySlots.get(i);
                Rank rank = batch.getRank(i + start);
                if (rank != null) {
                    GUIInventorySlot inventorySlot = new GUIInventorySlot("Rank", slot).addItem(info.getRankItem(rank));
                    inventorySlot.setAction(GUIInventorySlot.Action.fromConsumer(event -> BatchRankGUI.show(getPlayer(), rank)));
                    this.slots.put(slot, inventorySlot);
                }
            }
        }
    }
}

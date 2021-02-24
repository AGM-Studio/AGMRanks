package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.messenger.PHManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RankGUI extends GUIInventory {
    private final Rank rank;
    private final RankUpGUI rankUpGUI;
    private final boolean affordable;

    public RankGUI(Player player, Rank rank, RankUpGUI rankUpGUI) {
        super(AGMRanks.getGUI() , getTitle(player, rank), player, 54, true, true);
        this.rank = rank;
        this.rankUpGUI = rankUpGUI;
        this.affordable = AGMRanks.getRankManager().rankingSystem.isRankAvailable(UserManager.getUser(player), rank);
    }

    @Override
    public void show() {
        rankUpGUI.close();
        super.show();
    }

    @Override
    protected void design() {
        for (int i=0; i<54; i++)
            inventory.setItem(i, GUI.getItemStack(null, Items.RankPage.Border));

        inventory.setItem(4, GUI.getItemStack(player, Items.RankPage.Rewards));

        List<ItemStack> rewards = rank.getRewards(player);
        for (int i=9; i<27; i++) {
            if (i-9 < rewards.size()) inventory.setItem(i, rewards.get(i-9));
        }

        if (affordable) {
            inventory.setItem(38, GUI.getItemStack(player, Items.RankPage.Confirm));
            inventory.setItem(42, GUI.getItemStack(player, Items.RankPage.Cancel));
        } else {
            inventory.setItem(40, GUI.getItemStack(player, Items.RankPage.Cancel));
        }

        inventory.setItem(49, rank.getItem(player));
    }

    @Override
    public void click(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot == 38 && affordable)
            UserManager.getUser(player).setRank(rank);
        if (affordable && slot != 38 && slot != 42) return;
        if (!affordable && slot != 40) return;

        close(); rankUpGUI.show();
    }

    public static String getTitle(OfflinePlayer player, Rank rank) {
        String title = AGMRanks.getGUI().config.getString("GUI.ConfirmTitle", "Confirm ranking to %Rank%");
        title = title.replace("%Rank%", rank.getTranslatedName(player));
        return PHManager.translate(player, title);
    }
}

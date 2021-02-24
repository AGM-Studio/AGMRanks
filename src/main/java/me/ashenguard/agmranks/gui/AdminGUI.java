package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.gui.GUIInventory;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminGUI extends GUIInventory {
    private Rank center;
    private int playerPage;
    private List<OfflinePlayer> players = new ArrayList<>();

    public AdminGUI(Player player) {
        super(AGMRanks.getGUI(), "§5AGMRanks§7 - Admin Panel", player, 54);
        reload(AGMRanks.getRankManager().getRank(1));
    }

    private void setPlayersList(Rank rank) {
        players = UserManager.getOfflineUsers().stream().filter(user -> user.getRank().equals(rank)).map(User::getPlayer).collect(Collectors.toList());
    }

    @Override
    public void click(InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 18) {        // Left
            this.leftRank();
        } else if (slot == 26) { // Right
            this.rightRank();
        } else if (slot == 45) { // Previous
            this.previousPage();
        } else if (slot == 53) { // Next
            this.nextPage();
        }
    }

    @Override
    protected void design() {
        for (int i = 0; i < 9; i++)
            inventory.setItem(i, GUI.getItemStack(null, Items.Admin.TopBorder));

        for (int i = 18; i < 27; i++)
            inventory.setItem(i, GUI.getItemStack(null, Items.Admin.MiddleBorder));

        for (int i = 45; i < 54; i++)
            inventory.setItem(i, GUI.getItemStack(null, Items.Admin.BottomBorder));

        inventory.setItem(18, GUI.getItemStack(player, Items.Admin.LeftButton));
        inventory.setItem(26, GUI.getItemStack(player, Items.Admin.RightButton));

        inventory.setItem(45, GUI.getItemStack(player, Items.Admin.PreviousButton));
        inventory.setItem(53, GUI.getItemStack(player, Items.Admin.NextButton));

        setRankLine();
        setPlayers();
    }

    // -------------------------
    public void leftRank() {
        Rank left = center.getPrevious();
        if (left != null) reload(left);
    }
    public void rightRank() {
        Rank right = center.getNext();
        if (right != null) reload(right);
    }
    public void nextPage() {
        if (players.size() >= 18 * playerPage) playerPage += 1;
        reload(center);
    }
    public void previousPage() {
        if (playerPage > 0) playerPage -= 1;
        reload(center);
    }

    public void reload(Rank center) {
        if (!this.center.equals(center)) {
            playerPage = 0;
            setPlayersList(center != null ? center : this.center);
        }
        if (center != null) this.center = center;
        reload();
    }

    private void setRankLine() {
        RankManager manager = AGMRanks.getRankManager();
        for (int slot = 9; slot < 18; slot++) {
            int index = center.id - 13 + slot;
            Rank temp = manager.getRank(index);
            ItemStack item = temp == null ? new ItemStack(Material.AIR) : temp.getItem(player);
            inventory.setItem(slot, item);
        }

        if (center.getItem(player, Rank.RankType.Unavailable) != null)
            inventory.setItem(21, center.getItem(player, Rank.RankType.Unavailable));
        if (center.getItem(player, Rank.RankType.Active) != null)
            inventory.setItem(22, center.getItem(player, Rank.RankType.Active));
        if (center.getItem(player, Rank.RankType.Available) != null)
            inventory.setItem(23, center.getItem(player, Rank.RankType.Available));
    }
    private void setPlayers() {
        setPlayersList(center);
        for (int i = 27; i < 45; i++) {
            int index = i - 27 + playerPage * 18;
            if (index < 0 || index > players.size() - 1) inventory.setItem(i, new ItemStack(Material.AIR));
            else inventory.setItem(i, GUI.getItemStack(players.get(index), Items.Admin.PlayerHead));
        }
    }
}

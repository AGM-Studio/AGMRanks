package me.ashenguard.agmranks.gui;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankManager;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.gui.GUIInventory;
import me.ashenguard.api.messenger.PHManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class RankUpGUI extends GUIInventory {
    private Rank center;
    private final HashMap<Integer, Rank> rankLine = new HashMap<>();

    public RankUpGUI(Player player, Rank center) {
        super(AGMRanks.getGUI() , getTitle(), player, 27, true, true);
        reload(center);
    }
    public RankUpGUI(Player player) {
        this(player, UserManager.getUser(player).getRank());
    }
    
    @Override
    protected void design() {
        setRankLine();

        for (int i=0; i<9; i++)
            inventory.setItem(i, GUI.getItemStack(null, Items.RankUP.TopBorder));

        for (int i=18; i<27; i++)
            inventory.setItem(i, GUI.getItemStack(null, Items.RankUP.BottomBorder));

        inventory.setItem(4, GUI.getItemStack(player, Items.RankUP.PlayerInfo));
        inventory.setItem(18, GUI.getItemStack(player, Items.RankUP.LeftButton));
        inventory.setItem(22, GUI.getItemStack(player, Items.RankUP.RankUp));
        inventory.setItem(26, GUI.getItemStack(player, Items.RankUP.RightButton));
    }

    @Override
    public void click(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ClickType clickType = event.getClick();
        int slot = event.getSlot();
        
        if (slot == 22) {                   // RankUP
            User user = UserManager.getUser(player);
            Rank current = user.getRank();
            Rank target = switch (clickType) {
                case LEFT -> current.getNext();
                case RIGHT -> user.getBestAvailableRank();
                default -> current;
            };
            new RankGUI(player, target, this).show();
        } else if (slot == 18) {            // Left
            this.left();
        } else if (slot == 26) {            // Right
            this.right();
        } else if (slot < 18 && slot > 8) { // Rank
            Rank selected = this.getSlotRank(slot);
            new RankGUI(player, selected, this).show();
        }

        reload();
    }
    
    // -------------------------
    public void left() {
        Rank left = center.getPrevious();
        if (left != null) center = left;
    }
    public void right() {
        Rank right = center.getNext();
        if (right != null) center = right;
    }

    public void reload(Rank center) {
        if (center != null) this.center = center;
        reload();
    }

    private void setRankLine() {
        RankManager manager = AGMRanks.getRankManager();
        for (int slot=9; slot <18 ; slot++) {
            int index = center.id - 13 + slot;
            Rank temp = manager.getRank(index);
            ItemStack item = temp == null ? new ItemStack(Material.AIR) : temp.getItem(player);
            inventory.setItem(slot, item);
            rankLine.put(slot, temp);
        }
    }
    private Rank getSlotRank(int slot) {
        return rankLine.get(slot%9 + 9);
    }

    @SuppressWarnings("ConstantConditions")
    public static String getTitle() {
        return PHManager.translate(AGMRanks.getGUI().config.getString("RankUP.Title"));
    }
}

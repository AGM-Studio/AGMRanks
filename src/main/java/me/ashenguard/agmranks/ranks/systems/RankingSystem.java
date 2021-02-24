package me.ashenguard.agmranks.ranks.systems;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.Vault;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.messenger.Messenger;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class RankingSystem {
    protected final AGMRanks plugin = AGMRanks.getInstance();
    protected final Messenger messenger = AGMRanks.getMessenger();
    protected final FileConfiguration config = plugin.getConfig();
    protected final Vault vault = AGMRanks.getVault();

    public abstract String getName();
    
    public abstract void onEnable();

    public abstract double getScore(User user);
    
    public boolean isRankAvailable(User user, Rank rank) {
        Rank current = user.getRank();
        if (!vault.isPermissionsEnabled()) return false;
        return (current == null || current.isLowerThan(rank)) && user.getScore() >= getCost(user, rank);
    }

    public double getCost(Rank target) {
        return getCost(plugin.rankManager.getRank(1), target);
    }
    public double getCost(User user, Rank target) {
        return getCost(user.getRank(), target);
    }
    public double getCost(Rank rank, Rank target) {
        double cost = 0;

        if (rank == null || rank.isLowerThan(target)) {
            Rank temp = rank == null ? plugin.rankManager.getRank(1) : rank.getNext();
            while (temp.isLowerThan(target)) {
                cost += temp.cost;
                temp = temp.getNext();
            }
        } else {
            Rank temp = rank;
            while (temp.isHigherThan(target)) {
                cost -= temp.cost;
                temp = temp.getPrevious();
            }
        }

        return cost;
    }
    
    public abstract void payCost(User user, double amount);

    static class AutoRankingTask implements Runnable {
        @Override public void run() {
            for (User user: UserManager.getOnlineUsers()) user.setRank(user.getBestAvailableRank());
            AGMRanks.getMessenger().Debug("Ranks", "Auto-RankUP has been executed successfully");
        }
    }
}



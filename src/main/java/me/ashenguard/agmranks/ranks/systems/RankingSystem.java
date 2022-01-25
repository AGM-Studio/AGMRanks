package me.ashenguard.agmranks.ranks.systems;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.Vault;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.User;
import me.ashenguard.agmranks.users.UserManager;
import me.ashenguard.api.messenger.Messenger;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

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
        return current.isLowerThan(rank) && user.getScore() >= getCost(user, rank);
    }

    public double getCost(Rank target) {
        return getCost(plugin.rankManager.getRank(1), target);
    }
    public double getCost(User user, Rank target) {
        return getCost(user.getRank(), target);
    }
    public double getCost(Rank rank, Rank target) {
        List<Rank> ranks = plugin.rankManager.getRankingOrder(rank, target);
        return ranks.stream().mapToDouble(temp -> temp.cost).sum();
    }
    
    public abstract PaymentResponse payCost(User user, double amount);

    static class AutoRankingTask implements Runnable {
        @Override public void run() {
            for (User user: UserManager.getOnlineUsers()) user.setRank(user.getBestAvailableRank());
            AGMRanks.getMessenger().Debug("Ranks", "Auto-RankUP has been executed successfully");
        }
    }

    public static class PaymentResponse {
        public static final PaymentResponse ALREADY_PAID = new PaymentResponse(0, true, null);

        private final double amount;
        private final boolean success;
        private final String error;

        protected PaymentResponse(double amount, boolean success, String error) {
            this.amount = amount;
            this.success = success;
            this.error = error;
        }

        public static PaymentResponse fromEconomyResponse(EconomyResponse response) {
            return new PaymentResponse(response.amount, response.transactionSuccess(), response.errorMessage);
        }

        public double getAmount() {
            return amount;
        }
        public boolean isSuccess() {
            return success;
        }
        public String getError() {
            return error;
        }
    }
}



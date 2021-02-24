package me.ashenguard.agmranks.ranks.systems;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EconomySystem extends RankingSystem {
    private static final List<String> names = Collections.unmodifiableList(Arrays.asList("Economy", "Money"));
    public static boolean isType(String type) {
        for (String name: names) if (name.equalsIgnoreCase(type)) return true;
        return false;
    }
    
    @Override public String getName() {
        return "Economy";
    }

    private final boolean sellRank;
    private final double payback;

    public EconomySystem() {
        this.sellRank = config.getBoolean("RankingSystem.SellRanksForMoney");
        this.payback = config.getDouble("RankingSystem.PaybackOnSell");
    }

    @Override public void onEnable() {
        messenger.Debug("Ranks", "Economy system has been activated.", "SellRank= §6" + (sellRank ? "Enabled" : "Disabled"), "Payback= §6" + payback + "%");
    }

    @Override public double getScore(User user) {
        return vault.getPlayerBalance(user.player);
    }

    @Override public boolean isRankAvailable(User user, Rank target) {
        Rank current = user.getRank();
        if (!vault.isEconomyEnabled() || !vault.isPermissionsEnabled()) return false;
        if (current == null) return getCost(target) <= user.getScore();
        if (current.isLowerThan(target) && getCost(user, target) <= user.getScore()) return true;
        return current.isHigherThan(target) && sellRank;
    }

    @Override public double getCost(User user, Rank target) {
        if (user.getRank() == null) return getCost(target);
        return user.getRank().isLowerThan(target) ? super.getCost(user, target) : super.getCost(user, target) * payback / 100;
    }

    @Override public void payCost(User user, double amount) {
        if (amount > 0) vault.withdrawPlayerMoney(user.player, amount);
        else if (amount < 0) vault.depositPlayerMoney(user.player, -1 * amount);
    }
}

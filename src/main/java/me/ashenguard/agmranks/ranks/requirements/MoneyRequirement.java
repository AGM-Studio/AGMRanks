package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Requirement;
import org.bukkit.entity.Player;

public class MoneyRequirement extends Requirement {
    private final double amount;

    public MoneyRequirement(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean isMet(Player player) {
        return AGMRanks.getVault().getPlayerBalance(player) >= amount;
    }

    @Override
    public void effect(Player player) {
        AGMRanks.getVault().withdrawPlayerMoney(player, amount);
    }
}

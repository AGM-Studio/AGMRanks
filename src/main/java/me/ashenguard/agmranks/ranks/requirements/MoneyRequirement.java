package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.hooks.VaultAPI;
import org.bukkit.entity.Player;

public class MoneyRequirement extends Requirement {
    private final double amount;

    public MoneyRequirement(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean isMet(Player player) {
        return VaultAPI.getPlayerBalance(player) >= amount;
    }

    @Override
    public void effect(Player player) {
        VaultAPI.withdrawPlayerMoney(player, amount);
    }
}

package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.hooks.VaultAPI;
import org.bukkit.entity.Player;

public class MoneyRequirement extends Requirement {
    private final double amount;

    public MoneyRequirement(Rank rank,  double amount) {
        super(rank);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public boolean isMet(Player player) {
        Rank current = this.rank.getBatch().getPlayerInfo(player).getRank();
        if (current.getID() >= this.rank.getID()) return true;

        double cost = 0;
        Rank temp = this.rank;
        while (temp != current) {
            temp = temp.getPrevious();
            cost += rank.getRequirement(MoneyRequirement.class).getAmount();
        }

        return VaultAPI.getPlayerBalance(player) >= cost;
    }

    @Override
    public void effect(Player player) {
        VaultAPI.withdrawPlayerMoney(player, amount);
    }
}

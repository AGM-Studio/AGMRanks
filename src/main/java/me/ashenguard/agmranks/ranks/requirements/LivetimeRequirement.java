package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.statistics.Livetime;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LivetimeRequirement extends Requirement {
    private final long amount;

    public LivetimeRequirement(Rank rank, long amount) {
        this.amount = amount;

        rank.getBatch().updateWith(PlayerDeathEvent.class);
    }

    @Override
    public boolean isMet(Player player) {
        return Livetime.getLivetime(player) > this.amount;
    }

    @Override
    public void effect(Player player) {}
}

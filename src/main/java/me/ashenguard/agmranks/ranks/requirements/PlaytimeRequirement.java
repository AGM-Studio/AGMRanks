package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.statistics.Playtime;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class PlaytimeRequirement extends Requirement {
    private final long amount;

    public PlaytimeRequirement(long amount) {
        this.amount = amount;
    }

    @Override
    public boolean isMet(Player player) {
        return Playtime.getPlaytime(player, TimeUnit.MINUTES, true) > amount;
    }

    @Override
    public void effect(Player player) {}
}

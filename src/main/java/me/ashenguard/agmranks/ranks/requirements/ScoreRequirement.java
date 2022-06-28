package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.events.agmranks.PlayerScoreUpdateEvent;
import me.ashenguard.lib.statistics.Playtime;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class ScoreRequirement extends Requirement {
    private final long amount;

    public ScoreRequirement(Rank rank, long amount) {
        this.amount = amount;
        
        rank.getBatch().updateWith(PlayerScoreUpdateEvent.class);
    }

    @Override
    public boolean isMet(Player player) {
        return Playtime.getPlaytime(player, TimeUnit.MINUTES, true) > amount;
    }

    @Override
    public void effect(Player player) {}
}

package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.events.agmranks.PlayerScoreUpdateEvent;
import org.bukkit.entity.Player;

public class ScoreRequirement extends Requirement {
    private final long amount;

    public ScoreRequirement(Rank rank, long amount) {
        super(rank);
        this.amount = amount;
        
        rank.getBatch().updateWith(PlayerScoreUpdateEvent.class);
    }

    @Override
    public boolean isMet(Player player) {
        return rank.getBatch().getPlayerInfo(player).getScore() > amount;
    }

    @Override
    public void effect(Player player) {}
}

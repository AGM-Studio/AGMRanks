package me.ashenguard.agmranks.ranks.requirements;

import me.ashenguard.agmranks.player.RankedPlayer;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.agmranks.ranks.Requirement;
import me.ashenguard.lib.events.agmranks.PlayerScoreUpdateEvent;
import org.bukkit.entity.Player;

public class ScoreRequirement extends Requirement {
    private final RankBatch batch;
    private final long amount;

    public ScoreRequirement(Rank rank, long amount) {
        this.batch = rank.getBatch();
        this.amount = amount;
        
        rank.getBatch().updateWith(PlayerScoreUpdateEvent.class);
    }

    @Override
    public boolean isMet(Player player) {
        return RankedPlayer.get(player).getBatchInfo(batch).getScore() > amount;
    }

    @Override
    public void effect(Player player) {}
}

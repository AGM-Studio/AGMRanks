package me.ashenguard.agmranks.player;

import me.ashenguard.agmranks.AGMRanks;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankBatch;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class RankedPlayer {
    public static RankedPlayer get(Player player) {
        return new RankedPlayer(player);
    }

    protected final Player player;
    protected final Map<RankBatch, PlayerBatchInfo> batches = new HashMap<>();

    protected RankedPlayer(Player player) {
        this.player = player;

        for (RankBatch batch: AGMRanks.getBatches())
            if (batch.hasPermission(player)) batches.put(batch, new PlayerBatchInfo(this, batch));
    }

    public PlayerBatchInfo getBatchInfo(RankBatch batch) {
        return batches.get(batch);
    }

    public PlayerBatchInfo getBatchInfoOf(Rank rank) {
        return batches.get(rank.getBatch());
    }
}

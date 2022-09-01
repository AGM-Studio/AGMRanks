package me.ashenguard.agmranks.player;

import me.ashenguard.agmranks.Utils;
import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.ranks.RankBatch;
import me.ashenguard.agmranks.ranks.RankStatus;
import me.ashenguard.api.itemstack.placeholder.PlaceholderItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PlayerBatchInfo {
    private final RankedPlayer ranked;
    private final RankBatch batch;

    private long score;
    private Rank current;
    private Rank highest;
    private int prestige;

    protected PlayerBatchInfo(RankedPlayer ranked, RankBatch batch) {
        this.ranked = ranked;
        this.batch = batch;

        score = Utils.getPlayerData(ranked.player, batch.getScorePDCKey(), PersistentDataType.LONG, (long) 0);
        current = getRankFromKey(batch.getRankPDCKey());
        highest = getRankFromKey(batch.getHighRankPDCKey());
        prestige = Utils.getPlayerData(ranked.player, batch.getPrestigePDCKey(), PersistentDataType.INTEGER, 0);
    }

    private Rank getRankFromKey(String key) {
        int id = Utils.getPlayerData(ranked.player, key, PersistentDataType.INTEGER, -1);
        if (0 > id || id >= batch.getRanks().size()) {
            Utils.setPlayerData(ranked.player, key, PersistentDataType.INTEGER, 0);
            return batch.getRank(0);
        } else return batch.getRank(id);
    }

    public long getScore() {
        return score;
    }
    public void setScore(long score) {
        Utils.setPlayerData(ranked.player, batch.getScorePDCKey(), PersistentDataType.LONG, score);
        this.score = score;
    }

    public Rank getRank() {
        return current;
    }
    public void setRank(int id) {
        setRank(batch.getRank(id));
    }
    public void setRank(Rank rank) {
        if (rank.getBatch() != batch) return;
        Utils.setPlayerData(ranked.player, batch.getRankPDCKey(), PersistentDataType.INTEGER, rank.getID());
        this.current = rank;
    }

    public Rank getHighRank() {
        return highest;
    }
    public void setHighRank(int id, boolean overwrite) {
        setHighRank(batch.getRank(id), overwrite);
    }
    public void setHighRank(Rank rank, boolean overwrite) {
        if (rank.getBatch() != batch) return;
        Utils.setPlayerData(ranked.player, batch.getHighRankPDCKey(), PersistentDataType.INTEGER, rank.getID(), overwrite ? null : integer -> integer != null && integer < rank.getID());
        this.highest = rank;
    }

    public int getPrestige() {
        return prestige;
    }
    public void setPrestige(int prestige) {
        Utils.setPlayerData(ranked.player, batch.getPrestigePDCKey(), PersistentDataType.INTEGER, prestige);
        this.prestige = prestige;
    }

    public RankStatus getRankStatus(Rank rank) {
        if (rank.getID() <= current.getID()) return RankStatus.Purchased;
        if (rank.areRequirementsMet(ranked.player, true)) return RankStatus.Affordable;
        return RankStatus.Unaffordable;
    }

    public PlaceholderItemStack getRankItem(Rank rank) {
        return switch (getRankStatus(rank)) {
            case Affordable -> rank.getActiveItem();
            case Unaffordable -> rank.getUnavailableItem();
            case Purchased -> rank.getAvailableItem();
        };
    }
}

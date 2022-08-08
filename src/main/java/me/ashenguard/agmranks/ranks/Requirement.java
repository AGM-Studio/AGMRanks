package me.ashenguard.agmranks.ranks;

import org.bukkit.entity.Player;

public abstract class Requirement {
    protected final Rank rank;

    protected Requirement(Rank rank) {
        this.rank = rank;
    }

    public abstract boolean isMet(Player player);
    public abstract void effect(Player player);
}

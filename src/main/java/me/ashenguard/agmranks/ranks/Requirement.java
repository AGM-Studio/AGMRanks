package me.ashenguard.agmranks.ranks;

import org.bukkit.entity.Player;

public abstract class Requirement {
    public abstract boolean isMet(Player player);
    public abstract void effect(Player player);
}

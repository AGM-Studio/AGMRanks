package me.ashenguard.lib.events.agmranks;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RankUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final User user;
    private final Rank from;
    private final Rank to;
    private double cost;
    private boolean cancelled = false;

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public RankUpEvent(User user, Rank from, Rank to, double cost) {
        this.user = user;
        this.from = from;
        this.to = to;
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Rank getTo() {
        return to;
    }

    public Rank getFrom() {
        return from;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public User getUser() {
        return user;
    }
}

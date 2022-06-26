package me.ashenguard.lib.events.agmranks;

import me.ashenguard.agmranks.ranks.Rank;
import me.ashenguard.agmranks.users.User;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlayerRankUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final User user;
    private final Rank from;
    private final Rank to;
    private double cost;
    private boolean cancelled = false;
    private JavaPlugin canceller = null;

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerRankUpEvent(User user, Rank from, Rank to, double cost) {
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

    public JavaPlugin getCanceller() {
        return canceller;
    }

    @Override
    public void setCancelled(boolean cancel) {
        setCancelled(cancel, null);
    }

    public void setCancelled(boolean cancel, JavaPlugin plugin) {
        this.cancelled = cancel;
        this.canceller = plugin;
    }

    public User getUser() {
        return user;
    }

}

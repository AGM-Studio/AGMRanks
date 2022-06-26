package me.ashenguard.lib.events.agmranks;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerScoreUpdateEvent extends PlayerEvent {
    public PlayerScoreUpdateEvent(@NotNull Player who) {
        super(who);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return null;
    }
}

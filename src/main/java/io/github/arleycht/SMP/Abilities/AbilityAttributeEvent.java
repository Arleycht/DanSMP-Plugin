package io.github.arleycht.SMP.Abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityAttributeEvent extends Event {
    private final Player player;

    public AbilityAttributeEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    // Boiler plate

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}

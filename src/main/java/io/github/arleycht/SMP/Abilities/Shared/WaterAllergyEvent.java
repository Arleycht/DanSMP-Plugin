package io.github.arleycht.SMP.Abilities.Shared;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WaterAllergyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final boolean wearingWaterProtection;
    private final WaterDamageCause cause;

    private double damageAmount;

    private boolean cancelled;

    public WaterAllergyEvent(Player player, double damageAmount, boolean isWearingWaterProtection, WaterDamageCause cause) {
        this.player = player;
        this.damageAmount = damageAmount;
        this.wearingWaterProtection = isWearingWaterProtection;
        this.cause = cause;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isWearingWaterProtection() {
        return wearingWaterProtection;
    }

    public WaterDamageCause getCause() {
        return cause;
    }

    public void setDamageAmount(double damageAmount) {
        this.damageAmount = damageAmount;
    }

    public double getDamageAmount() {
        return damageAmount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum WaterDamageCause {
        WATER,
        RAIN
    }

    public static class WaterDamageData {
        public double damageAmount;
        public boolean isProtected;
    }
}

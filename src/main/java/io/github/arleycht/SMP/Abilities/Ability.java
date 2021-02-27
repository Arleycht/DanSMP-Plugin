package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Characters.Actor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public abstract class Ability implements Listener, Runnable {
    public static final String NO_NAME = "Blank Ability";
    public static final String NO_DESCRIPTION = "No description.";

    protected Plugin plugin;
    protected Actor owner;

    public Ability() {

    }

    public boolean isRunnable() {
        return false;
    }

    public long getTaskIntervalTicks() {
        return -1L;
    }

    @Override
    public void run() {

    }

    public boolean isOwner(UUID uuid) {
        if (owner == null) {
            return false;
        }

        return owner.getUniqueId().equals(uuid);
    }

    public String getName() {
        return NO_NAME;
    }

    public String getDescription() {
        return NO_DESCRIPTION;
    }

    public Actor getOwner() {
        return owner;
    }

    public void setOwner(Actor owner) {
        this.owner = owner;
    }
}

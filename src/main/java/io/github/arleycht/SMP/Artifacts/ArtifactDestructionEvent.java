package io.github.arleycht.SMP.Artifacts;

import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ArtifactDestructionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Item item;
    private final IArtifact artifact;

    public ArtifactDestructionEvent(Item item, IArtifact artifact) {
        this.item = item;
        this.artifact = artifact;
    }

    public Item getEntity() {
        return item;
    }

    public IArtifact getArtifact() {
        return artifact;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

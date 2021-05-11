package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Material;
import org.bukkit.event.Listener;

public interface IArtifact extends Listener {
    /**
     * Allow possible destruction of the artifact
     * @return Whether the artifact should be destroyable
     */
    boolean allowDestruction();

    String getName();
    String[] getLore();
    Material getType();
}

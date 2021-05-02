package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Material;

public interface IArtifact {
    /**
     * Allow multiple instances of the artifact
     * @return Whether multiple instances of the artifact should be allowed
     */
    boolean allowDuplicates();

    /**
     * Allow possible destruction of the artifact
     * @return Whether the artifact should be destroyable
     */
    boolean allowDestruction();

    String getName();
    String[] getLore();
    Material getType();
}

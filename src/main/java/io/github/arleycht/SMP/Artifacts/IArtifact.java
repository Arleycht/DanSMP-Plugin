package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Material;

public interface IArtifact {
    String getName();
    String[] getLore();
    Material getType();
    boolean allowDuplicates();
}

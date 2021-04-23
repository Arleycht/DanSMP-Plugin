package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Material;

public class TestArtifact implements IArtifact {
    @Override
    public boolean allowDuplicates() {
        return true;
    }

    @Override
    public String getName() {
        return "The Test";
    }

    @Override
    public String[] getLore() {
        return new String[0];
    }

    @Override
    public Material getType() {
        return null;
    }
}

package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Material;
import org.bukkit.event.Listener;

public class TestArtifact implements IArtifact, Listener {
    @Override
    public boolean allowDuplicates() {
        return false;
    }

    @Override
    public boolean allowDestruction() {
        return false;
    }

    @Override
    public String getName() {
        return "The Test";
    }

    @Override
    public String[] getLore() {
        return new String[]{
            "A test artifact that cannot be duplicated."
        };
    }

    @Override
    public Material getType() {
        return null;
    }
}

package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class TestArtifact implements IArtifact {
    private final String artifactName;

    public TestArtifact(String artifactName) {
        this.artifactName = artifactName;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (ArtifactManager.getArtifact(event.getItem()) == this) {
            Bukkit.broadcastMessage("Artifact was used!");
        }
    }

    @Override
    public boolean allowDestruction() {
        return false;
    }

    @Override
    public String getName() {
        return artifactName;
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

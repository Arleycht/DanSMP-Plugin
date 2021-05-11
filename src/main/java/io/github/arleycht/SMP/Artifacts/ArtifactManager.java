package io.github.arleycht.SMP.Artifacts;

import io.github.arleycht.SMP.DanSMP;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;

public class ArtifactManager {
    public static final HashMap<String, IArtifact> ARTIFACT_MAP = new HashMap<>();

    private static Plugin plugin;

    private ArtifactManager() {

    }

    public static void initialize(Plugin plugin) {
        ArtifactManager.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new ArtifactListener(), plugin);
    }

    public static void registerArtifact(IArtifact artifact) {
        ARTIFACT_MAP.put(artifact.getName(), artifact);

        Bukkit.getPluginManager().registerEvents(artifact, DanSMP.getPlugin());
    }

    public static NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    /**
     * Tags an item as an artifact
     * @param itemStack ItemStack to tag
     * @param artifactName Artifact name to tag the ItemStack with
     */
    public static void tagItem(ItemStack itemStack, String artifactName) {
        IArtifact artifact = ARTIFACT_MAP.get(artifactName);

        if (artifact == null) {
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();

        assert(plugin != null);
        assert(meta != null);

        ArrayList<String> lore = new ArrayList<>();

        // Display

        meta.setDisplayName(artifact.getName());

        lore.add("Artifact");

        meta.setLore(lore);

        // Tagging

        PersistentDataContainer data = meta.getPersistentDataContainer();

        // Name

        data.set(getNamespacedKey("ArtifactName"), PersistentDataType.STRING, artifact.getName());

        itemStack.setItemMeta(meta);
    }

    public static IArtifact getArtifact(String artifactName) {
        return ARTIFACT_MAP.get(artifactName);
    }

    public static IArtifact getArtifact(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }

        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            return null;
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();

        NamespacedKey nameKey = getNamespacedKey("ArtifactName");

        if (data.isEmpty() || !data.has(nameKey, PersistentDataType.STRING)) {
            return null;
        }

        String artifactName = data.get(nameKey, PersistentDataType.STRING);

        return ARTIFACT_MAP.get(artifactName);
    }

    public static IArtifact getArtifact(Item item) {
        if (item == null) {
            return null;
        }

        return getArtifact(item.getItemStack());
    }
}

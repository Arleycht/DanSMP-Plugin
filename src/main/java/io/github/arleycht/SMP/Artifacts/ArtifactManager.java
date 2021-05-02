package io.github.arleycht.SMP.Artifacts;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArtifactManager {
    public static final HashMap<String, IArtifact> ARTIFACT_MAP = new HashMap<>();

    private static Plugin plugin;

    private ArtifactManager() {

    }

    public static void initialize(Plugin plugin) {
        ArtifactManager.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new ArtifactListener(), plugin);
    }

    public static NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, key);
    }

    /**
     * Tags an item as an artifact
     * @param itemStack ItemStack to tag
     * @param artifact Artifact to associate with the ItemStack
     */
    public static void tagItem(ItemStack itemStack, IArtifact artifact) {
        if (!ARTIFACT_MAP.containsKey(artifact.getName())) {
            ARTIFACT_MAP.put(artifact.getName(), artifact);
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

    public static IArtifact getArtifactFromItemStack(ItemStack itemStack) {
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

        Bukkit.broadcastMessage(MessageFormat.format("Checking {0} against registered artifacts", artifactName));

        for (Map.Entry<String, IArtifact> entry : ARTIFACT_MAP.entrySet()) {
            Bukkit.broadcastMessage(entry.getKey());
        }

        return ARTIFACT_MAP.get(artifactName);
    }
}

package io.github.arleycht.SMP.Artifacts;

import io.github.arleycht.SMP.Characters.Actor;
import io.github.arleycht.SMP.Characters.ActorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ArtifactManager {
    public static final HashMap<String, IArtifact> ARTIFACT_MAP = new HashMap<>();

    private static Plugin plugin;

    public static void initialize(Plugin plugin) {
        ArtifactManager.plugin = plugin;
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

    public static void findArtifact() {
        // Shallow check

        List<OfflinePlayer> players;

        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            UUID uuid = player.getUniqueId();
            Actor actor = ActorRegistry.getActorFromUuid(uuid);
            String name = player.getName();

            if (actor != null) {
                Bukkit.broadcastMessage(actor.getRealName());
            } else if (name != null) {
                Bukkit.broadcastMessage(name);
            } else {
                Bukkit.broadcastMessage(uuid.toString());
            }


        }
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

        return ARTIFACT_MAP.get(artifactName);
    }
}

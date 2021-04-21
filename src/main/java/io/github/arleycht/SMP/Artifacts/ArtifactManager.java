package io.github.arleycht.SMP.Artifacts;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class ArtifactManager {
    public static final HashMap<String, Artifact> ARTIFACT_MAP = new HashMap<String, Artifact>() {
    };

    public static void initialize() {

    }

    public static void tagItem(ItemStack itemStack, Artifact artifact) {
        //itemStack

        ItemMeta meta = itemStack.getItemMeta();


    }
}

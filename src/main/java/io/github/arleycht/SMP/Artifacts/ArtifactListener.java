package io.github.arleycht.SMP.Artifacts;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.*;

import java.util.Set;

public class ArtifactListener implements Listener {
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        ItemStack itemStack = null;
        Inventory inventory = null;

        if (event.isShiftClick()) {
            itemStack = event.getCurrentItem();
            inventory = event.getInventory();
        } else {
            itemStack = event.getCursor();
            inventory = event.getClickedInventory();
        }

        IArtifact artifact = ArtifactManager.getArtifactFromItemStack(itemStack);

        if (artifact != null && !artifact.allowDestruction()) {
            if (IsPotentiallyDestructiveInventory(inventory)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        ItemStack itemStack = event.getOldCursor();
        IArtifact artifact = ArtifactManager.getArtifactFromItemStack(itemStack);

        if (artifact != null && !artifact.allowDestruction()) {
            Inventory inventory = event.getInventory();

            InventoryView view = event.getView();
            Set<Integer> slots = event.getRawSlots();

            for (int slot : slots) {
                if (IsPotentiallyDestructiveInventory(view.getInventory(slot))) {
                    event.setCancelled(true);

                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        IArtifact artifact = ArtifactManager.getArtifactFromItemStack(event.getItem());

        if (artifact != null && !artifact.allowDestruction()) {
            if (IsPotentiallyDestructiveInventory(event.getDestination())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        Item entity = event.getEntity();

        IArtifact artifact = ArtifactManager.getArtifactFromItemStack(entity.getItemStack());

        if (artifact != null && !artifact.allowDestruction()) {
            // TODO: Just log what time the artifact was dropped so that way you can manually track where it goes


        }
    }

    public boolean IsPotentiallyDestructiveInventory(Inventory inventory) {
        return !(inventory instanceof AbstractHorseInventory
        || inventory instanceof DoubleChestInventory
        || inventory instanceof LecternInventory
        || inventory instanceof PlayerInventory);
    }
}

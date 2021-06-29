package io.github.arleycht.SMP.Artifacts;

import io.github.arleycht.SMP.DanSMP;
import io.github.arleycht.SMP.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class ArtifactListener implements Listener {
    private final HashMap<Item, IArtifact> DROPPED_ARTIFACTS = new HashMap<>();

    public ArtifactListener() {
        Bukkit.getScheduler().runTaskTimer(DanSMP.getPlugin(), () -> {
            ArrayList<Map.Entry<Item, IArtifact>> deadItems = new ArrayList<>();

            for (Map.Entry<Item, IArtifact> entry : DROPPED_ARTIFACTS.entrySet()) {
                Item item = entry.getKey();
                IArtifact artifact = entry.getValue();

                if (item.isDead()) {
                    deadItems.add(entry);
                } else {
                    World world = item.getWorld();

                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, item.getLocation().clone().add(0.0, 1.0, 0.0), 25);
                    world.playSound(item.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 2.0f, 2.0f);
                }
            }

            for (Map.Entry<Item, IArtifact> entry : deadItems) {
                DROPPED_ARTIFACTS.remove(entry.getKey());

                Bukkit.getPluginManager().callEvent(new ArtifactDestructionEvent(entry.getKey(), entry.getValue()));
            }
        }, 0L, 2L * 20L);
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        ItemStack itemStack;
        Inventory inventory;

        if (event.isShiftClick()) {
            itemStack = event.getCurrentItem();
            inventory = event.getInventory();
        } else {
            itemStack = event.getCursor();
            inventory = event.getClickedInventory();
        }

        IArtifact artifact = ArtifactManager.getArtifact(itemStack);

        if (artifact != null && !artifact.allowDestruction()) {
            if (IsPotentiallyDestructiveInventory(inventory)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event) {
        ItemStack itemStack = event.getOldCursor();
        IArtifact artifact = ArtifactManager.getArtifact(itemStack);

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
        IArtifact artifact = ArtifactManager.getArtifact(event.getItem());

        if (artifact != null && !artifact.allowDestruction()) {
            if (IsPotentiallyDestructiveInventory(event.getDestination())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
        IArtifact artifact = ArtifactManager.getArtifact(event.getItem());

        if (artifact != null) {
            if (IsPotentiallyDestructiveInventory(event.getInventory())) {
                event.setCancelled(true);
            } else {
                DROPPED_ARTIFACTS.remove(event.getItem());
            }
        }
    }

    @EventHandler
    public void onItemSpawnEvent(ItemSpawnEvent event) {
        Item item = event.getEntity();

        IArtifact artifact = ArtifactManager.getArtifact(item);

        if (artifact != null) {
            DROPPED_ARTIFACTS.put(item, artifact);

            String message = "The artifact {0} was dropped!";
            Bukkit.getLogger().warning(MessageFormat.format(message, artifact.getName()));

            for (Entity entity : item.getNearbyEntities(8.0, 8.0, 8.0)) {
                if (entity instanceof Player) {
                    Bukkit.getLogger().warning(MessageFormat.format("{0} was nearby!", entity.getName()));
                }
            }

            if (!artifact.allowDestruction()) {
                item.setInvulnerable(true);

                AtomicReference<Item> atomicItem = new AtomicReference<>(item);
                AtomicReference<BukkitTask> atomicTask = new AtomicReference<>();

                atomicTask.set(Bukkit.getScheduler().runTaskTimer(DanSMP.getPlugin(), () -> {
                    if (!atomicItem.get().isDead()) {
                        Util.safeTaskCancel(atomicTask.get());
                    } else {
                        item.teleport(item);
                    }
                }, 0L, 1L));
            }
        }
    }

    @EventHandler
    public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        Item item = event.getItem();

        IArtifact artifact = ArtifactManager.getArtifact(item);

        if (artifact != null) {
            DROPPED_ARTIFACTS.remove(item);

            if (entity instanceof Player) {
                Player player = (Player) entity;

                ComponentBuilder builder = new ComponentBuilder()
                        .append("You have picked up the artifact item ")
                        .append(artifact.getName()).italic(true).color(ChatColor.GOLD)
                        .append("!").reset();

                player.spigot().sendMessage(builder.create());

                String message = "The artifact {0} was picked up by {1}!";
                Bukkit.getLogger().warning(MessageFormat.format(message, artifact.getName(), player.getName()));
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                IArtifact artifact = ArtifactManager.getArtifact(item);

                if (artifact != null) {
                    DROPPED_ARTIFACTS.put(item, artifact);
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                IArtifact artifact = ArtifactManager.getArtifact(item);

                if (artifact != null) {
                    item.remove();

                    Bukkit.getPluginManager().callEvent(new ArtifactDestructionEvent(item, artifact));
                }
            }
        }
    }

    @EventHandler
    public void onItemDespawnEvent(ItemDespawnEvent event) {
        Item item = event.getEntity();

        IArtifact artifact = ArtifactManager.getArtifact(item.getItemStack());

        if (artifact != null) {
            Bukkit.getPluginManager().callEvent(new ArtifactDestructionEvent(item, artifact));
        }
    }

    @EventHandler
    public void onPlayerItemBreakEvent(PlayerItemBreakEvent event) {
        IArtifact artifact = ArtifactManager.getArtifact(event.getBrokenItem());

        if (artifact != null) {
            Bukkit.getPluginManager().callEvent(new ArtifactDestructionEvent(null, artifact));
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Item) {
            if (ArtifactManager.getArtifact((Item) entity) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArtifactDestructionEvent(ArtifactDestructionEvent event) {
        TextComponent artifactName = new TextComponent(event.getArtifact().getName());

        artifactName.setItalic(true);
        artifactName.setColor(ChatColor.GOLD);

        StringBuilder stringBuilder = new StringBuilder();

        String[] lore = event.getArtifact().getLore();

        for (int i = 0; i < lore.length; ++i) {
            if (i > 0) {
                stringBuilder.append("\n");
            }

            stringBuilder.append(lore[i]);
        }

        artifactName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(stringBuilder.toString())));

        ComponentBuilder builder = new ComponentBuilder()
                .append("The artifact ")
                .append(artifactName)
                .append(" was lost to the aether!").reset();

        Bukkit.spigot().broadcast(builder.create());

        String message = "The artifact {0} was lost to the aether!";
        Bukkit.getLogger().warning(MessageFormat.format(message, event.getArtifact().getName()));

        DROPPED_ARTIFACTS.remove(event.getEntity());
    }

    public boolean IsPotentiallyDestructiveInventory(Inventory inventory) {
        if (inventory == null) {
            return true;
        }

        switch (inventory.getType()) {
            case PLAYER:
            case CHEST:
            case BARREL:
            case ENDER_CHEST:
            case SHULKER_BOX:
            case LECTERN:
            case DISPENSER:
            case DROPPER:
            case HOPPER:
                return false;
            default:
                break;
        }

        if (inventory instanceof AbstractHorseInventory) {
            return false;
        }

        return true;
    }
}

package io.github.arleycht.SMP.Abilities;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.Random;

public class SheepAbility extends Ability {
    public static final long TASK_UPDATE_INTERVAL_TICKS = 20L;

    public static final double WOLF_AGGRO_RANGE = 15.0;

    public static final int FOOD_LEVEL_INCREMENT = 1;
    public static final float SATURATION_LEVEL_INCREMENT = 0.25f;
    public static final long EAT_COOLDOWN_MS = 5L * 1000L;

    public static final int WOOL_GENERATION_MIN = 4;
    public static final int WOOL_GENERATION_MAX = 10;
    public static final long GENERATION_COOLDOWN_MS = 25000L;

    private long lastEatenTime = 0;
    private long lastGeneratedTime = 0;
    private boolean eaten = false;

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public long getTaskIntervalTicks() {
        return TASK_UPDATE_INTERVAL_TICKS;
    }

    @Override
    public void run() {
        Player player = owner.getPlayer();

        if (player == null || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        World world = player.getWorld();
        Location location = player.getLocation();

        double size = WOLF_AGGRO_RANGE;
        double sizeSquared = Math.pow(size, 2.0);

        Collection<Entity> entities = world.getNearbyEntities(location, size, size, size);

        for (Entity entity : entities) {
            if (entity instanceof Wolf) {
                Wolf wolf = (Wolf) entity;

                if (wolf.isTamed()) {
                    continue;
                }

                double distanceSquared = location.distanceSquared(wolf.getLocation());

                if (distanceSquared < sizeSquared) {
                    wolf.setAngry(true);
                    wolf.setTarget(player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (isOwner(player) && player.isSneaking()) {
            long currentTime = System.currentTimeMillis();

            if (eaten) {
                PlayerInventory inventory = player.getInventory();
                ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND);

                if (heldItem.getType() == Material.SHEARS) {
                    if (currentTime - lastGeneratedTime > GENERATION_COOLDOWN_MS) {
                        lastGeneratedTime = currentTime;
                        eaten = false;

                        Random rng = new Random();
                        int amount = WOOL_GENERATION_MIN + rng.nextInt(WOOL_GENERATION_MAX - WOOL_GENERATION_MIN);
                        ItemStack woolItem = new ItemStack(Material.WHITE_WOOL, amount);

                        world.dropItem(player.getLocation(), woolItem);

                        world.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.0f);

                        return;
                    }
                }
            }

            Block block = event.getClickedBlock();
            Material conversionType = getConversionType(block);

            if (block != null && conversionType != null) {
                if (currentTime - lastEatenTime > EAT_COOLDOWN_MS) {
                    lastEatenTime = currentTime;
                    eaten = true;

                    block.setType(conversionType);

                    player.setFoodLevel(player.getFoodLevel() + FOOD_LEVEL_INCREMENT);
                    player.setSaturation(player.getSaturation() + SATURATION_LEVEL_INCREMENT);

                    world.playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
                }
            }
        }
    }

    private Material getConversionType(Block block) {
        if (block == null) {
            return null;
        }

        switch (block.getType()) {
            case GRASS_BLOCK:
                return Material.DIRT;
            default:
                return null;
        }
    }

    @Override
    public String getName() {
        return "Fluff";
    }

    @Override
    public String getDescription() {
        return "Eat grass and generate wool.";
    }
}

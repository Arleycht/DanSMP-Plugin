package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Random;

public class SheepAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;

    public static final double WOLF_AGGRO_RANGE = 15.0;

    public static final int FOOD_LEVEL_INCREMENT = 1;
    public static final float SATURATION_LEVEL_INCREMENT = 0.6f;

    public static final Material[] VEGETARIAN_UNFRIENDLY = {
            Material.COD, Material.COOKED_COD,
            Material.SALMON, Material.COOKED_SALMON,
            Material.TROPICAL_FISH, Material.PUFFERFISH,
            Material.PORKCHOP, Material.COOKED_PORKCHOP,
            Material.MUTTON, Material.COOKED_MUTTON,
            Material.BEEF, Material.COOKED_BEEF,
            Material.CHICKEN, Material.CHICKEN,
            Material.RABBIT, Material.COOKED_RABBIT,
    };

    public static final int WOOL_GENERATION_MIN = 4;
    public static final int WOOL_GENERATION_MAX = 10;

    private final Cooldown GENERATION_COOLDOWN = new Cooldown(25.0);
    private final Cooldown EAT_COOLDOWN = new Cooldown(5.0);

    private boolean nutritionAvailable = false;

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public long getTaskIntervalTicks() {
        return TASK_INTERVAL_TICKS;
    }

    @Override
    public void run() {
        Player player = owner.getPlayer();

        if (player == null || player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        double size = WOLF_AGGRO_RANGE;
        double sizeSquared = Math.pow(size, 2.0);

        Collection<Entity> entities = player.getNearbyEntities(size, size, size);

        Location location = player.getLocation();

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
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        // Apply hunger as if rotten flesh (80% chance of hunger for 30 seconds)

        if (Math.random() < 0.2) {
            return;
        }

        Material itemMaterial = event.getItem().getType();

        for (Material material : VEGETARIAN_UNFRIENDLY) {
            if (itemMaterial == material) {
                PotionEffect effect = new PotionEffect(PotionEffectType.HUNGER, 30 * 20, 0,
                        false, true, true);

                player.addPotionEffect(effect);

                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (isOwner(player) && player.isSneaking()) {
            long currentTime = System.currentTimeMillis();

            if (nutritionAvailable) {
                PlayerInventory inventory = player.getInventory();
                ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND);

                if (heldItem.getType() == Material.SHEARS) {
                    if (GENERATION_COOLDOWN.isReady()) {
                        GENERATION_COOLDOWN.reset();

                        nutritionAvailable = false;

                        Random rng = new Random();
                        int amount = WOOL_GENERATION_MIN + rng.nextInt(WOOL_GENERATION_MAX - WOOL_GENERATION_MIN);

                        Util.giveItem(player, Material.WHITE_WOOL, amount);

                        world.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, Util.nextFloatRange(0.9f, 1.0f));

                        return;
                    }
                }
            }

            if (EAT_COOLDOWN.isReady()) {
                Block block = event.getClickedBlock();
                Material conversionType = getConversionType(block);

                if (block != null && conversionType != null) {
                    EAT_COOLDOWN.reset();

                    nutritionAvailable = true;

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

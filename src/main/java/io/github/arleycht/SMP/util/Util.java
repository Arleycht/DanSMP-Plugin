package io.github.arleycht.SMP.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Util {
    /**
     * Gives a player an ItemStack, dropping anything it couldn't store to the world.
     * @param player Player to give the ItemStack to
     * @param material The material that will be given
     * @param amount The amount that will be given
     * @return Whether any items couldn't be stored in the player's inventory
     */
    public static boolean giveItem(@NotNull Player player, @NotNull Material material, int amount) {
        ItemStack item = new ItemStack(material, amount);
        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);

        Location location = player.getLocation();
        World world = player.getWorld();

        for (Map.Entry<Integer, ItemStack> entry : leftovers.entrySet()) {
            world.dropItem(location, entry.getValue());
        }

        return !leftovers.isEmpty();
    }

    public static void applyEffect(@NotNull Player player, @NotNull PotionEffectType effectType, float durationSeconds, int amplifier) {
        PotionEffect effect = new PotionEffect(effectType, (int) (durationSeconds * 20), amplifier, false, true, true);

        player.addPotionEffect(effect);
    }

    public static void applyEffect(@NotNull Player player, @NotNull PotionEffectType effectType, float durationSeconds, int amplifier, boolean ambient, boolean particles, boolean icon) {
        PotionEffect effect = new PotionEffect(effectType, (int) (durationSeconds * 20), amplifier, ambient, particles, icon);

        player.addPotionEffect(effect);
    }

    public static boolean hasSkyAccess(@NotNull Player player) {
        World world = player.getWorld();
        Location location = player.getEyeLocation();
        int x = location.getBlockX();
        int z = location.getBlockZ();

        for (int y = location.getBlockY(); y < world.getMaxHeight(); ++y) {
            Material material = world.getBlockAt(x, y, z).getType();

            if (material.isSolid()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInRain(@NotNull Player player) {
        World world = player.getWorld();

        if (world.getWeatherDuration() > 0 && world.getEnvironment() == World.Environment.NORMAL) {
            return hasSkyAccess(player);
        }

        return false;
    }

    public static boolean isInWater(@NotNull Player player) {
        Location location = player.getLocation();

        final double playerWidthHalf = player.getWidth() * 0.5;
        final double playerHeight = player.getHeight();
        final int MAX_INCREMENTS = 3;

        ArrayList<Vector> offsets = new ArrayList<>();

        for (int yi = 0; yi < MAX_INCREMENTS; ++yi) {
            for (int x = -1; x < 2; ++x) {
                for (int z = -1; z < 2; ++z) {
                    offsets.add(new Vector(
                            x * playerWidthHalf,
                            playerHeight * yi / ((double) MAX_INCREMENTS / 2),
                            z * playerWidthHalf
                    ));
                }
            }
        }

        for (Vector offset : offsets) {
            Block block = location.clone().add(offset).getBlock();

            Material m = block.getType();
            BlockData data = block.getBlockData();

            boolean waterLogged = data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged();

            if (m == Material.WATER || m == Material.KELP || waterLogged) {
                return true;
            }
        }

        return false;
    }

    public static boolean safeTaskIsCancelled(@Nullable BukkitTask task) {
        if (task == null) {
            return true;
        }

        return task.isCancelled();
    }

    public static void safeTaskCancel(@Nullable BukkitTask task) {
        if (task == null) {
            return;
        }

        task.cancel();
    }

    public static void dealTrueDamage(Damageable damageable, double damage) {
        if (damageable.isDead() || damageable.isInvulnerable()) {
            return;
        }

        if (damageable instanceof Player && ((Player) damageable).getGameMode() == GameMode.CREATIVE) {
            return;
        }

        double newHealth = Math.max(0.0, damageable.getHealth() - damage);

        damageable.damage(0.000001);

        if (damageable.isDead()) {
            newHealth = 0.0;
        }

        damageable.setHealth(newHealth);
    }

    public static void dealTrueDamage(Damageable damageable, double damage, Entity source) {
        if (damageable.isDead() || damageable.isInvulnerable()) {
            return;
        }

        if (damageable instanceof Player && ((Player) damageable).getGameMode() == GameMode.CREATIVE) {
            return;
        }

        double newHealth = Math.max(0.0, damageable.getHealth() - damage);

        damageable.damage(0.000001, source);

        if (damageable.isDead()) {
            newHealth = 0.0;
        }

        damageable.setHealth(newHealth);
    }

    public static int nextIntRange(int min, int max) {
        assert(max >= min);

        return min + (int) ((new Random()).nextFloat() * (max - min));
    }

    public static float nextFloatRange(float min, float max) {
        assert(max >= min);

        return min + ((new Random()).nextFloat() * (max - min));
    }

    public static double nextDoubleRange(double min, double max) {
        assert(max >= min);

        return min + ((new Random()).nextDouble() * (max - min));
    }

    public static double angleBetween(Vector a, Vector b) {
        a = a.clone().normalize();
        b = b.clone().normalize();

        return Math.acos(a.dot(b));
    }
}

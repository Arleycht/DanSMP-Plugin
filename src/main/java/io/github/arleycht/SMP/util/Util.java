package io.github.arleycht.SMP.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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
}

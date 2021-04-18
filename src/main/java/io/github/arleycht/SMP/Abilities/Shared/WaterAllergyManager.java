package io.github.arleycht.SMP.Abilities.Shared;

import io.github.arleycht.SMP.Abilities.Ability;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class WaterAllergyManager {
    public static double WATER_DAMAGE = 1.0;
    public static long WATER_DAMAGE_INTERVAL_TICKS = 30L;

    public static double PROTECTED_WATER_DAMAGE = 0.25;

    private static final HashMap<UUID, Ability> ALLERGIC = new HashMap<>();
    private static BukkitTask waterDamageTask = null;

    public static void initialize(Plugin plugin) {
        Util.safeTaskCancel(waterDamageTask);

        waterDamageTask = Bukkit.getScheduler().runTaskTimer(plugin, WaterAllergyManager::update, 0L, WATER_DAMAGE_INTERVAL_TICKS);
    }

    public static void add(UUID uuid, Ability ability) {
        ALLERGIC.put(uuid, ability);
    }

    public static void remove(UUID uuid) {
        ALLERGIC.remove(uuid);
    }

    @Nullable public static Ability getAbility(UUID uuid) {
        return ALLERGIC.get(uuid);
    }

    public static boolean isAllergic(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }

        return ALLERGIC.containsKey(entity.getUniqueId());
    }

    public static boolean isAllergic(@Nullable UUID uuid) {
        if (uuid == null) {
            return false;
        }

        return ALLERGIC.containsKey(uuid);
    }

    public static boolean isValidProtection(@Nullable ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        switch (itemStack.getType()) {
            case GLASS:
            case WHITE_STAINED_GLASS:
            case ORANGE_STAINED_GLASS:
            case MAGENTA_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS:
            case YELLOW_STAINED_GLASS:
            case LIME_STAINED_GLASS:
            case PINK_STAINED_GLASS:
            case GRAY_STAINED_GLASS:
            case LIGHT_GRAY_STAINED_GLASS:
            case CYAN_STAINED_GLASS:
            case PURPLE_STAINED_GLASS:
            case BLUE_STAINED_GLASS:
            case BROWN_STAINED_GLASS:
            case GREEN_STAINED_GLASS:
            case RED_STAINED_GLASS:
            case BLACK_STAINED_GLASS:
            case ICE:
                return true;
            default:
                break;
        }

        return false;
    }

    private static void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (!ALLERGIC.containsKey(uuid)) {
                continue;
            }

            if ((Util.isInRain(player) || player.isInWater()) && !player.isDead()) {
                PlayerInventory inventory = player.getInventory();
                ItemStack helmet = inventory.getHelmet();

                double damage = isValidProtection(helmet) ? PROTECTED_WATER_DAMAGE : WATER_DAMAGE;

                if (Util.isFatal(player, damage)) {
                    DeathMessageManager.setNextDeathMessage(uuid, ALLERGIC.get(uuid));
                }

                Util.dealTrueDamage(player, damage);
            }
        }
    }
}

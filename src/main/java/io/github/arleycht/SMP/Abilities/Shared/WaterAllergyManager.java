package io.github.arleycht.SMP.Abilities.Shared;

import io.github.arleycht.SMP.Abilities.Ability;
import io.github.arleycht.SMP.Abilities.Shared.WaterAllergyEvent.WaterDamageCause;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WaterAllergyManager {
    private static final HashMap<UUID, ArrayList<Ability>> ALLERGIC = new HashMap<>();
    private static final ArrayList<WaterAllergyEvent> eventQueue = new ArrayList<>();

    public static double WATER_DAMAGE = 1.0;
    public static long WATER_DAMAGE_INTERVAL_TICKS = 30L;
    public static double PROTECTED_WATER_DAMAGE = 0.25;
    private static BukkitTask waterCheckTask = null;
    private static BukkitTask waterDamageTask = null;

    public static void initialize(Plugin plugin) {
        Util.safeTaskCancel(waterCheckTask);
        Util.safeTaskCancel(waterDamageTask);

        waterCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, WaterAllergyManager::update, 0L, 1L);
        waterDamageTask = Bukkit.getScheduler().runTaskTimer(plugin, WaterAllergyManager::applyWaterDamage, 0L, WATER_DAMAGE_INTERVAL_TICKS);
    }

    /**
     * Register a water allergy with an ability
     *
     * @param uuid    Player UUID
     * @param ability Ability that is registering the allergy
     */
    public static void add(UUID uuid, Ability ability) {
        if (ALLERGIC.containsKey(uuid)) {
            ALLERGIC.get(uuid).add(ability);
        } else {
            ArrayList<Ability> l = new ArrayList<>();

            l.add(ability);

            ALLERGIC.put(uuid, l);
        }
    }

    /**
     * Unregisters a water allergy with an ability
     *
     * @param uuid    Player UUID
     * @param ability Ability that is registering the allergy
     */
    public static void remove(UUID uuid, Ability ability) {
        if (ALLERGIC.containsKey(uuid)) {
            ArrayList<Ability> l = ALLERGIC.get(uuid);

            l.remove(ability);

            if (l.size() <= 0) {
                ALLERGIC.remove(uuid);
            }
        }
    }

    public static Ability[] getAbilities(UUID uuid) {
        ArrayList<Ability> abilities = ALLERGIC.get(uuid);

        if (abilities != null) {
            return abilities.toArray(new Ability[0]);
        }

        return new Ability[0];
    }

    public static boolean isAllergic(@Nullable Entity entity) {
        return entity != null && isAllergic(entity.getUniqueId());
    }

    public static boolean isAllergic(@Nullable UUID uuid) {
        return uuid != null && ALLERGIC.containsKey(uuid);
    }

    public static boolean isDeadlyPotion(ItemStack itemStack) {
        if (itemStack.getType() != Material.POTION && itemStack.getType() != Material.SPLASH_POTION) {
            return false;
        }

        ItemMeta meta = itemStack.getItemMeta();

        if (meta instanceof PotionMeta) {
            PotionType potionType = ((PotionMeta) meta).getBasePotionData().getType();

            return potionType == PotionType.WATER || potionType == PotionType.MUNDANE || potionType == PotionType.AWKWARD;
        }

        return false;
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

    private static void applyWaterDamage() {
        for (WaterAllergyEvent event : eventQueue) {
            Player player = event.getPlayer();

            UUID uuid = player.getUniqueId();
            double damage = event.getDamageAmount();

            if (Util.isFatal(player, damage)) {
                Ability[] abilities = getAbilities(uuid);

                if (abilities.length <= 0) {
                    // This should never happen
                    Bukkit.getLogger().warning("No abilities mapped to player with water allergy?");

                    continue;
                }

                Ability ability = abilities[Util.nextIntRange(0, abilities.length)];

                DeathMessageManager.setNextDeathMessage(uuid, ability);
            }

            Util.dealTrueDamage(player, damage);
        }
    }

    private static void update() {
        eventQueue.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (!ALLERGIC.containsKey(uuid)) {
                continue;
            }

            if ((player.isInWater() || Util.isInRain(player)) && !player.isDead()) {
                PlayerInventory inventory = player.getInventory();
                ItemStack helmet = inventory.getHelmet();

                Ability[] abilities = WaterAllergyManager.getAbilities(uuid);

                if (abilities.length <= 0) {
                    // This should never happen
                    Bukkit.getLogger().warning("No abilities mapped to player with water allergy?");

                    continue;
                }

                boolean hasProtection = isValidProtection(helmet);
                double damage = hasProtection ? PROTECTED_WATER_DAMAGE : WATER_DAMAGE;
                WaterDamageCause cause = player.isInWater() ? WaterDamageCause.WATER : WaterDamageCause.RAIN;

                WaterAllergyEvent event = new WaterAllergyEvent(player, damage, hasProtection, cause);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    eventQueue.add(event);
                }
            }
        }
    }
}

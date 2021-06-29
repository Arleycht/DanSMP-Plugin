package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HuntAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;
    public static final int DUPLICATION_ODDS = 3;

    public static final HashSet<Material> LIGHT_ARMORS = new HashSet<>();
    public static final HashSet<Material> HEAVY_ARMORS = new HashSet<>();
    public static final double INCREASE_PER_LIGHT_ARMOR = 0.4 / 4.0;
    public static final double DECREASE_PER_HEAVY_ARMOR = 0.4 / 4.0;

    private final HashMap<UUID, Long> slainEntries = new HashMap<>();

    static {
        LIGHT_ARMORS.add(Material.LEATHER_HELMET);
        LIGHT_ARMORS.add(Material.LEATHER_CHESTPLATE);
        LIGHT_ARMORS.add(Material.LEATHER_LEGGINGS);
        LIGHT_ARMORS.add(Material.LEATHER_BOOTS);

        LIGHT_ARMORS.add(Material.CHAINMAIL_HELMET);
        LIGHT_ARMORS.add(Material.CHAINMAIL_CHESTPLATE);
        LIGHT_ARMORS.add(Material.CHAINMAIL_LEGGINGS);
        LIGHT_ARMORS.add(Material.CHAINMAIL_BOOTS);

        LIGHT_ARMORS.add(Material.GOLDEN_HELMET);
        LIGHT_ARMORS.add(Material.GOLDEN_CHESTPLATE);
        LIGHT_ARMORS.add(Material.GOLDEN_LEGGINGS);
        LIGHT_ARMORS.add(Material.GOLDEN_BOOTS);

        HEAVY_ARMORS.add(Material.IRON_HELMET);
        HEAVY_ARMORS.add(Material.IRON_CHESTPLATE);
        HEAVY_ARMORS.add(Material.IRON_LEGGINGS);
        HEAVY_ARMORS.add(Material.IRON_BOOTS);

        HEAVY_ARMORS.add(Material.DIAMOND_HELMET);
        HEAVY_ARMORS.add(Material.DIAMOND_CHESTPLATE);
        HEAVY_ARMORS.add(Material.DIAMOND_LEGGINGS);
        HEAVY_ARMORS.add(Material.DIAMOND_BOOTS);

        HEAVY_ARMORS.add(Material.NETHERITE_HELMET);
        HEAVY_ARMORS.add(Material.NETHERITE_CHESTPLATE);
        HEAVY_ARMORS.add(Material.NETHERITE_LEGGINGS);
        HEAVY_ARMORS.add(Material.NETHERITE_BOOTS);
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public long getTaskIntervalTicks() {
        return TASK_INTERVAL_TICKS;
    }

    /**
     * Movement speed bonus when wearing light armor
     * Movement speed penalty when wearing heavy armor
     */
    @Override
    public void run() {
        Player player = owner.getPlayer();

        if (player == null) {
            return;
        }

        double i = 0;

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null) {
                Material material = itemStack.getType();

                if (LIGHT_ARMORS.contains(material)) {
                    i += INCREASE_PER_LIGHT_ARMOR;
                } else if (HEAVY_ARMORS.contains(material)) {
                    i -= DECREASE_PER_HEAVY_ARMOR;
                }
            }
        }

        this.clearAttributeModifiers();
        this.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, i, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
        Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));
    }

    @EventHandler
    public void on(EntityDamageEvent event) {
        if (isOwner(event.getEntity())) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            if (cause == EntityDamageEvent.DamageCause.WITHER) {
                double damage = event.getFinalDamage() * 0.8;

                Entity entity = event.getEntity();

                if (entity instanceof Damageable) {
                    Damageable damageable = (Damageable) entity;

                    if (!Util.isFatal(damageable, damage)) {
                        damageable.damage(damage);
                    }
                }
            }
        }
    }

    /**
     * Increased drops from animals
     * @param event
     */
    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();

        if (slainEntries.containsKey(uuid)) {
            slainEntries.remove(uuid);

            World world = event.getEntity().getWorld();
            Location location = event.getEntity().getLocation();

            Random random = new Random();

            for (ItemStack itemStack : event.getDrops()) {
                if (random.nextInt(DUPLICATION_ODDS) == 0) {
                    world.dropItemNaturally(location, itemStack);
                }
            }

            // Remove old keys (older than 1 second)

            if (!slainEntries.isEmpty()) {
                ArrayList<UUID> oldKeys = new ArrayList<>();

                for (HashMap.Entry<UUID, Long> entry : slainEntries.entrySet()) {
                    if (entry.getValue() > 1000) {
                        oldKeys.add(entry.getKey());
                    }
                }

                for (UUID oldUuid : oldKeys) {
                    slainEntries.remove(oldUuid);
                }
            }
        }
    }

    /**
     * Increased drops from animals
     * @param event
     */
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!isOwner(damager)) {
            return;
        }

        if (entity instanceof Animals) {
            Animals animal = (Animals) entity;

            if (Util.isFatal(animal, event.getFinalDamage())) {
                slainEntries.put(entity.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @Override
    public String getName() {
        return "The Hunt";
    }

    @Override
    public String getDescription() {
        return null;
    }
}

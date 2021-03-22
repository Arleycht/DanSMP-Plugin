package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class WyvernAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;
    public static final double RAIN_DAMAGE = 1.0;
    public static final long RAIN_DAMAGE_INTERVAL_TICKS = 40L;

    public static final EntityDamageEvent.DamageCause[] DAMAGE_CAUSE_IMMUNITIES = {
        EntityDamageEvent.DamageCause.FIRE,
        EntityDamageEvent.DamageCause.FIRE_TICK,
        EntityDamageEvent.DamageCause.LAVA
    };

    public static final String[] DEATH_MESSAGES = {
            "{0} had their life extinguished by water",
            "{0} couldn't swim",
            "{0} died as they begged the question"
    };

    private Cooldown fireballCooldown = new Cooldown(15.0);

    private BukkitTask rainDamageTask = null;

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, DEATH_MESSAGES);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (!isOwner(entity)) {
            return;
        }

        for (EntityDamageEvent.DamageCause cause : DAMAGE_CAUSE_IMMUNITIES) {
            if (event.getCause() == cause) {
                event.setCancelled(true);

                entity.setFireTicks(0);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player) || !player.isSneaking()) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_AIR) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack handItem = event.getItem();

        if (handItem == null || handItem.getType() != Material.FIRE_CHARGE) {
            return;
        }

        // Activate ability

        if (fireballCooldown.isNotReady()) {
            return;
        }

        fireballCooldown.reset();

        if (player.getGameMode() != GameMode.CREATIVE) {
            handItem.setAmount(handItem.getAmount() - 1);
        }

        // Play sound

        World world = player.getWorld();

        world.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

        // Create fireball

        Location location = player.getEyeLocation();
        Vector direction = location.getDirection();

        Fireball fireball = world.spawn(location, Fireball.class);

        fireball.setShooter(player);
        fireball.setDirection(direction);

        fireball.setIsIncendiary(true);
        fireball.setYield(2.0f);
    }

    @EventHandler
    public void WeatherChangeEvent(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            if (rainDamageTask != null) {
                rainDamageTask.cancel();
            }

            rainDamageTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                Player player = owner.getPlayer();

                if (player == null) {
                    return;
                }

                // Check sky access

                World world = event.getWorld();
                Location location = player.getLocation();

                int x = (int) location.getX();
                int z = (int) location.getZ();

                for (int y = (int) location.getY(); y < world.getMaxHeight(); ++y) {
                    if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
                        return;
                    }
                }

                // Set death message

                if (player.getHealth() <= RAIN_DAMAGE) {
                    DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);
                }

                player.damage(RAIN_DAMAGE);

            }, RAIN_DAMAGE_INTERVAL_TICKS, RAIN_DAMAGE_INTERVAL_TICKS);
        } else if (rainDamageTask != null) {
            rainDamageTask.cancel();
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (isOwner(player) && itemStack.getType() == Material.POTION) {

        }
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && player.isSneaking() && !player.isOnGround()) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (isOwner(player) && !player.isOnGround() && !player.isFlying()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getName() {
        return "Reign of Fire";
    }

    @Override
    public String getDescription() {
        return "Being a Flying-Fire type, Wyverns are weak to water.";
    }
}

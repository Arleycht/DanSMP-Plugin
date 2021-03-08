package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class WyvernAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;
    public static final double RAIN_DAMAGE = 1.0;
    public static final long RAIN_DAMAGE_INTERVAL_TICKS = 40L;

    public static final String[] DEATH_MESSAGES = {
            "{0} had their life extinguished by water",
            "{0} couldn't swim",
            "{0} died as they begged the question"
    };

    private BukkitTask rainDamageTask = null;

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, DEATH_MESSAGES);
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

package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

public class WyvernAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;
    public static final double RAIN_DAMAGE = 1.0;

    public static final String RAIN_DEATH_MESSAGE = "%s died to water";
    public static final String WATER_DEATH_MESSAGE = "%s couldn't swim";

    private static final Cooldown RAIN_DAMAGE_COOLDOWN = new Cooldown(2.0);


    private boolean isDeadToRain = false;

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

        if (player != null) {
            World world = player.getWorld();

            if (world.getWeatherDuration() > 0) {
                if (RAIN_DAMAGE_COOLDOWN.isReady()) {
                    Location location = player.getLocation();

                    int x = (int) location.getX();
                    int z = (int) location.getZ();

                    for (int y = (int) location.getY(); y < world.getMaxHeight(); ++y) {
                        if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
                            return;
                        }
                    }

                    RAIN_DAMAGE_COOLDOWN.reset();

                    if (player.getHealth() <= RAIN_DAMAGE) {
                        isDeadToRain = true;
                    }

                    player.damage(RAIN_DAMAGE);
                }
            }
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
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (isDeadToRain && isOwner(event.getEntity())) {
            isDeadToRain = false;

            event.setDeathMessage(RAIN_DEATH_MESSAGE);
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

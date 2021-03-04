package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DarkAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;

    public static final long BEGIN_TIME = 10800L;
    public static final long END_TIME = 1200L;
    public static final long ACTIVE_INTERVAL_TIME = Math.abs(END_TIME - BEGIN_TIME);

    public static final double DAMAGE_MULTIPLIER = 2.0;

    public static final String ACTIVE_MESSAGE = "You feel the moon begin to rise";
    public static final String INACTIVE_MESSAGE = "You feel the moon begin to set";

    private final Cooldown checkCooldown = new Cooldown(1.0);

    private boolean active = false;

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

        if (player == null) {
            return;
        }

        World world = player.getWorld();

        if (active && player.getPotionEffect(PotionEffectType.INVISIBILITY) == null) {
            Location location = player.getLocation();

            double radius = 0.75;

            double t = ((double) System.currentTimeMillis()) / 1000.0;
            t *= Math.PI;

            double x =  Math.sin(t) * radius;
            double y = 1.0 + Math.sin((t * Math.E) + 0.5) * 0.25;
            double z = Math.cos(t) * radius;

            Particle.DustOptions data = new Particle.DustOptions(Color.WHITE, 0.25f);

            location = location.add(new Vector(x, y, z));

            world.spawnParticle(Particle.REDSTONE, location, 3, data);
        }

        if (checkCooldown.isNotReady()) {
            return;
        }

        checkCooldown.reset();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        active = Math.floorMod(world.getTime() - BEGIN_TIME, 24000L) < ACTIVE_INTERVAL_TIME;
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity entity = event.getEntity();

        if (attacker instanceof Projectile) {
            // Get entity that shot the projectile
            attacker = (Entity) ((Projectile) attacker).getShooter();
        }

        if (!(entity instanceof Damageable)) {
            return;
        }

        Damageable victim = (Damageable) entity;

        // Apply effect when active and attacker is owner,
        // or when inactive and victim is owner
        boolean applyEffect = active ? isOwner(attacker) : isOwner(victim);

        if (applyEffect) {
            event.setDamage(event.getDamage() * DAMAGE_MULTIPLIER);
        }
    }

    @Override
    public String getName() {
        return "In the Dark";
    }

    @Override
    public String getDescription() {
        return "You are twice damaging at night, and twice damaged at day.";
    }
}

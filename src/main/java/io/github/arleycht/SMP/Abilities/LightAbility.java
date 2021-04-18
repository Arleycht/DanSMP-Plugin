package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class LightAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;

    public static final long BEGIN_TIME = 22800L;
    public static final long END_TIME = 13200L;
    public static final long INTERVAL_DURATION = Math.floorMod(END_TIME - BEGIN_TIME, 24000);

    public static final double DAMAGE_MULTIPLIER = 2.0;

    private final Cooldown checkCooldown = new Cooldown(5.0);

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

        if (checkCooldown.isReady()) {
            checkCooldown.reset();

            active = false;

            if (world.getEnvironment() == World.Environment.NORMAL) {
                active = Math.floorMod(world.getTime() - BEGIN_TIME, 24000) < INTERVAL_DURATION;
            }
        }

        if (active && player.getPotionEffect(PotionEffectType.INVISIBILITY) == null) {
            Location location = player.getLocation();

            double radius = 0.75;

            double t = ((double) System.currentTimeMillis()) / 1000.0;
            t *= Math.PI;

            double x =  Math.sin(t) * radius;
            double y = 1.0 + Math.sin((t * Math.E) + 0.5) * 0.25;
            double z = Math.cos(t) * radius;

            Particle.DustOptions data = new Particle.DustOptions(Color.YELLOW, 0.25f);

            location = location.add(new Vector(x, y, z));

            world.spawnParticle(Particle.REDSTONE, location, 3, data);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity entity = event.getEntity();

        if (attacker instanceof Arrow) {
            // Get entity that shot the projectile
            attacker = (Entity) ((Arrow) attacker).getShooter();
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

            World world = victim.getWorld();

            BoundingBox box = victim.getBoundingBox();

            double x = box.getWidthX();
            double y = box.getHeight();
            double z = box.getWidthZ();

            Particle.DustOptions data = new Particle.DustOptions(Color.WHITE, 0.5f);

            world.spawnParticle(Particle.REDSTONE, victim.getLocation(), 25, x, y, z, data);
        }
    }

    @Override
    public String getName() {
        return "In the Light";
    }

    @Override
    public String getDescription() {
        return "You are twice damaging at day, and twice damaged at night.";
    }
}

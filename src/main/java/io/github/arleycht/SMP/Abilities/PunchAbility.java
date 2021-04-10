package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Util;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;

public class PunchAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;

    private long previousTime = 0;
    private boolean active = true;

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

        long modTime = Math.floorMod(world.getTime(), 24000);

        // Detect when time has looped
        if (modTime < previousTime) {
            active = true;
        }

        previousTime = modTime;

        if (active) {
            BoundingBox box = player.getBoundingBox();

            double x = box.getWidthX() * 0.5;
            double y = box.getHeight() * 0.5;
            double z = box.getWidthZ() * 0.5;

            Particle.DustOptions data = new Particle.DustOptions(Color.YELLOW, 0.5f);

            world.spawnParticle(Particle.REDSTONE, player.getLocation(), 25, x, y, z, data);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity entity = event.getEntity();

        if (attacker instanceof Projectile) {
            // Get entity that shot the projectile
            attacker = (Entity) ((Projectile) attacker).getShooter();
        }

        if (!isOwner(attacker)) {
            return;
        }

        if (!(entity instanceof Player)) {
            return;
        }

        if (!active) {
            return;
        }

        event.setCancelled(true);

        Player victim = (Player) entity;

        Util.dealTrueDamage(victim, victim.getHealth(), attacker);

        active = false;
    }

    @Override
    public String getName() {
        return "One Punch";
    }

    @Override
    public String getDescription() {
        return "You know the deal.";
    }
}

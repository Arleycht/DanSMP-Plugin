package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.Shared.DeathMessageManager;
import io.github.arleycht.SMP.util.Util;
import io.github.arleycht.SMP.Abilities.Shared.WaterAllergyManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SlimeAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 10L;
    public static final String[] DEATH_MESSAGES = {
            "{0} became too slimy",
            "{0} doesn't like water",
            "{0} died as they begged the question",
            "{0} blubbed their last blub",
            "{0} became fish food"
    };

    public static final double WEAK_ATTACK_MULTIPLIER = 2.0;
    public static final EntityDamageEvent.DamageCause[] WEAK_ATTACKS = {
            EntityDamageEvent.DamageCause.MAGIC,
            EntityDamageEvent.DamageCause.DRAGON_BREATH
    };

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, DEATH_MESSAGES);

        WaterAllergyManager.add(owner.getUniqueId(), this);
    }

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

        Util.applyEffect(player, PotionEffectType.JUMP, 10.0f, 1, false, false, false);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (isOwner(event.getEntity())) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            for (EntityDamageEvent.DamageCause weakCause : WEAK_ATTACKS) {
                if (cause == weakCause) {
                    event.setDamage(event.getDamage() * WEAK_ATTACK_MULTIPLIER);

                    return;
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        Entity hitEntity = event.getHitEntity();

        if (!isOwner(hitEntity)) {
            return;
        }

        //Bukkit.broadcastMessage("Arrow : )");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (!isOwner(entity)) {
            return;
        }

        Player player = (Player) entity;
        Entity damager = event.getDamager();

        if (damager instanceof Arrow) {
            Arrow originalArrow = (Arrow) damager;

            Vector direction = originalArrow.getVelocity().normalize();
            Location newLocation = player.getLocation();

            newLocation.setY(originalArrow.getLocation().getY());

            float newSpeed = (float) originalArrow.getVelocity().length();

            newSpeed *= 0.25f;

            if (newSpeed > 0.1f) {
                Arrow newArrow = originalArrow.getWorld().spawnArrow(newLocation, direction, newSpeed, 90.0f);

                // Combat related

                newArrow.setShooter(player);
                newArrow.setDamage(originalArrow.getDamage());
                newArrow.setCritical(originalArrow.isCritical());

                newArrow.setFireTicks(originalArrow.getFireTicks());

                newArrow.setKnockbackStrength(originalArrow.getKnockbackStrength());
                newArrow.setPierceLevel(originalArrow.getPierceLevel());

                // Misc

                newArrow.setPickupStatus(originalArrow.getPickupStatus());
                newArrow.setBounce(originalArrow.doesBounce());
            }

            originalArrow.remove();

            event.setCancelled(true);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, Util.nextFloatRange(0.8f, 1.2f));
        }
    }

    @Override
    public String getName() {
        return "Slime";
    }

    @Override
    public String getDescription() {
        return "Slime";
    }
}

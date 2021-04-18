package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.CombatHelper;
import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class DarkAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 1L;

    public static final long BEGIN_TIME = 10800L;
    public static final long END_TIME = 1200L;
    public static final long INTERVAL_DURATION = Math.floorMod(END_TIME - BEGIN_TIME, 24000);

    public static final double ADD_MULTIPLIER = 1.0;

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

            Particle.DustOptions data = new Particle.DustOptions(Color.WHITE, 0.25f);

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

        if (!(entity instanceof LivingEntity)) {
            return;
        }

        LivingEntity victim = (LivingEntity) entity;

        // Apply effect when active and attacker is owner,
        // or when inactive and victim is owner
        boolean applyEffect = active ? isOwner(attacker) : isOwner(victim);

        if (applyEffect) {
            double finalDamage = CombatHelper.getFinalDamage(event.getDamage(), victim, event.getCause());

            // If fatal regardless of multiplier, ignore
            if (!Util.isFatal(victim, finalDamage)) {
                // Apply additional damage

                double addDamage = finalDamage * ADD_MULTIPLIER;
                double totalDamage = finalDamage + addDamage;

                if (Util.isFatal(victim, totalDamage)) {
                    // If fatal after multiplier, then we can set the victim's health to the final damage
                    // and let the event continue
                    victim.setAbsorptionAmount(0.0);
                    victim.setHealth(finalDamage);
                } else {
                    // If the total damage inflicted is not fatal, everything after this should be non-fatal

                    // Absorb damage

                    double newAbsorption = victim.getAbsorptionAmount() - totalDamage;
                    double dealtDamage = Math.max(0.0, -newAbsorption);

                    // Apply absorption and health damage

                    victim.setAbsorptionAmount(Math.max(0.0, victim.getAbsorptionAmount() - dealtDamage));
                    victim.setHealth(Math.max(0.0, victim.getHealth() - dealtDamage));

                    // Nullify event, as we have applied the total damage non-fatally

                    event.setDamage(0.0);
                }
            }

            // Particle effects

            World world = victim.getWorld();

            BoundingBox box = victim.getBoundingBox();

            double x = box.getWidthX() * 0.5f;
            double y = box.getHeight() * 0.5f;
            double z = box.getWidthZ() * 0.5f;

            Particle.DustOptions data = new Particle.DustOptions(Color.WHITE, 0.5f);

            world.spawnParticle(Particle.REDSTONE, victim.getLocation(), 50, x, y, z, data);
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

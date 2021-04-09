package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

public class PunchAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 10L * 20L;
    private final Cooldown ABILITY_COOLDOWN = new Cooldown(60.0 * 60.0);

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
        if (ABILITY_COOLDOWN.isReady()) {
            Player player = owner.getPlayer();

            if (player != null) {
                Util.applyEffect(player, PotionEffectType.GLOWING, 15.0f, 0, false, false, false);
            }
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

        if (ABILITY_COOLDOWN.isNotReady()) {
            return;
        }

        ABILITY_COOLDOWN.reset();

        event.setCancelled(true);

        Player victim = (Player) entity;

        Util.dealTrueDamage(victim, victim.getHealth(), attacker);

        Bukkit.broadcastMessage("ONE PAAAAAWWWWWNNNNNCH!");
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

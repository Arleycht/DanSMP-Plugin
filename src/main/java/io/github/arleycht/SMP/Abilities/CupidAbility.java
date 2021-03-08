package io.github.arleycht.SMP.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class CupidAbility extends Ability {
    public static final double STRONG_ATTACK_MULTIPLIER = 1.5;
    public static final double WEAK_ATTACK_MULTIPLIER = 0.75;

    public static final EntityDamageEvent.DamageCause[] STRONG_ATTACKS = {
            EntityDamageEvent.DamageCause.PROJECTILE,
    };
    public static final EntityDamageEvent.DamageCause[] WEAK_ATTACKS = {
            EntityDamageEvent.DamageCause.ENTITY_ATTACK,
            EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK,
    };

    private static final PotionEffect SWIMMING_EFFECT = new PotionEffect(PotionEffectType.DOLPHINS_GRACE,
            60, 0,
            true, true, true);

    private BukkitTask effectTask = null;

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (isOwner(event.getDamager())) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            for (EntityDamageEvent.DamageCause strongCause : STRONG_ATTACKS) {
                if (cause == strongCause) {
                    event.setDamage(event.getDamage() * STRONG_ATTACK_MULTIPLIER);

                    return;
                }
            }

            for (EntityDamageEvent.DamageCause weakCause : WEAK_ATTACKS) {
                if (cause == weakCause) {
                    event.setDamage(event.getDamage() * WEAK_ATTACK_MULTIPLIER);

                    return;
                }
            }
        }
    }

    @EventHandler
    public void onEntityToggleSwimEvent(EntityToggleSwimEvent event) {
        Entity entity = event.getEntity();

        if (isOwner(entity) && entity instanceof Player) {
            Player player = (Player) entity;

            if (event.isSwimming()) {
                if (effectTask == null || effectTask.isCancelled()) {
                    effectTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () ->
                            player.addPotionEffect(SWIMMING_EFFECT), 0L, 5L);
                }
            } else if (effectTask != null) {
                effectTask.cancel();
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = event.getItem();

        if (isOwner(player) && heldItem.getType() == Material.HONEY_BOTTLE) {

        }
    }

    @Override
    public String getName() {
        return "Eros";
    }

    @Override
    public String getDescription() {
        return "Love is in the air! Stronger projectile, weaker melee, swim fast, and transform into a bee.";
    }
}

package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.Shared.DeathMessageManager;
import io.github.arleycht.SMP.Abilities.Shared.WaterAllergyManager;
import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EndermanAbility extends Ability {
    public static final double STRONG_ATTACK_MULTIPLIER = 1.5;
    public static final EntityDamageEvent.DamageCause[] STRONG_ATTACKS = {
            EntityDamageEvent.DamageCause.PROJECTILE,
    };
    public static final String[] DEATH_MESSAGES = {
            "{0} had their life extinguished by water",
            "{0} couldn't swim",
            "{0} died as they begged the question",
            "{0} blubbed their last blub",
            "{0} became fish food"
    };

    private final Cooldown ABILITY_COOLDOWN = new Cooldown(0.5);

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, DEATH_MESSAGES);

        WaterAllergyManager.add(owner.getUniqueId(), this);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack handItem = event.getItem();

        if (handItem == null || handItem.getType() != Material.ENDER_PEARL) {
            return;
        }

        event.setCancelled(true);

        // Check cooldown

        if (ABILITY_COOLDOWN.isNotReady()) {
            return;
        }

        ABILITY_COOLDOWN.reset();

        // Activate ability

        World world = player.getWorld();
        Location location = player.getEyeLocation();

        Vector velocity = location.clone().getDirection().normalize();

        velocity.multiply(Util.nextDoubleRange(1.4, 1.5));
        velocity.add(player.getVelocity());

        EnderPearl pearl = world.spawn(location, EnderPearl.class);

        pearl.setShooter(player);
        pearl.setVelocity(velocity);

        world.playSound(location, Sound.ENTITY_ENDER_PEARL_THROW, 1.0f, Util.nextFloatRange(0.75f, 1.0f));
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Location to = event.getTo();

            if (to != null) {
                event.setCancelled(true);

                World world = player.getWorld();

                player.teleport(to);
                player.setFallDistance(0.0f);
                player.setVelocity(new Vector(0.0, 0.0, 0.0));

                world.playSound(event.getFrom(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, Util.nextFloatRange(0.5f, 0.75f));
                world.playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, Util.nextFloatRange(0.9f, 1.1f));
            }
        }
    }

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
        }
    }

    @Override
    public String getName() {
        return "Of the Ender";
    }

    @Override
    public String getDescription() {
        return "Teleport like an Enderman. Try swimming.";
    }
}

package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class PaladinAbility extends Ability {
    public static final double ABILITY_RADIUS = 5.0;

    public static final ArrayList<PotionEffect> EFFECT_LIST = new ArrayList<>();

    private Cooldown abilityCooldown = new Cooldown(60.0);

    static {
        addEffect(PotionEffectType.REGENERATION, 0, 15);
        addEffect(PotionEffectType.INCREASE_DAMAGE, 0, 15);

        addEffect(PotionEffectType.SPEED, 0, 24);
        addEffect(PotionEffectType.FIRE_RESISTANCE, 0, 24);

        addEffect(PotionEffectType.ABSORPTION, 0, 30);
    }

    private static void addEffect(PotionEffectType type, int amplifier, float durationSeconds) {
        EFFECT_LIST.add(new PotionEffect(type, (int) (durationSeconds * 20), amplifier, true));
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player) || !player.isSneaking()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        // Activate ability

        if (abilityCooldown.isNotReady()) {
            return;
        }

        abilityCooldown.reset();

        // Play activation sound

        World world = player.getWorld();

        Random rng = new Random();
        float pitch = 0.8f + (rng.nextFloat() * 0.2f);

        world.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, pitch);

        // Apply potion effects

        PotionEffect effect = EFFECT_LIST.get(rng.nextInt(EFFECT_LIST.size()));

        Location location = player.getLocation();
        double radiusSquared = Math.pow(ABILITY_RADIUS, 2.0);

        for (Entity e : player.getNearbyEntities(ABILITY_RADIUS, ABILITY_RADIUS, ABILITY_RADIUS)) {
            if (!(e instanceof Player) || isOwner(e)) {
                continue;
            }

            if (e.getLocation().distanceSquared(location) < radiusSquared) {
                Player p = (Player) e;

                effect.apply(p);

                p.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public String getName() {
        return "Probabilistic Paladin";
    }

    @Override
    public String getDescription() {
        return "Gives a random buff to everyone within 10 blocks, excluding yourself.";
    }
}

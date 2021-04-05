package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
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
    public static final float FOOD_COST_PER_PLAYER = 1.0f;

    public static final Sound ACTIVATE_SOUND = Sound.ENTITY_EVOKER_PREPARE_SUMMON;
    public static final Sound RECEIVE_BUFF_SOUND = Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR;

    public static final ArrayList<PotionEffect> EFFECT_LIST = new ArrayList<>();

    private final Cooldown abilityCooldown = new Cooldown(60.0);

    static {
        // Utility
        addEffect(PotionEffectType.SPEED, 0, 30);
        addEffect(PotionEffectType.FAST_DIGGING, 0, 30);
        addEffect(PotionEffectType.FIRE_RESISTANCE, 0, 30);
        addEffect(PotionEffectType.WATER_BREATHING, 0, 60);
        addEffect(PotionEffectType.WATER_BREATHING, 0, 60);

        // Combat
        addEffect(PotionEffectType.REGENERATION, 1, 5);
        addEffect(PotionEffectType.INCREASE_DAMAGE, 0, 30);
        addEffect(PotionEffectType.DAMAGE_RESISTANCE, 0, 30);
        addEffect(PotionEffectType.ABSORPTION, 0, 60);
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

        // Shift right click on the top of a block

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (event.getBlockFace() != BlockFace.UP) {
            return;
        }

        // Activate ability

        if (abilityCooldown.isNotReady()) {
            return;
        }

        World world = player.getWorld();
        Random rng = new Random();

        // Apply potion effects

        PotionEffect effect = EFFECT_LIST.get(rng.nextInt(EFFECT_LIST.size()));

        Location location = player.getLocation();
        double radiusSquared = Math.pow(ABILITY_RADIUS, 2.0);

        int playersBuffedCount = 0;

        for (Entity e : player.getNearbyEntities(ABILITY_RADIUS, ABILITY_RADIUS, ABILITY_RADIUS)) {
            if (!(e instanceof Player) || isOwner(e)) {
                continue;
            }

            if (e.getLocation().distanceSquared(location) < radiusSquared) {
                Player p = (Player) e;
                effect.apply(p);

                ++playersBuffedCount;

                p.playSound(p.getLocation(), RECEIVE_BUFF_SOUND, 1.0f, 1.0f);
            }
        }

        // Only continue if buffs were applied

        if (playersBuffedCount < 1) {
            return;
        }

        abilityCooldown.reset();

        // Play activation sound

        world.playSound(player.getLocation(), ACTIVATE_SOUND, 1.0f, 0.8f + (rng.nextFloat() * 0.2f));

        // Decrement food level according to amount of players buffed, and take from saturation if necessary

        float finalCost = playersBuffedCount * FOOD_COST_PER_PLAYER;
        float newFoodLevel = player.getFoodLevel() - finalCost;

        if (newFoodLevel >= 0) {
            // Round up because we're nice like that
            player.setFoodLevel((int) (newFoodLevel + 0.5f));
        } else {
            // Take from saturation
            float saturation = Math.max(0.0f, player.getSaturation() + newFoodLevel);

            player.setFoodLevel(0);
            player.setSaturation(saturation);
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

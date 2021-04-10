package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class LightningAbility extends Ability {
    public final double MAX_AIM_ANGLE = 10.0;
    public static final double MAX_BOLT_RANGE = 24.0;
    public static final double MAX_BOLT_RANGE_SQUARED = Math.pow(MAX_BOLT_RANGE, 2.0);
    public static final double BOLT_DAMAGE = 2.0;

    public static final double HALF_PI = Math.PI / 2.0;

    private final Cooldown ABILITY_COOLDOWN = new Cooldown(1.5);
    private final Cooldown CHARGED_ABILITY_COOLDOWN = new Cooldown(30.0);

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack handItem = event.getItem();

        if (handItem == null || handItem.getType() != Material.SPECTRAL_ARROW) {
            return;
        }

        // Get closest entity to look direction

        World world = player.getWorld();
        Location location = player.getEyeLocation();

        Entity closestEntity = null;
        double closestAngle = Double.MAX_VALUE;

        for (Entity e : world.getNearbyEntities(location, MAX_BOLT_RANGE, MAX_BOLT_RANGE, MAX_BOLT_RANGE)) {
            if (isOwner(e) || !player.hasLineOfSight(e)) {
                continue;
            }

            Vector centerOfMass = e.getLocation().toVector().add(new Vector(0.0, e.getHeight() / 2.0, 0.0));
            Vector dir = centerOfMass.subtract(location.toVector());
            double angle = Util.angleBetween(location.getDirection(), dir);

            if (angle > HALF_PI) {
                continue;
            }

            if (angle < closestAngle && angle < MAX_AIM_ANGLE) {
                if (e.getLocation().distanceSquared(location) > MAX_BOLT_RANGE_SQUARED) {
                    continue;
                }

                closestEntity = e;
                closestAngle = angle;
            }
        }

        if (closestEntity == null) {
            return;
        }

        if (!(closestEntity instanceof Damageable)) {
            return;
        }

        Damageable victim = (Damageable) closestEntity;

        // Activate ability

        if (ABILITY_COOLDOWN.isNotReady()) {
            return;
        }

        ABILITY_COOLDOWN.reset();

        event.setCancelled(true);

        Vector startPos = player.getEyeLocation().toVector();
        Vector endPos = victim.getLocation().toVector().add(new Vector(0.0, victim.getHeight() / 2.0, 0.0));
        Vector midPos = startPos.clone().add(endPos).multiply(0.5);

        Particle.DustOptions data = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 0.2f);

        final int NODE_COUNT = 5;
        final int PARTICLE_ITERATIONS = 10;
        Vector previousNode = startPos;

        ArrayList<Location> particleLocations = new ArrayList<>();

        for (int i = 1; i < NODE_COUNT; ++i) {
            double p = (i + 1) / (double) NODE_COUNT;

            Vector currentNode = endPos.clone().subtract(startPos);

            currentNode.multiply(p);
            currentNode.add(Vector.getRandom().multiply(2.0));
            currentNode.add(startPos);

            for (int j = 0; j < PARTICLE_ITERATIONS; ++j) {
                double q = (j + 1) / (double) PARTICLE_ITERATIONS;

                Vector particlePos = currentNode.clone().subtract(previousNode);

                particlePos.multiply(q);
                particlePos.add(previousNode);

                //particleLocations.add(particlePos.toLocation(world));
                world.spawnParticle(Particle.REDSTONE, particlePos.toLocation(world), 25, 0.1, 0.1, 0.1, data);
            }

            previousNode = currentNode;
        }

        world.playSound(midPos.toLocation(world), Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 2.0f, Util.nextFloatRange(1.0f, 1.5f));

        victim.damage(BOLT_DAMAGE, player);
    }

    @Override
    public String getName() {
        return "Lightning Hands";
    }

    @Override
    public String getDescription() {
        return "You shoot lightning bolts";
    }
}

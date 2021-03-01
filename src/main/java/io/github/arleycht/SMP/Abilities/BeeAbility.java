package io.github.arleycht.SMP.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.Random;

public class BeeAbility extends Ability {
    public static final long TASK_UPDATE_INTERVAL = 20L;

    public static final int BEE_COUNT_MIN = 12;
    public static final int BEE_COUNT_MAX = 20;
    public static final int BEE_DELAY_MAX = 10;
    public static final int BEE_DURATION_TICKS = 10 * 20;
    public static final long BEE_COOLDOWN_MS = 30L * 1000L;

    private long lastBeeActivationTime = 0;

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (isOwner(entity) && (damager instanceof Player)) {
            Player attacker = (Player) damager;
            Player victim = (Player) entity;

            if (System.currentTimeMillis() - lastBeeActivationTime < BEE_COOLDOWN_MS) {
                return;
            }

            lastBeeActivationTime = System.currentTimeMillis();

            World world = victim.getWorld();
            Location location = victim.getLocation();

            Random rng = new Random();
            int beeCount = BEE_COUNT_MIN + rng.nextInt(BEE_COUNT_MAX - BEE_COUNT_MIN);

            ArrayList<Bee> bees = new ArrayList<>();

            for (int i = 0; i < beeCount; ++i) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Bee bee = world.spawn(location, Bee.class);

                    bee.setRemoveWhenFarAway(true);
                    bee.setAnger(BEE_DURATION_TICKS + BEE_DELAY_MAX);
                    bee.setTarget(attacker);

                    bees.add(bee);
                }, rng.nextInt(BEE_DELAY_MAX));
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (attacker.isDead()) {
                    for (Bee bee : bees) {
                        bee.setAnger(0);
                        bee.setHasStung(true);
                    }
                }
            }, BEE_DURATION_TICKS + BEE_DELAY_MAX);
        }
    }

    @Override
    public String getName() {
        return "Queen Bee";
    }

    @Override
    public String getDescription() {
        return "Summons bees when attacked by players, and passively generates honey.";
    }
}

package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class BeeAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;

    public static final int BEE_COUNT_MIN = 8;
    public static final int BEE_COUNT_MAX = 12;
    public static final int BEE_DELAY_MAX = 10;
    public static final int BEE_DURATION_TICKS = 10 * 20;

    private Cooldown BEE_COOLDOWN = new Cooldown(30.0);
    private Cooldown HONEY_BOTTLE_GENERATION_COOLDOWN = new Cooldown(10.0 * 60.0 * 60.0);

    @Override
    public void initialize() {
        HONEY_BOTTLE_GENERATION_COOLDOWN.reset();
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
        if (HONEY_BOTTLE_GENERATION_COOLDOWN.isNotReady()) {
            return;
        }

        Player player = owner.getPlayer();

        if (player == null) {
            return;
        }

        ItemStack honeyBottle = new ItemStack(Material.HONEY_BOTTLE);
        player.getInventory().addItem(honeyBottle);

        HONEY_BOTTLE_GENERATION_COOLDOWN.reset();
    }

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();

        if (entity instanceof Bee && isOwner(target)) {
            Bee bee = (Bee) entity;

            bee.setTarget(null);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (isOwner(entity) && (damager instanceof Player)) {
            Player attacker = (Player) damager;
            Player victim = (Player) entity;

            if (BEE_COOLDOWN.isNotReady()) {
                return;
            }

            BEE_COOLDOWN.reset();

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
                PotionEffect witherEffect = new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 1, false, false);

                for (Bee bee : bees) {
                    bee.addPotionEffect(witherEffect);
                }
            }, BEE_DURATION_TICKS + BEE_DELAY_MAX);
        }
    }

    @Override
    public String getName() {
        return "Queen V";
    }

    @Override
    public String getDescription() {
        return "Summons bees when attacked by players, and passively generates honey.";
    }
}

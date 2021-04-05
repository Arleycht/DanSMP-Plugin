package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class BeeAbility extends Ability {
    public static final int BEE_COUNT_MIN = 8;
    public static final int BEE_COUNT_MAX = 12;
    public static final int BEE_DELAY_MAX = 20;
    public static final int BEE_DURATION_TICKS = 10 * 20;

    private final Cooldown BEE_COOLDOWN = new Cooldown(45.0);
    private final Cooldown HONEY_BOTTLE_GENERATION_COOLDOWN = new Cooldown(2.0 * 60.0);
    private final Cooldown ABILITY_COOLDOWN = new Cooldown(10.0);

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack heldItem = player.getInventory().getItem(EquipmentSlot.HAND);

        World world = player.getWorld();

        if (heldItem.getType() == Material.GLASS_BOTTLE) {
            if (!player.isSneaking()) {
                return;
            }

            if (HONEY_BOTTLE_GENERATION_COOLDOWN.isNotReady()) {
                return;
            }

            HONEY_BOTTLE_GENERATION_COOLDOWN.reset();

            heldItem.setAmount(heldItem.getAmount() - 1);
            Util.giveItem(player, Material.HONEY_BOTTLE, 1);

            world.playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 1.0f, 1.0f);
        } else if (heldItem.getType() == Material.HONEYCOMB) {
            if (ABILITY_COOLDOWN.isNotReady()) {
                return;
            }

            ABILITY_COOLDOWN.reset();

            Vector velocity = player.getVelocity();

            velocity.add(new Vector(0.0, 1.0 - velocity.getY(), 0.0));

            Util.applyEffect(player, PotionEffectType.LEVITATION, 3.0f, 0, false, true, false);
            player.setVelocity(velocity);

            world.playSound(player.getLocation(), Sound.ENTITY_BEE_LOOP, 1.0f, 1.0f);

            Bukkit.getScheduler().runTaskLater(getPlugin(),
                    () -> Util.applyEffect(player, PotionEffectType.SLOW_FALLING, 6.0f, 0, false, true, false),
                    (long) (3.0f * 20.0f));
        }
    }

    @EventHandler
    public void onEntityTargetEvent(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();

        if (isOwner(target) && entity instanceof Bee) {
            Bee bee = (Bee) entity;

            if (bee.getAnger() < 1) {
                return;
            }

            bee.setTarget(null);

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (isOwner(entity) && (damager instanceof LivingEntity)) {
            LivingEntity attacker = (LivingEntity) damager;
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

            BukkitTask beeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Vector diff, target, offset;

                for (Bee bee : bees) {
                    offset = new Vector(rng.nextFloat(), attacker.getEyeHeight() / 2, rng.nextFloat());
                    target = attacker.getLocation().toVector().add(offset);
                    diff = target.subtract(bee.getLocation().toVector());

                    bee.setTarget(attacker);
                    bee.setVelocity(diff.normalize().multiply(0.5f));
                    bee.setHasStung(false);
                }
            }, 0, 5);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                beeTask.cancel();

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

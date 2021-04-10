package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.DeathMessage.DeathMessageManager;
import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.text.MessageFormat;

public class OverdriveAbility extends Ability {
    public static final int MAX_POWER = 10;
    public static final long TRUE_DAMAGE_DURATION_TICKS = 10L * 20L;
    public static final String[] ABILITY_DEATH_MESSAGES = {
            "{0} burnt out their servos",
            "{0} released the magic smoke",
            "{0}\'s operating system crashed",
            "{0} encountered a fatal error"
    };

    private final Cooldown SELECT_COOLDOWN = new Cooldown(0.1);
    private final Cooldown ABILITY_COOLDOWN = new Cooldown(5.0);
    private final Cooldown TRUE_DAMAGE_COOLDOWN = new Cooldown(30.0);

    private boolean trueDamageActive = false;
    private BukkitTask trueDamageTickTask = null;

    private int power = 1;

    @Override
    public void initialize() {
        DeathMessageManager.setDeathMessages(this, ABILITY_DEATH_MESSAGES);
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

        EquipmentSlot hand = event.getHand();

        if (hand == null) {
            return;
        }

        ItemStack item = player.getInventory().getItem(hand);

        float cost = (float) getHealthCost();

        PotionEffectType effectType = null;
        float duration = 0;
        int amplifier = 0;

        switch (item.getType()) {
            case CLOCK:
                if (SELECT_COOLDOWN.isNotReady()) {
                    break;
                }

                SELECT_COOLDOWN.reset();

                if (++power > MAX_POWER) {
                    power = 1;
                }

                cost = (float) getHealthCost();

                TextComponent component = new TextComponent(MessageFormat.format("{0} hearts", cost / 2.0f));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

                float pitch = (cost / 20.0f) + 0.5f;

                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);

                break;
            case MUSIC_DISC_11:
                break;
            case MUSIC_DISC_13:
                if (TRUE_DAMAGE_COOLDOWN.isNotReady()) {
                    break;
                }

                TRUE_DAMAGE_COOLDOWN.reset();
                trueDamageActive = true;

                cost = 0.0f;

                if (trueDamageTickTask != null) {
                    trueDamageTickTask.cancel();
                }

                trueDamageTickTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
                    if (!trueDamageActive) {
                        return;
                    }

                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.5f);
                }, 0L, 20L);

                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    trueDamageActive = false;
                    trueDamageTickTask.cancel();
                }, TRUE_DAMAGE_DURATION_TICKS);

                effectType = PotionEffectType.GLOWING;
                duration = TRUE_DAMAGE_DURATION_TICKS / 20.0f;

                break;
            case MUSIC_DISC_BLOCKS:
                // HASTE

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.FAST_DIGGING;
                duration = cost * 2.0f;

                break;
            case MUSIC_DISC_CAT:
                // SLOW FALLING

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.SLOW_FALLING;
                duration = cost * 2.0f;

                break;
            case MUSIC_DISC_CHIRP:
                break;
            case MUSIC_DISC_FAR:
                // SPEED

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.SPEED;
                duration = cost * 2.0f;

                break;
            case MUSIC_DISC_MALL:
                break;
            case MUSIC_DISC_MELLOHI:
                // JUMP

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.JUMP;
                duration = cost * 2.0f;
                amplifier = 1;

                break;
            case MUSIC_DISC_PIGSTEP:
                break;
            case MUSIC_DISC_STAL:
                break;
            case MUSIC_DISC_STRAD:
                break;
            case MUSIC_DISC_WAIT:
                break;
            case MUSIC_DISC_WARD:
                // RESISTANCE

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.DAMAGE_RESISTANCE;
                duration = cost * 2.0f;

                break;
            default:
                break;
        }

        if (ABILITY_COOLDOWN.isReady() && effectType != null) {
            ABILITY_COOLDOWN.reset();

            Util.applyEffect(player, effectType, duration, amplifier, true, true, true);

            DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);

            player.setHealth(Math.max(0.0, player.getHealth() - cost));

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (damager instanceof Projectile) {
            // Get entity that shot the projectile
            damager = (Entity) ((Projectile) damager).getShooter();
        }

        if (damager == null || !isOwner(damager)) {
            return;
        }

        if (!(damager instanceof Player) || !(entity instanceof Damageable)) {
            return;
        }

        DeathMessageManager.setNextDeathMessage(damager.getUniqueId(), this);

        // Deal true damage to both

        if (!trueDamageActive) {
            return;
        }

        Player attacker = (Player) damager;
        Damageable victim = (Damageable) entity;

        trueDamageActive = false;
        attacker.removePotionEffect(PotionEffectType.GLOWING);

        double cost = Math.min(getHealthCost(), attacker.getHealth());

        Util.dealTrueDamage(victim, cost, attacker);
        Util.dealTrueDamage(attacker, cost);

        event.setDamage(0.0);

        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, Util.nextFloatRange(0.75f, 1.0f));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (isOwner(event.getEntity())) {
            if (trueDamageActive) {
                trueDamageActive = false;
            }

            if (trueDamageTickTask != null) {
                trueDamageTickTask.cancel();
            }
        }
    }

    private double getHealthCost() {
        return (power / (double) MAX_POWER) * 20.0f;
    }

    @Override
    public String getName() {
        return "Current Overdrive";
    }

    @Override
    public String getDescription() {
        return "Sacrifice health for temporary buffs.";
    }
}

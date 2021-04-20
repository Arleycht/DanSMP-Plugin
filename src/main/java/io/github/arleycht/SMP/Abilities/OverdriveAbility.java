package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.Shared.DeathMessageManager;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class OverdriveAbility extends Ability {
    public static final int MAX_POWER = 10;
    public static final long TRUE_DAMAGE_WIND_UP_TICKS = 40L;
    public static final long TRUE_DAMAGE_DURATION_TICKS = 10L * 20L;
    public static final String[] ABILITY_DEATH_MESSAGES = {
            "{0} burnt out their servos",
            "{0} released the magic smoke",
            "{0}\'s operating system crashed",
            "{0} encountered a fatal error"
    };

    private final Cooldown SELECT_COOLDOWN = new Cooldown(0.1);
    private final Cooldown ABILITY_COOLDOWN = new Cooldown(1.5);
    private final Cooldown TRUE_DAMAGE_COOLDOWN = new Cooldown(30.0);

    private enum OverdriveState {
        INACTIVE,
        WINDING,
        ACTIVE,
        SPENT
    }

    private AtomicReference<OverdriveState> state = new AtomicReference<>(OverdriveState.INACTIVE);

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

        if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
            return;
        }

        EquipmentSlot hand = event.getHand();

        if (hand == null) {
            return;
        }

        ItemStack item = player.getInventory().getItem(hand);

        double cost = getHealthCost();

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

                cost = getHealthCost();

                TextComponent component = new TextComponent(MessageFormat.format("{0} hearts", cost / 2.0f));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);

                float pitch = ((float) cost / 20.0f) + 0.5f;

                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);

                break;
            case MUSIC_DISC_11:
                break;
            case MUSIC_DISC_13:
                if (TRUE_DAMAGE_COOLDOWN.isNotReady()) {
                    break;
                }

                if (state.get() != OverdriveState.INACTIVE && state.get() != OverdriveState.SPENT) {
                    break;
                }

                state.set(OverdriveState.WINDING);

                // Wind up and active ticks
                createTicker(0L, 5L, TRUE_DAMAGE_WIND_UP_TICKS);
                createTicker(TRUE_DAMAGE_WIND_UP_TICKS + 2L, 10L, TRUE_DAMAGE_DURATION_TICKS - 2L - 40L);
                createTicker(TRUE_DAMAGE_WIND_UP_TICKS + TRUE_DAMAGE_DURATION_TICKS - 38L, 5L, 40L);

                // Wind up state change
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    state.set(OverdriveState.ACTIVE);

                    Util.applyEffect(player, PotionEffectType.GLOWING, TRUE_DAMAGE_DURATION_TICKS / 20.0f, 0, true, true, true);
                }, TRUE_DAMAGE_WIND_UP_TICKS);

                // Deactivate after timer runs out
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                    if (TRUE_DAMAGE_COOLDOWN.isReady() && state.get() == OverdriveState.ACTIVE) {
                        state.set(OverdriveState.SPENT);

                        TRUE_DAMAGE_COOLDOWN.reset();
                    }
                }, TRUE_DAMAGE_WIND_UP_TICKS + TRUE_DAMAGE_DURATION_TICKS);

                break;
            case MUSIC_DISC_BLOCKS:
                // HASTE

                effectType = PotionEffectType.FAST_DIGGING;
                duration = (float) cost * 2.0f;
                amplifier = cost > 7.0f ? 1 : 0;

                break;
            case MUSIC_DISC_CAT:
                // SLOW FALLING

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.SLOW_FALLING;
                duration = (float) cost * 2.0f;

                break;
            case MUSIC_DISC_CHIRP:
                break;
            case MUSIC_DISC_FAR:
                // SPEED

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.SPEED;
                duration = (float) cost * 2.0f;

                break;
            case MUSIC_DISC_MALL:
                break;
            case MUSIC_DISC_MELLOHI:
                // JUMP

                // Cap cost to 10 (5 hearts), limiting the max duration to 20 seconds
                cost = Math.min(10.0f, cost);

                effectType = PotionEffectType.JUMP;
                duration = (float) cost * 2.0f;
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
                duration = (float) cost * 2.0f;

                break;
            default:
                break;
        }

        if (ABILITY_COOLDOWN.isReady() && effectType != null) {
            // Use health for cost, absorption hearts should not count for balance reasons
            if (player.getHealth() <= cost) {
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 1.0f, 2.0f);

                return;
            }

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

        if (entity.isDead()) {
            return;
        }

        DeathMessageManager.setNextDeathMessage(damager.getUniqueId(), this);

        // Deal true damage to both

        if (state.get() != OverdriveState.ACTIVE) {
            return;
        }

        // State change

        state.set(OverdriveState.SPENT);

        TRUE_DAMAGE_COOLDOWN.reset();

        // Apply effect

        Player attacker = (Player) damager;
        Damageable victim = (Damageable) entity;

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
            ABILITY_COOLDOWN.reset();

            state.set(OverdriveState.SPENT);
        }
    }

    private double getHealthCost() {
        return (power / (double) MAX_POWER) * 20.0f;
    }

    private void createTicker(long delay, long interval, long duration) {
        AtomicBoolean active = new AtomicBoolean(true);
        AtomicInteger counter = new AtomicInteger();

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
            if (!active.get()) {
                return;
            }

            Player player = owner.getPlayer();

            if (player != null) {
                // Interrupt on player death
                if (player.isDead() || state.get() == OverdriveState.SPENT) {
                    active.set(false);

                    return;
                }

                float pitch = (counter.getAndIncrement() % 4 == 0) ? 1.5f : 1.0f;

                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);
            }
        }, delay, interval);

        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            active.set(false);

            Util.safeTaskCancel(task);
        }, delay + duration);
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

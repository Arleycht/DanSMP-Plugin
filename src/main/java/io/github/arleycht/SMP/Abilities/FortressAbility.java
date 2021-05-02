package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.CombatHelper;
import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.text.MessageFormat;

public class FortressAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 5L;
    public static final double MAX_ARMOR_HEALTH = 10.0;
    public static final long ARMOR_REGEN_INTERVAL_TICKS = 5L * 20L;
    public static final double ATTACK_SPEED_MUL = -0.25;
    public static final double ABILITY_RANGE = 16.0;
    public static final double ABILITY_RANGE_SQUARED = ABILITY_RANGE * ABILITY_RANGE;
    public static final long ABILITY_DURATION_TICKS = 10L * 20L;

    private final Cooldown HIT_COOLDOWN = new Cooldown(10.0);
    private final Cooldown ABILITY_COOLDOWN = new Cooldown(60.0);

    private double armorHealth = MAX_ARMOR_HEALTH;
    private boolean active = false;

    private boolean reducedAttackSpeed = false;
    private boolean wasRegenerating = true;
    private boolean tickPhase = false;

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
        Player player = owner.getPlayer();

        if (armorHealth <= 0.0) {
            clearAttributeModifiers();

            reducedAttackSpeed = false;

            if (player != null) {
                Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));

                player.playSound(player.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, tickPhase ? 1.1f : 1.0f);

                tickPhase = !tickPhase;
            }
        } else if (!reducedAttackSpeed) {
            reducedAttackSpeed = true;

            addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, ATTACK_SPEED_MUL, Operation.MULTIPLY_SCALAR_1);
        }

        if (HIT_COOLDOWN.isNotReady()) {
            return;
        }

        if (!wasRegenerating && player != null) {
            player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.8f);
        }

        wasRegenerating = true;

        double rate = MAX_ARMOR_HEALTH * TASK_INTERVAL_TICKS / (double) ARMOR_REGEN_INTERVAL_TICKS;

        armorHealth = Math.min(MAX_ARMOR_HEALTH, armorHealth + rate);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (isOwner(event.getDamager())) {
            return;
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (isOwner((Entity) projectile.getShooter())) {
                return;
            }
        }

        if (!isOwner(event.getEntity())) {
            if (active) {
                Player player = owner.getPlayer();

                if (player == null) {
                    return;
                }

                Entity entity = event.getDamager();

                if (entity.getLocation().distanceSquared(player.getLocation()) < ABILITY_RANGE_SQUARED && player.hasLineOfSight(entity)) {
                    event.setCancelled(true);

                    if (entity instanceof Player) {
                        Player other = (Player) entity;

                        String message = MessageFormat.format("{0}''s ability is nullifying your attacks!", owner.getRealName());

                        other.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                    }
                }
            }

            return;
        }

        Player player = (Player) event.getEntity();

        if (player.isBlocking()) {
            return;
        }

        HIT_COOLDOWN.reset();

        wasRegenerating = false;

        boolean hadArmor = armorHealth > 0.0;

        double finalDamage = CombatHelper.getFinalDamage(event.getDamage(), player, event.getCause());

        double leftover = finalDamage - armorHealth;

        armorHealth = Math.max(0.0, armorHealth - finalDamage);

        if (leftover <= 0.0) {
            float p = 1.0f - (float) (armorHealth / MAX_ARMOR_HEALTH);

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f - p);
        } else {
            // Absorb damage

            double newAbsorption = player.getAbsorptionAmount() - leftover;
            double dealtDamage = Math.max(0.0, -newAbsorption);

            // Apply absorption and health damage

            final double damageAmount = dealtDamage;

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                player.setAbsorptionAmount(Math.max(0.0, player.getAbsorptionAmount() - dealtDamage));
                player.setHealth(Math.max(0.0, player.getHealth() - damageAmount));
            }, 0L);

            // Play armor breaking sound

            if (hadArmor) {
                player.getWorld().playSound(player.getEyeLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
            }
        }

        String message = MessageFormat.format("Armor Health: {0}", armorHealth);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));

        event.setDamage(0.0);
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

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack heldItem = player.getInventory().getItem(EquipmentSlot.HAND);

        World world = player.getWorld();

        if (heldItem.getType() == Material.DIAMOND) {
            if (ABILITY_COOLDOWN.isNotReady()) {
                return;
            }

            ABILITY_COOLDOWN.reset();

            active = true;

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> active = false, ABILITY_DURATION_TICKS);

            for (Entity entity : player.getNearbyEntities(ABILITY_RANGE, ABILITY_RANGE, ABILITY_RANGE)) {
                if (entity instanceof Mob) {
                    Mob mob = (Mob) entity;

                    if (mob.getTarget() != null) {
                        if (mob.getLocation().distanceSquared(player.getLocation()) < ABILITY_RANGE_SQUARED) {
                            mob.setTarget(player);
                        }
                    }
                }
            }

            float durationSeconds = ABILITY_DURATION_TICKS * 0.05f;

            Util.applyEffect(player, PotionEffectType.DAMAGE_RESISTANCE, durationSeconds, 3);
            Util.applyEffect(player, PotionEffectType.GLOWING, durationSeconds, 0, false, false, false);

            world.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.25f);
        }
    }

    @Override
    public String getName() {
        return "Walking Fortress";
    }

    @Override
    public String getDescription() {
        return "You have a regenerable absorption.";
    }
}

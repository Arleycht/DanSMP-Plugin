package io.github.arleycht.SMP.Abilities;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DarkAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;

    public static final long BEGIN_TIME = 10800L;
    public static final long END_TIME = 1200L;
    public static final long ACTIVE_INTERVAL_TIME = Math.abs(END_TIME - BEGIN_TIME);

    public static final double DAMAGE_MULTIPLIER = 2.0;

    public static final String ACTIVE_MESSAGE = "You feel the moon begin to rise";
    public static final String INACTIVE_MESSAGE = "You feel the moon begin to set";

    private boolean active = false;

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

        if (player == null) {
            return;
        }

        World world = player.getWorld();

        boolean nowActive = Math.floorMod(world.getTime() - BEGIN_TIME, 24000L) < ACTIVE_INTERVAL_TIME;

        if (active != nowActive) {
            active = nowActive;

            if (active) {
                clearAttributeModifiers();

                addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, 2.0, AttributeModifier.Operation.ADD_SCALAR);

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ACTIVE_MESSAGE));
            } else {
                clearAttributeModifiers();

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(INACTIVE_MESSAGE));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();

        if (attacker instanceof Projectile) {
            // Get entity that shot the projectile
            attacker = (Entity) ((Projectile) attacker).getShooter();
        }

        // Apply effect when active and attacker is owner,
        // or when inactive and victim is owner
        boolean applyEffect = active ? isOwner(attacker) : isOwner(victim);

        if (applyEffect) {
            event.setDamage(event.getFinalDamage() * DAMAGE_MULTIPLIER);
        }
    }

    @Override
    public String getName() {
        return "Dark Embrace";
    }

    @Override
    public String getDescription() {
        return "You are twice damaging in night, and twice damaged in day.";
    }
}

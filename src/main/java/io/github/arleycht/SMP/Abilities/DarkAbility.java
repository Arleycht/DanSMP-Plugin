package io.github.arleycht.SMP.Abilities;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DarkAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 20L;

    public static final long BEGIN_TIME = 10800L;
    public static final long END_TIME = 1200L;
    public static final long ACTIVE_INTERVAL_TIME = Math.abs(END_TIME - BEGIN_TIME);

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
        Player player = Bukkit.getPlayer(owner.getUniqueId());

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

        if (!(attacker instanceof Player) || !(victim instanceof Player)) {
            return;
        }

        if (active && isOwner(attacker)) {
            event.setDamage(event.getFinalDamage() * 2.0);
        } else if (!active && isOwner(victim)) {
            event.setDamage(event.getFinalDamage() * 2.0);
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

package io.github.arleycht.SMP.Abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SkyWandererAbility extends Ability {
    public static final double ADD_HEALTH = -4.0;

    @Override
    public void initialize() {
        AttributeModifier.Operation add = AttributeModifier.Operation.ADD_NUMBER;

        addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, ADD_HEALTH, add);
    }

    @EventHandler
    public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && player.isSneaking() && !player.isOnGround()) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onEntityToggleGlideEvent(EntityToggleGlideEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (isOwner(player) && !player.isOnGround() && !player.isFlying()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getName() {
        return "Sky's the Limit";
    }

    @Override
    public String getDescription() {
        return "A nomadic lifestyle of reduced health from the wears of travel.";
    }
}

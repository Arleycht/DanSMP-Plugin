package io.github.arleycht.SMP.Abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SkyWandererAbility extends Ability {
    protected boolean gliding = false;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack heldItemStack = player.getInventory().getItem(hand);
        Material heldItemType = heldItemStack.getType();

        gliding = !gliding;

        if (hand == EquipmentSlot.HAND && heldItemType == Material.STICK) {
            player.setGliding(gliding);
        }
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (event.isGliding() != gliding) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getName() {
        return "Sky is the Limit";
    }

    @Override
    public String getDescription() {
        return "A nomadic lifestyle of reduced health from the wears of travel.";
    }
}

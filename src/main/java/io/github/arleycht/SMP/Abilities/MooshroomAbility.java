package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MooshroomAbility extends Ability {
    // TODO: Produce suspiscious "milk"
    // TODO: Grow BIG mushrooms on the spot or something
    public static final Cooldown MILK_COOLDOWN = new Cooldown(60.0);
    public static final Cooldown STEW_COOLDOWN = new Cooldown(90.0);

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && player.isSneaking()) {
            activateAbility(player);
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity targetEntity = event.getRightClicked();

        if (isOwner(targetEntity) && targetEntity instanceof Player) {
            activateAbility(player);
        }
    }

    private void activateAbility(Player player) {
        ItemStack heldItem = player.getInventory().getItem(EquipmentSlot.HAND);
        ItemStack gainedItem;

        switch (heldItem.getType()) {
            case BUCKET:
                if (MILK_COOLDOWN.isNotReady()) {
                    return;
                }

                MILK_COOLDOWN.reset();

                gainedItem = new ItemStack(Material.MILK_BUCKET);

                break;
            case BOWL:
                if (STEW_COOLDOWN.isNotReady()) {
                    return;
                }

                STEW_COOLDOWN.reset();

                gainedItem = new ItemStack(Material.MUSHROOM_STEW);

                break;
            default:
                return;
        }

        heldItem.setAmount(heldItem.getAmount() - 1);

        HashMap<Integer, ItemStack> excess = player.getInventory().addItem(gainedItem);

        for (Map.Entry<Integer, ItemStack> entry : excess.entrySet()) {
            player.getWorld().dropItem(player.getLocation(), entry.getValue());
        }
    }
}

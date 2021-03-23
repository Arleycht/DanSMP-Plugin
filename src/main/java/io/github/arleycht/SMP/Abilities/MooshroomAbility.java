package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class MooshroomAbility extends Ability {
    // TODO: Produce suspicious "milk"
    // TODO: Grow BIG mushrooms on the spot or something
    private final Cooldown MILK_COOLDOWN = new Cooldown(60.0);
    private final Cooldown STEW_COOLDOWN = new Cooldown(90.0);

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isOwner(player) && player.isSneaking()) {
            activateAbility(player, player);
        }
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity targetEntity = event.getRightClicked();

        if (isOwner(targetEntity) && targetEntity instanceof Player) {
            activateAbility(player, (Player) targetEntity);
        }
    }

    private void activateAbility(Player player, Player target) {
        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND);

        Sound interactionSound;
        ItemStack gainedItem;

        switch (heldItem.getType()) {
            case BUCKET:
                if (MILK_COOLDOWN.isNotReady()) {
                    return;
                }

                MILK_COOLDOWN.reset();

                interactionSound = Sound.ENTITY_COW_MILK;
                gainedItem = new ItemStack(Material.MILK_BUCKET);

                break;
            case BOWL:
                if (STEW_COOLDOWN.isNotReady()) {
                    return;
                }

                STEW_COOLDOWN.reset();

                interactionSound = Sound.ENTITY_MOOSHROOM_MILK;
                gainedItem = new ItemStack(Material.MUSHROOM_STEW);

                break;
            default:
                return;
        }

        int heldAmount = heldItem.getAmount();

        if (heldAmount <= 1 || player.getGameMode() == GameMode.CREATIVE) {
            inventory.setItem(EquipmentSlot.HAND, gainedItem);
        } else {
            heldItem.setAmount(heldAmount - 1);

            for (Map.Entry<Integer, ItemStack> entry : inventory.addItem(gainedItem).entrySet()) {
                player.getWorld().dropItem(player.getLocation(), entry.getValue());
            }
        }

        target.getWorld().playSound(target.getLocation(), interactionSound, 1.0f, 1.0f);
    }

    @Override
    public String getName() {
        return "Literally a Mooshroom";
    }

    @Override
    public String getDescription() {
        return "Produce milk and stew like a moo-moo.";
    }
}

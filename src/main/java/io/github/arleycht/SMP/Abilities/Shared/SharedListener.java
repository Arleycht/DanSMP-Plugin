package io.github.arleycht.SMP.Abilities.Shared;

import io.github.arleycht.SMP.Abilities.Ability;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.UUID;

public class SharedListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!WaterAllergyManager.isAllergic(player)) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItem(EquipmentSlot.HAND);

        if (WaterAllergyManager.isValidProtection(heldItem)) {
            if (inventory.getHelmet() == null) {
                ItemStack helmet = new ItemStack(heldItem.getType(), 1);

                Util.decrementItemStack(heldItem, 1);

                inventory.setItem(EquipmentSlot.HEAD, helmet);

                player.getWorld().playSound(player.getEyeLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (WaterAllergyManager.isAllergic(player) && itemStack.getType() == Material.POTION) {
            ItemMeta meta = itemStack.getItemMeta();

            if (!(meta instanceof PotionMeta)) {
                return;
            }

            PotionType potionType = ((PotionMeta) meta).getBasePotionData().getType();

            if (potionType == PotionType.WATER || potionType == PotionType.MUNDANE || potionType == PotionType.AWKWARD) {
                UUID uuid = player.getUniqueId();
                Ability[] abilities = WaterAllergyManager.getAbilities(uuid);

                if (abilities.length <= 0) {
                    // This shouldn't happen, but it'll be obvious if it did during runtime
                    return;
                }

                Ability ability = abilities[Util.nextIntRange(0, abilities.length)];

                DeathMessageManager.setNextDeathMessage(uuid, ability);

                Util.dealTrueDamage(player, player.getHealth());
            }
        }
    }
}

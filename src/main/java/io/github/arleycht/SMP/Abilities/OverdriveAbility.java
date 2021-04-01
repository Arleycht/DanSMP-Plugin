package io.github.arleycht.SMP.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class OverdriveAbility extends Ability {
    // TODO: Speed, Damage, Haste, True Damage

    /*
        Haste I -> 1.0 / s
        Haste II ->
        Speed
     */

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        EquipmentSlot hand = event.getHand();

        if (hand == null) {
            return;
        }

        ItemStack item = player.getInventory().getItem(hand);

        switch (item.getType()) {
            case MUSIC_DISC_CAT:
                Bukkit.getLogger().info("Overdrive CAT");
            case MUSIC_DISC_WAIT:
                Bukkit.getLogger().info("Overdrive WAIT");
            default:
                break;
        }
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

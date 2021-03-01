package io.github.arleycht.SMP.Abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SkyWandererAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 5L;

    private ItemStack originalItem;
    private int originalElytraSlot;

    private boolean wasOnGround = false;

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

        if (player != null) {
            boolean isOnGround = player.isOnGround();

            if (wasOnGround != isOnGround) {
                wasOnGround = isOnGround;

                if (isOnGround) {
                    unequipElytra(player);
                } else {
                    equipElytra(player);
                }
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

    private int getElytraSlot(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); ++i) {
            ItemStack item = inventory.getItem(i);

            if (item != null && item.getType() == Material.ELYTRA) {
                return i;
            }
        }

        return -1;
    }

    private void equipElytra(Player player) {

    }

    private void unequipElytra(Player player) {

    }
}

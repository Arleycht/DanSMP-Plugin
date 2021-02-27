package io.github.arleycht.SMP.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

public class CreepyManAbility extends Ability {
    // Gunpowder generation interval in milliseconds
    // This is in realtime because it would be terrible to wait
    // several minutes longer for these absolutely insane rates
    public static final long GENERATION_INTERVAL_MS = 1000L * 60L * 20L;
    // Check every 60 seconds
    protected static final long CHECK_INTERVAL_TICKS = 20L * 60L;

    protected long lastGenerationTime;

    public CreepyManAbility() {
        lastGenerationTime = System.currentTimeMillis();
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    @Override
    public long getTaskIntervalTicks() {
        return CHECK_INTERVAL_TICKS;
    }

    @Override
    public void run() {
        if (owner == null) {
            return;
        }

        if (System.currentTimeMillis() - lastGenerationTime > GENERATION_INTERVAL_MS) {
            Player player = Bukkit.getPlayer(owner.getUniqueId());

            if (player == null) {
                return;
            }

            ItemStack gunpowder = new ItemStack(Material.GUNPOWDER);

            // ONE SINGULAR GUNPOWDER
            gunpowder.setAmount(1);

            player.getInventory().addItem(gunpowder);

            lastGenerationTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();

        // Make creepers ignore the ability owner
        if (isOwner(target.getUniqueId()) && entity instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getName() {
        return "« Explosive Origins »";
    }

    @Override
    public String getDescription() {
        return "Friend of creepers, enemy of cats. Explodes on command!";
    }
}

package io.github.arleycht.SMP.Abilities;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;

public class CreepyManAbility extends Ability {
    protected long lastGenerationTime;

    public CreepyManAbility() {
        super();
        
        lastGenerationTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();

        // Ignored by creepers
        if (isOwner(target) && entity instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getName() {
        return "Explosive Origins";
    }

    @Override
    public String getDescription() {
        return "Friend of creepers, enemy of cats. Explodes on command!";
    }
}

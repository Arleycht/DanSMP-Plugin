package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GhostAbility extends Ability {
    public static final int MAX_PHASE_DISTANCE = 8;
    public static final int MAX_HEIGHT_CORRECTION = 5;

    private final Cooldown ABILITY_COOLDOWN = new Cooldown(5.0);

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!isOwner(player)) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack handItem = event.getItem();

        if (handItem == null || handItem.getType() != Material.GHAST_TEAR) {
            return;
        }

        if (ABILITY_COOLDOWN.isNotReady()) {
            return;
        }

        // Find first available location

        Block block = event.getClickedBlock();
        BlockFace face = event.getBlockFace();

        if (block == null || !block.getType().isSolid()) {
            return;
        }

        Vector moveDirection = face.getDirection().multiply(-1.0);

        Location firstValidLocation = null;

        for (int i = 0; i < MAX_PHASE_DISTANCE; ++i) {
            Vector offset = moveDirection.clone().multiply(i);
            Block obstacle = block.getLocation().clone().add(offset).getBlock();

            if (!obstacle.getType().isSolid()) {
                firstValidLocation = obstacle.getLocation();

                break;
            }
        }

        if (firstValidLocation == null) {
            return;
        }

        // Height correction

        for (int i = 0; i < MAX_HEIGHT_CORRECTION; ++i) {
            Location below = firstValidLocation.clone().add(0.0, -1.0, 0.0);

            if (!below.getBlock().getType().isSolid()) {
                firstValidLocation.add(0.0, -1.0, 0.0);
            }
        }

        Location below = firstValidLocation.clone().add(0.0, -1.0, 0.0);

        if (!below.getBlock().getType().isSolid()) {
            return;
        }

        firstValidLocation.add(0.5, 0.5, 0.5);
        firstValidLocation.setDirection(player.getLocation().getDirection());

        ABILITY_COOLDOWN.reset();

        World world = player.getWorld();

        world.playSound(player.getLocation(), Sound.PARTICLE_SOUL_ESCAPE, 1.5f, Util.nextFloatRange(0.5f, 1.1f));
        world.playSound(firstValidLocation, Sound.PARTICLE_SOUL_ESCAPE, 1.5f, Util.nextFloatRange(0.5f, 1.1f));

        player.teleport(firstValidLocation);
    }

    @Override
    public String getName() {
        return "Literally a Ghost";
    }

    @Override
    public String getDescription() {
        return "F";
    }
}

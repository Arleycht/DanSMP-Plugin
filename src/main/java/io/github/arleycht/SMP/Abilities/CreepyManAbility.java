package io.github.arleycht.SMP.Abilities;

import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    protected PacketAdapter packetAdapter;

    @Override
    public void initialize() {
        lastGenerationTime = System.currentTimeMillis();

        if (packetAdapter != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
        }

        packetAdapter = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player receivingPlayer = event.getPlayer();
                World world = receivingPlayer.getWorld();
                WrapperPlayServerNamedSoundEffect soundPacket = new WrapperPlayServerNamedSoundEffect(packet);
                String soundName = soundPacket.getSoundEffect().name().toLowerCase();

                // If the sound is going to the owner, we can ignore this packet
                if (isOwner(receivingPlayer.getUniqueId())) {
                    return;
                }

                if (!soundName.contains("step")) {
                    return;
                }

                double x = soundPacket.getEffectPositionX() / 8.0;
                double y = soundPacket.getEffectPositionY() / 8.0;
                double z = soundPacket.getEffectPositionZ() / 8.0;

                Location soundLocation = new Location(world, x, y, z);

                Player closestPlayer = null;
                double closestDistanceSquared = Double.MAX_VALUE;

                for (Player p : world.getPlayers()) {
                    double distanceSquared = soundLocation.distanceSquared(p.getLocation());

                    if (distanceSquared < closestDistanceSquared) {
                        closestPlayer = p;
                        closestDistanceSquared = distanceSquared;
                    }
                }

                if (closestPlayer != null && isOwner(closestPlayer.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
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

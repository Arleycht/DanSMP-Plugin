package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.Shared.DeathMessageManager;
import io.github.arleycht.SMP.util.Cooldown;
import io.github.arleycht.SMP.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CreeperAbility extends Ability {
    public static final long TASK_INTERVAL_TICKS = 60L * 20L;

    // Your ability: self-destruction
    public static final Material ABILITY_ITEM = Material.GUNPOWDER;
    public static final long ABILITY_DELAY_TICKS = 15L;
    public static final double ABILITY_SCARE_RANGE = 8.0;
    public static final long ABILITY_DELAY_SCARED_TICKS = 30L;
    public static final String[] ABILITY_DEATH_MESSAGES = {
            "{0} used Self Destruct!\nIt was super effective!",
            "{0}''s insides became outsides",
            "{0} went boom",
            "{0} had a short fuse",
            "{0} perished in smoke and flames",
    };

    private final Cooldown GENERATION_COOLDOWN = new Cooldown(20.0 * 60.0);

    //private PacketAdapter packetAdapter;

    @Override
    public void initialize() {
        GENERATION_COOLDOWN.reset();

        DeathMessageManager.setDeathMessages(this, ABILITY_DEATH_MESSAGES);

        /*if (packetAdapter != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
        }

        packetAdapter = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player receivingPlayer = event.getPlayer();
                WrapperPlayServerNamedSoundEffect soundPacket = new WrapperPlayServerNamedSoundEffect(packet);
                String soundName = soundPacket.getSoundEffect().name().toLowerCase();

                // If the sound is going to the owner, we can ignore this packet
                if (isOwner(receivingPlayer)) {
                    return;
                }

                if (!soundName.contains("step")) {
                    return;
                }

                World world = receivingPlayer.getWorld();

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

                if (closestPlayer != null && isOwner(closestPlayer)) {
                    soundPacket.setVolume(soundPacket.getVolume() * 0.5f);

                    event.setPacket(soundPacket.getHandle());
                }
            }
        };

        ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);*/
    }

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
        if (GENERATION_COOLDOWN.isNotReady()) {
            return;
        }

        Player player = owner.getPlayer();

        if (player == null) {
            return;
        }

        Util.giveItem(player, Material.GUNPOWDER, 1);

        GENERATION_COOLDOWN.reset();
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();

        if (target == null) {
            return;
        }

        // Make creepers ignore the ability owner
        if (isOwner(target) && entity instanceof Creeper) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack heldItemStack = player.getInventory().getItem(EquipmentSlot.HAND);
        Material heldItemType = heldItemStack.getType();

        if (!isOwner(player) || heldItemType != ABILITY_ITEM) {
            return;
        }

        boolean scared = false;

        for (Entity entity : player.getNearbyEntities(ABILITY_SCARE_RANGE, ABILITY_SCARE_RANGE, ABILITY_SCARE_RANGE)) {
            if (entity instanceof Cat || entity instanceof Ocelot) {
                scared = true;

                break;
            }
        }

        long delayTicks = scared ? ABILITY_DELAY_SCARED_TICKS : ABILITY_DELAY_TICKS;
        float pitch = scared ? 1.0f : 2.0f;
        float explosionPower = 6.0f;

        World world = player.getWorld();

        world.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, pitch);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.getHealth() <= 0.0) {
                return;
            }

            // It's about the principle, the item must be consumed!
            heldItemStack.setAmount(heldItemStack.getAmount() - 1);

            DeathMessageManager.setNextDeathMessage(player.getUniqueId(), this);

            world.createExplosion(player.getLocation(), explosionPower, false, true, player);
            player.setHealth(0.0);
        }, delayTicks);
    }

    @Override
    public String getName() {
        return "Explosive Origins";
    }

    @Override
    public String getDescription() {
        return "Friend of creepers, enemy of cats, quieter footsteps. Use gunpowder to explode on command!";
    }
}

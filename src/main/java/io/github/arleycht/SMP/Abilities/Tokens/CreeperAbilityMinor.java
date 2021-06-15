package io.github.arleycht.SMP.Abilities.Tokens;

public class CreeperAbilityMinor extends MinorAbility {
    //private PacketAdapter packetAdapter;

    @Override
    public void initialize() {
        /*packetAdapter = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
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
    public void destroy() {
        //ProtocolLibrary.getProtocolManager().removePacketListener(packetAdapter);
    }

    @Override
    public String getName() {
        return "Minor Explosive Origins";
    }

    @Override
    public String getDescription() {
        return "Your footsteps are quieter.";
    }
}

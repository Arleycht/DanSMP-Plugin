package io.github.arleycht.SMP.Characters;

import java.util.UUID;

class ActorRegistryTest {
    private String testUsername = "Arleycht";
    private UUID testUUID = UUID.fromString("99ff480d-e626-4065-a5c7-6cc89354eda4");

    @org.junit.jupiter.api.Test
    void getUsernameFromUUID() {
        String username = ActorRegistry.getUsernameFromUUID(testUUID);

        System.out.println(username);

        assert(username != null);
        assert(username.equalsIgnoreCase(testUsername));
    }

    @org.junit.jupiter.api.Test
    void getUUIDFromUsername() {
        UUID uuid = ActorRegistry.getUUIDFromUsername(testUsername);

        System.out.println(String.valueOf(uuid));

        assert(uuid != null);
        assert(uuid.equals(testUUID));
    }
}
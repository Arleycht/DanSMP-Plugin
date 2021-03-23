package io.github.arleycht.SMP.Abilities.DeathMessage;

import io.github.arleycht.SMP.Abilities.Ability;
import io.github.arleycht.SMP.util.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.text.MessageFormat;
import java.util.*;

public final class DeathMessageManager implements Listener {
    // Player should die
    public static final long MESSAGE_TIMEOUT_MS = 3L * 1000L;

    private static final DeathMessageManager INSTANCE;

    private static final HashMap<Ability, List<String>> REGISTERED_MESSAGES = new HashMap<>();

    private static final HashMap<UUID, Pair<Ability, Long>> NEXT_MESSAGES = new HashMap<>();

    private static final Random RANDOM = new Random();

    static {
        INSTANCE = new DeathMessageManager();
    }

    private DeathMessageManager() {

    }

    public static DeathMessageManager getInstance() {
        return INSTANCE;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        Pair<Ability, Long> pair = NEXT_MESSAGES.get(uuid);

        if (pair != null) {
            NEXT_MESSAGES.remove(uuid);

            Ability ability = pair.getKey();
            long registrationTimeMs = pair.getValue();

            if (System.currentTimeMillis() - registrationTimeMs > MESSAGE_TIMEOUT_MS) {
                return;
            }

            List<String> messages = REGISTERED_MESSAGES.get(ability);

            if (messages != null && messages.size() > 0) {
                Player victim = event.getEntity();

                int i = RANDOM.nextInt(messages.size());
                String message = MessageFormat.format(messages.get(i), victim.getName());

                event.setDeathMessage(message);
            }
        }
    }

    public static void setDeathMessages(Ability ability, List<String> messages) {
        REGISTERED_MESSAGES.put(ability, messages);
    }

    public static void setDeathMessages(Ability ability, String[] messages) {
        REGISTERED_MESSAGES.put(ability, Arrays.asList(messages));
    }

    public static void setNextDeathMessage(UUID playerUuid, Ability ability) {
        NEXT_MESSAGES.put(playerUuid, new Pair<>(ability, System.currentTimeMillis()));
    }
}

package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Abilities.Shared.WaterAllergyManager;
import io.github.arleycht.SMP.Characters.Actor;
import io.github.arleycht.SMP.Characters.ActorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class AbilityRegistry {
    public static final String ABILITY_ATTRIBUTE_MODIFIER_NAME = "Ability Modifier";

    private static final long ABILITY_ATTRIBUTE_CHECK_INTERVAL = 5L * 20L;
    private static final ArrayList<Ability> ABILITIES = new ArrayList<>();
    private static final HashMap<Ability, BukkitTask> ABILITY_BUKKIT_TASK_MAP = new HashMap<>();

    private static Plugin plugin;

    private AbilityRegistry() {

    }

    public static final class AbilityAttributeEventListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            //Bukkit.getLogger().info("Player join event!");

            applyModifiers(event.getPlayer());
        }

        @EventHandler
        public void onRespawn(PlayerRespawnEvent event) {
            //Bukkit.getLogger().info("Player respawn event!");

            applyModifiers(event.getPlayer());
        }

        @EventHandler
        public void onAbilityAttributeEvent(AbilityAttributeEvent event) {
            applyModifiers(event.getPlayer());
        }

        private void applyModifiers(Player player) {
            if (player == null) {
                return;
            }

            for (Attribute attribute : Attribute.values()) {
                AttributeInstance attributeInstance = player.getAttribute(attribute);

                if (attributeInstance == null) {
                    continue;
                }

                for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                    if (!modifier.getName().equalsIgnoreCase(ABILITY_ATTRIBUTE_MODIFIER_NAME)) {
                        continue;
                    }

                    attributeInstance.removeModifier(modifier);
                }

                for (Ability ability : ABILITIES) {
                    if (!ability.isOwner(player)) {
                        continue;
                    }

                    AttributeModifier[] modifiers = ability.getAttributeModifiers(attribute);

                    for (AttributeModifier modifier : modifiers) {
                        attributeInstance.addModifier(modifier);
                    }
                }
            }
        }
    }

    public static <T extends Ability> void registerAbility(String realName, Class<T> abilityClass, Plugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("AbilityRegistry plugin must be set before registering abilities!");
        }

        if (realName == null) {
            throw new IllegalArgumentException("A valid realName must be provided!");
        }

        Actor actor = ActorRegistry.getActorFromRealName(realName);

        if (actor == null) {
            String msg = "'%s' is not a registered name!";
            throw new IllegalArgumentException(String.format(msg, realName));
        }

        T ability;

        try {
            ability = abilityClass.newInstance();
            ability.setPlugin(plugin);
            ability.setOwner(actor);

            Bukkit.getServer().getPluginManager().registerEvents(ability, plugin);

            ABILITIES.add(ability);

            ability.initialize();

            String msg = "Registered ability '%s' to '%s'";
            Bukkit.getLogger().info(String.format(msg, ability.getName(), realName));

        } catch (Exception e) {
            e.printStackTrace();

            String msg = "Failed to instantiate ability '%s'";
            Bukkit.getLogger().severe(String.format(msg, abilityClass.toString()));
        }
    }

    public static Ability[] getRegisteredAbilities() {
        return ABILITIES.toArray(new Ability[0]);
    }

    public static Ability[] getAbilities(UUID ownerUuid) {
        ArrayList<Ability> abilities = new ArrayList<>();

        for (Ability ability : ABILITIES) {
            if (ability.isOwner(ownerUuid)) {
                abilities.add(ability);
            }
        }

        return abilities.toArray(new Ability[0]);
    }

    public static Ability[] getAbilities(Player player) {
        return getAbilities(player.getUniqueId());
    }

    public static <T extends Ability> Ability getAbility(UUID ownerUuid, Class<T> abilityClass) {
        for (Ability ability : ABILITIES) {
            if (abilityClass.isInstance(ability) && ability.isOwner(ownerUuid)) {
                return ability;
            }
        }

        return null;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static void setPlugin(Plugin plugin) {
        if (AbilityRegistry.plugin != null) {
            Bukkit.getLogger().warning("Plugin was already defined in AbilityRegistry!");

            // Cancel old tasks
            cancelAllAbilityTasks();

            // Set new plugin
            AbilityRegistry.plugin = plugin;

            // Reschedule tasks to new plugin
            scheduleAllAbilityTasks();

            return;
        }

        AbilityRegistry.plugin = plugin;

        // Ability attribute events

        plugin.getServer().getPluginManager().registerEvents(new AbilityAttributeEventListener(), plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Bukkit.getPluginManager().callEvent(new AbilityAttributeEvent(player));
            }
        }, 0L, ABILITY_ATTRIBUTE_CHECK_INTERVAL);

        // Water allergy

        WaterAllergyManager.initialize(plugin);
    }

    public static void scheduleAbilityTask(Ability ability) {
        if (ability == null) {
            throw new NullPointerException("Cannot schedule null ability!");
        }

        if (ability.isRunnable()) {
            BukkitTask task = ABILITY_BUKKIT_TASK_MAP.get(ability);

            if (task != null && !task.isCancelled()) {
                String msg = "Attempted to schedule '%s', but it's already scheduled!";
                Bukkit.getLogger().warning(String.format(msg, ability.getName()));

                return;
            }

            task = Bukkit.getScheduler().runTaskTimer(plugin, ability, 0L, ability.getTaskIntervalTicks());

            ABILITY_BUKKIT_TASK_MAP.put(ability, task);

            String msg = "Scheduled task for ability '%s'";
            Bukkit.getLogger().info(String.format(msg, ability.getName()));
        } else if (ability.getTaskIntervalTicks() != -1L) {
            String msg = "Ability '%s' has a non-zero task interval but is not runnable!";
            Bukkit.getLogger().warning(String.format(msg, ability.getName()));
        }
    }

    public static void cancelAbilityTask(Ability ability) {
        if (ABILITY_BUKKIT_TASK_MAP.containsKey(ability)) {
            ABILITY_BUKKIT_TASK_MAP.get(ability).cancel();
            ABILITY_BUKKIT_TASK_MAP.remove(ability);
        }
    }

    public static void scheduleAllAbilityTasks() {
        for (Ability a : ABILITIES) {
            scheduleAbilityTask(a);
        }

        Bukkit.getLogger().info("Scheduled all ability tasks");
    }

    public static void cancelAllAbilityTasks() {
        for (Map.Entry<Ability, BukkitTask> e : ABILITY_BUKKIT_TASK_MAP.entrySet()) {
            e.getValue().cancel();
        }

        ABILITY_BUKKIT_TASK_MAP.clear();

        Bukkit.getLogger().info("Cancelled all ability tasks");
    }
}

package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Characters.Actor;
import io.github.arleycht.SMP.Characters.ActorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class AbilityRegistry {
    private static final ArrayList<Ability> ABILITIES = new ArrayList<>();
    private static final HashMap<Ability, BukkitTask> ABILITY_BUKKIT_TASK_MAP = new HashMap<>();

    private static Plugin plugin;

    private AbilityRegistry() {

    }

    public static <T extends Ability> Ability registerAbility(String username, Class<T> t, Plugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("AbilityRegistry plugin must be set before registering abilities!");
        }

        if (username == null) {
            throw new NullPointerException("A valid username must be provided!");
        }

        Actor actor = ActorRegistry.getCharacterFromUsername(username);

        if (actor == null) {
            actor = ActorRegistry.addActor(null, username);

            String msg = "'%s' was not previously registered!";
            Bukkit.getLogger().warning(String.format(msg, username));
        }

        T ability;

        try {
            ability = t.newInstance();
            ability.setPlugin(plugin);
            ability.setOwner(actor);

            Bukkit.getServer().getPluginManager().registerEvents(ability, plugin);

            ABILITIES.add(ability);

            ability.initialize();

            String msg = "Registered ability '%s' to '%s'";
            Bukkit.getLogger().info(String.format(msg, ability.getName(), username));

            return ability;
        } catch (Exception e) {
            e.printStackTrace();

            String msg = "Failed to instantiate ability '%s'";
            Bukkit.getLogger().severe(String.format(msg, String.valueOf(t)));

            return null;
        }
    }

    public static Ability[] getRegisteredAbilities() {
        return (Ability[]) ABILITIES.toArray();
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
    }

    public static BukkitTask scheduleAbilityTask(Ability ability) {
        if (ability == null) {
            throw new NullPointerException("Cannot schedule null ability!");
        }

        if (ability.isRunnable()) {
            BukkitTask task = ABILITY_BUKKIT_TASK_MAP.get(ability);

            if (task != null && !task.isCancelled()) {
                String msg = "Attempted to schedule '%s', but it's already scheduled!";
                Bukkit.getLogger().warning(String.format(msg, ability.getName()));

                return task;
            }

            task = Bukkit.getScheduler().runTaskTimer(plugin, ability, 0L, ability.getTaskIntervalTicks());

            ABILITY_BUKKIT_TASK_MAP.put(ability, task);

            String msg = "Scheduled task for ability '%s'";
            Bukkit.getLogger().info(String.format(msg, ability.getName()));

            return task;
        } else if (ability.getTaskIntervalTicks() != -1L) {
            String msg = "Ability '%s' has a non-zero task interval but is not runnable!";
            Bukkit.getLogger().warning(String.format(msg, ability.getName()));
        }

        return null;
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

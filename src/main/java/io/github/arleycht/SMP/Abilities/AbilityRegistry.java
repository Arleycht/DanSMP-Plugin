package io.github.arleycht.SMP.Abilities;

import io.github.arleycht.SMP.Characters.Actor;
import io.github.arleycht.SMP.Characters.ActorRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public final class AbilityRegistry {
    private static final ArrayList<Ability> ABILITIES = new ArrayList<>();

    private AbilityRegistry() {

    }

    public static <T extends Ability> Ability registerAbility(String username, Class<T> t, Plugin plugin) {
        if (username == null) {
            Bukkit.getLogger().severe("Failed to register ability to null user");

            return null;
        }

        Actor actor = ActorRegistry.getCharacterFromUsername(username);

        if (actor == null) {
            actor = ActorRegistry.addActor(null, username);

            Bukkit.getLogger().warning(String.format("'%s' was not previously registered!", username));
        }

        T ability;

        try {
            ability = t.newInstance();
            ability.setOwner(actor);

            Bukkit.getServer().getPluginManager().registerEvents(ability, plugin);

            ABILITIES.add(ability);

            String msg = "Registered ability « %s » to '%s'";
            Bukkit.getLogger().info(String.format(msg, ability.getName(), username));

            return ability;
        } catch (Exception e) {
            e.printStackTrace();

            String msg = "Failed to instantiate ability of class '%s'";
            Bukkit.getLogger().severe(String.format(msg, String.valueOf(t.getClass())));

            return null;
        }
    }

    public static Ability[] getRegisteredAbilities() {
        return (Ability[]) ABILITIES.toArray();
    }
}

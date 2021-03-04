package io.github.arleycht.SMP.Characters;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.UUID;

public final class ActorRegistry {
	private static final ArrayList<Actor> ACTORS = new ArrayList<>();

	private static Plugin plugin = null;
	
	public static Actor addActor(String realName, String uuidString) {
		if (uuidString == null) {
			Bukkit.getLogger().severe("UUID was not provided!");

			return null;
		}

		UUID uuid = UUID.fromString(uuidString);

		// Warn if name is not provided
		if (realName == null) {
			Bukkit.getLogger().warning("Real name was not provided!");

			realName = Bukkit.getOfflinePlayer(uuid).getName();
		}

		Actor actor = new Actor(realName, uuid);

		Bukkit.getLogger().info(String.format("Adding character '%s'", actor.toString()));
		
		ACTORS.add(actor);

		return actor;
	}

	public static void setPlugin(Plugin plugin) {
		if (ActorRegistry.plugin != null) {
			Bukkit.getLogger().warning("Plugin was already defined in ActorRegistry!");
		}

		ActorRegistry.plugin = plugin;
	}

	public static Actor getActorFromUsername(String username) {
		for (Actor actor : ACTORS) {
			if (actor.getUsername().equalsIgnoreCase(username)) {
				return actor;
			}
		}

		return null;
	}

	public static Actor getActorFromRealName(String realName) {
		for (Actor actor : ACTORS) {
			if (actor.getRealName().equalsIgnoreCase(realName)) {
				return actor;
			}
		}

		return null;
	}

	public static Actor getActorFromUuid(UUID uuid) {
		for (Actor actor : ACTORS) {
			if (actor.getUniqueId().equals(uuid)) {
				return actor;
			}
		}

		return null;
	}
}

package io.github.arleycht.SMP.Characters;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ActorRegistry {
	private static final int REQUEST_LIMIT = 300;
	private static final long REQUEST_COUNT_RESET_INTERVAL_MS = 1000L * 60L * 10L;
	private static final String UUID_REQUEST_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
	private static final String NAME_REQUEST_URL = "https://api.mojang.com/user/profiles/%s/names";

	private static final HashMap<UUID, String> UUID_USERNAME_CACHE = new HashMap<>();
	private static final ArrayList<Actor> ACTORS = new ArrayList<>();

	private static Plugin plugin = null;
	private static int requestCount = 0;
	private static long requestResetTime = 0L;
	
	static class NameResponse {
		//public String name;
		public String id;
	}
	
	static class NameHistoryEntry {
		public String name;
		//public long changedToAt;
	}
	
	private ActorRegistry() {
		
	}
	
	public static Actor addActor(String realName, String username) {
		Actor actor = new Actor(realName, username);

		Bukkit.getLogger().info(String.format("Adding character '%s'", actor.toString()));
		
		// Warn if name is not provided
		if (realName == null) {
			Bukkit.getLogger().warning("Real name was not provided!");
		}
		
		ACTORS.add(actor);

		return actor;
	}

	public static void setPlugin(Plugin plugin) {
		if (ActorRegistry.plugin != null) {
			Bukkit.getLogger().warning("Plugin was already defined in ActorRegistry!");
		}

		ActorRegistry.plugin = plugin;
	}

	public static Actor getCharacterFromRealName(String realName) {
		for (Actor actor : ACTORS) {
			if (actor.getRealName().equalsIgnoreCase(realName)) {
				return actor;
			}
		}

		return null;
	}

	public static Actor getCharacterFromUsername(String username) {
		for (Actor actor : ACTORS) {
			if (actor.getUsername().equalsIgnoreCase(username)) {
				return actor;
			}
		}

		return null;
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static String hyphenateUUIDString(String uuidString) {
		if (uuidString == null) {
			return null;
		}

		return uuidString.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
	}

	public static String getUsernameFromUUID(UUID uuid) {
		if (uuid == null) {
			return null;
		}

		// Return cached result, if any
		if (UUID_USERNAME_CACHE.containsKey(uuid)) {
			return UUID_USERNAME_CACHE.get(uuid);
		}

		// Try Bukkit
		Player player = Bukkit.getPlayer(uuid);

		if (player != null) {
			UUID_USERNAME_CACHE.put(player.getUniqueId(), player.getName());

			return player.getName();
		}

		String url = String.format(NAME_REQUEST_URL, uuid.toString());
		String text = getResponseFromUrl(url);
		
		if (text != null)
		{
			Gson gson = new Gson();

			NameHistoryEntry[] response = gson.fromJson(text, NameHistoryEntry[].class);

			if (response.length > 0) {
				String username = response[response.length - 1].name;

				UUID_USERNAME_CACHE.put(uuid, username);

				return username;
			}
		}
		
		return null;
	}

	public static UUID getUUIDFromUsername(String username) {
		if (username == null) {
			return null;
		}

		// Return cached result, if any
		if (UUID_USERNAME_CACHE.containsValue(username)) {
			for (Map.Entry<UUID, String> e : UUID_USERNAME_CACHE.entrySet()) {
				if (username.equalsIgnoreCase(e.getValue())) {
					return e.getKey();
				}
			}
		}

		// Try Bukkit
		Player player = Bukkit.getPlayer(username);

		if (player != null) {
			UUID_USERNAME_CACHE.put(player.getUniqueId(), player.getName());

			return player.getUniqueId();
		}

		String url = String.format(UUID_REQUEST_URL, username);
		String text = getResponseFromUrl(url);
		
		if (text != null)
		{
			Gson gson = new Gson();
			
			NameResponse response = gson.fromJson(text, NameResponse.class);
			
			UUID uuid = UUID.fromString(hyphenateUUIDString(response.id));

			UUID_USERNAME_CACHE.put(uuid, username);

			return uuid;
		}
		
		return null;
	}
	
	private static String getResponseFromUrl(String url) {
		Bukkit.getLogger().info(String.format("Performing HTTP GET on '%s'", url));

		if (System.currentTimeMillis() >= requestResetTime) {
			requestResetTime = System.currentTimeMillis() + REQUEST_COUNT_RESET_INTERVAL_MS;

			requestCount = 0;
		}

		if (requestCount > REQUEST_LIMIT) {
			Bukkit.getLogger().warning("Sending too many requests! Rate limiting!");

			return null;
		}
		else {
			++requestCount;
		}

		try {
			URLConnection c = new URL(url).openConnection();
			
			StringBuilder sb = new StringBuilder();
			
			InputStream stream = c.getInputStream();
			InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

			final int bufferSize = 1024;
			final char[] buffer = new char[bufferSize];
			int bufferPos = 0;
			int bytesRead;

			while ((bytesRead = reader.read(buffer, bufferPos, bufferSize)) > 0) {
				sb.append(buffer, bufferPos, bytesRead);
			}
			
			return sb.toString();
		} catch (Exception e) {
			String msg = "Failed to get from URL %s";
			Bukkit.getLogger().warning(String.format(msg, url));
		}
		
		return null;
	}
}

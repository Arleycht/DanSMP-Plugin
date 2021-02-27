package io.github.arleycht.SMP.Characters;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public final class ActorRegistry {
	private static final String UUID_REQUEST_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
	private static final String NAME_REQUEST_URL = "https://api.mojang.com/user/profiles/%s/names";
	
	private static final ArrayList<Actor> ACTORS = new ArrayList<>();
	
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
		UUID uuid = getUUIDFromUsername(username);
		Actor actor = new Actor(realName, username);

		Bukkit.getLogger().info(String.format("Adding character '%s'", actor.toString()));
		
		// Warn if name is not provided
		if (realName == null) {
			Bukkit.getLogger().warning("Real name was not provided!");
		}
		
		// Verify username exists
		if (uuid == null) {
			String msg = String.format("Username '%s' not found!", username);
			Bukkit.getLogger().severe(msg);
		}
		
		ACTORS.add(actor);

		return actor;
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

		String url = String.format(NAME_REQUEST_URL, uuid.toString());
		String text = getResponseString(url);
		
		if (text != null)
		{
			Gson gson = new Gson();

			NameHistoryEntry[] response = gson.fromJson(text, NameHistoryEntry[].class);

			if (response.length > 0) {
				return response[response.length - 1].name;
			}
		}
		
		return null;
	}

	public static UUID getUUIDFromUsername(String username) {
		if (username == null) {
			return null;
		}

		String url = String.format(UUID_REQUEST_URL, username);
		String text = getResponseString(url);
		
		if (text != null)
		{
			Gson gson = new Gson();
			
			NameResponse response = gson.fromJson(text, NameResponse.class);
			
			return UUID.fromString(hyphenateUUIDString(response.id));
		}
		
		return null;
	}
	
	private static String getResponseString(String url) {
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

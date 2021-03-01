package io.github.arleycht.SMP.Characters;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Actor {
	public static final String NO_NAME = "<NO_NAME>";
	public static final String NO_USERNAME = "<NO_USER>";
	
	protected String realName;
	protected String username;
	
	public Actor() {
		realName = NO_NAME;
		username = NO_USERNAME;
	}
	
	public Actor(String realName, String username) {
		this.realName = realName != null ? realName : NO_NAME;
		this.username = username != null ? username : NO_USERNAME;
	}

	public Player getPlayer() {
		Player player = Bukkit.getPlayer(username);

		return player;
	}

	public UUID getUniqueId() {
		return ActorRegistry.getUUIDFromUsername(username);
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealName() {
		return realName;
	}

	public String getUsername() {
		return username;
	}

	public boolean equals(Actor other) {
		if (other == null) {
			return false;
		}

		return username.equalsIgnoreCase(other.getUsername()) || getUniqueId().equals(other.getUniqueId());
	}

	public String toString() {
		String s = "%s (%s)";
		return String.format(s, username, realName);
	}
}

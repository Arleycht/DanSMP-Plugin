package io.github.arleycht.SMP.Characters;

import java.util.UUID;

public class Actor {
	private static final String uuidRequestURL = "https://api.mojang.com/users/profiles/minecraft/%s";
	private static final String nameRequestURL = "https://api.mojang.com/user/profiles/%s/names";

	public static final String NO_NAME = "<NO_NAME>";
	public static final String NO_USERNAME = "<NO_USER>";
	
	private String realName;
	private String username;
	
	public Actor() {
		realName = NO_NAME;
		username = NO_USERNAME;
	}
	
	public Actor(String realName, String username) {
		this.realName = realName != null ? realName : NO_NAME;
		this.username = username != null ? username : NO_USERNAME;
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

		return getUniqueId().equals(other.getUniqueId());
	}

	public String toString() {
		String s = "%s (%s)";
		return String.format(s, username, realName);
	}
}

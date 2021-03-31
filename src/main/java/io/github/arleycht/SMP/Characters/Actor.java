package io.github.arleycht.SMP.Characters;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Actor {
	public static final String NO_REAL_NAME = "<NO_NAME>";
	
	protected final String realName;
	protected final String username;
	protected UUID ownerUuid;
	
	public Actor(String realName, UUID uuid) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

		this.realName = (realName != null) ? realName : NO_REAL_NAME;
		this.username = offlinePlayer.getName();
		this.ownerUuid = offlinePlayer.getUniqueId();
	}

	public UUID getUniqueId() {
		return ownerUuid;
	}

	public String getRealName() {
		return realName;
	}

	public String getUsername() {
		return username;
	}

	public @Nullable Player getPlayer() {
		return Bukkit.getPlayer(ownerUuid);
	}

	public boolean equals(Actor other) {
		if (other == null) {
			return false;
		}

		return getUniqueId().equals(other.getUniqueId());
	}

	public String toString() {
		if (realName.equals(NO_REAL_NAME)) {
			return username;
		}

		return realName;
	}
}

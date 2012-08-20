package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

public class NullHandler implements IPermissionsHandler {
	
	@Override
	public boolean hasPermission(final Player player, final String node) {
		return false;
	}
}

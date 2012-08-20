package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

public class SuperpermsHandler implements IPermissionsHandler {
	
	@Override
	public boolean hasPermission(final Player player, final String node) {
		return player.hasPermission(node);
	}
}


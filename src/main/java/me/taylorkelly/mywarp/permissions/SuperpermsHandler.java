package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

public class SuperpermsHandler implements IPermissionsHandler {
	
	@Override
	public boolean hasPermission(final Player player, final String node, boolean defaultPerm) {
		return player.hasPermission(node);
	}

	@Override
	public int getInteger(final Player player, final String node, final int defaultInt) {
		return defaultInt;
	}
}


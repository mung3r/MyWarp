package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class Permissions3Handler implements IPermissionsHandler {
	private final transient PermissionHandler handler;

	public Permissions3Handler(final Plugin permissionsPlugin) {
		handler = ((Permissions)permissionsPlugin).getHandler();
	}

	@Override
	public boolean hasPermission(final Player player, final String node) {
		return handler.has(player, node);
	}
}


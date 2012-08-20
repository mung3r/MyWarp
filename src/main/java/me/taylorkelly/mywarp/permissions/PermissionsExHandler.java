package me.taylorkelly.mywarp.permissions;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExHandler implements IPermissionsHandler {
	private final transient PermissionManager manager;

	public PermissionsExHandler() {
		manager = PermissionsEx.getPermissionManager();
	}

	@Override
	public boolean hasPermission(final Player player, final String node) {
		String playername = player.getName();
		String playerworld = player.getWorld().getName();
		return manager.has(playername, node, playerworld);
	}
}

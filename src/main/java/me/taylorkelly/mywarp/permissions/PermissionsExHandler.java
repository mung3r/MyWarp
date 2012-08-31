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
		return manager.has(player.getName(), node, player.getWorld().getName());
	}

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return manager.getUser(player.getName()).inGroup(group, player.getWorld().getName());
    }
}

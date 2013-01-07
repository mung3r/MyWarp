package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Handler for PermissionsEx
 * 
 */
public class PermissionsExHandler implements PermissionsHandler {
    private final transient PermissionManager manager;

    public PermissionsExHandler() {
        manager = PermissionsEx.getPermissionManager();
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return manager.getUser(player.getName()).inGroup(group,
                player.getWorld().getName());
    }
}

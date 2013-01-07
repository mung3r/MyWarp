package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;

/**
 * Handler for GroupManager
 * 
 */
public class GroupManagerHandler implements PermissionsHandler {
    private final transient GroupManager manager;

    public GroupManagerHandler(final Plugin permissionsPlugin) {
        manager = ((GroupManager) permissionsPlugin);
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        AnjoPermissionsHandler handler = manager.getWorldsHolder()
                .getWorldPermissions(player);
        return handler.inGroup(player.getName(), group);
    }
}

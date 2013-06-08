package me.taylorkelly.mywarp.permissions;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Handler for GroupManager
 * 
 */
public class GroupManagerHandler implements PermissionsHandler {
    private final GroupManager manager;

    public GroupManagerHandler(final Plugin permissionsPlugin) {
        manager = ((GroupManager) permissionsPlugin);
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return manager.getWorldsHolder().getWorldPermissions(player)
                .inGroup(player.getName(), group);
    }
}

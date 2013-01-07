package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

/**
 * Handler for SuperPerms
 * 
 */
public class SuperpermsHandler implements PermissionsHandler {

    @Override
    public boolean playerHasGroup(Player player, String group) {
        // instead of returning false we check for a specific group permission
        return player.hasPermission("mywarp.group." + group);
    }
}

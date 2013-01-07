package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

/**
 * Dummy handler, will return false no matter what
 * 
 */
public class NullHandler implements PermissionsHandler {

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return false;
    }
}

package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;
/**
 * Interface for permission handlers
 *
 */
public interface PermissionsHandler {
    /**
     * Returns if a specific player is in a group or not
     * 
     * @param player
     *            the player
     * @param group
     *            the groupname
     * @return
     */
    boolean playerHasGroup(Player player, String group);
}

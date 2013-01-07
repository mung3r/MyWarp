package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
/**
 * Handler for bPermissions2
 *
 */
public class BPermissions2Handler implements PermissionsHandler {

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return ApiLayer.hasGroupRecursive(player.getWorld().getName(), CalculableType.USER, player.getName(), group);
    }
}

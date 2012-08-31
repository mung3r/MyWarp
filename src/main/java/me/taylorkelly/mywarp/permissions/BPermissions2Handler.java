package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

public class BPermissions2Handler implements IPermissionsHandler {

    @Override
    public boolean hasPermission(Player player, String node) {
        return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), node);
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return ApiLayer.hasGroupRecursive(player.getWorld().getName(), CalculableType.USER, player.getName(), group);
    }

}

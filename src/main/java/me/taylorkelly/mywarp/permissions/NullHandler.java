package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

public class NullHandler implements IPermissionsHandler {

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return false;
    }
}

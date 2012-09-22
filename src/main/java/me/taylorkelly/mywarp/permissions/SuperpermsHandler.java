package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

public class SuperpermsHandler implements IPermissionsHandler {
	
    @Override
    public boolean playerHasGroup(Player player, String group) {
        return player.hasPermission("mywarp.group." + group);
    }
}


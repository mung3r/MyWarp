package me.taylorkelly.mywarp.permissions;

import org.bukkit.entity.Player;

public interface IPermissionsHandler {
    boolean playerHasGroup(Player player, String group);
}

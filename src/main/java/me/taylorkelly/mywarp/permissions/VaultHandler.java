package me.taylorkelly.mywarp.permissions;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;


public class VaultHandler implements IPermissionsHandler {

    private Permission permission;

	public VaultHandler(Permission permission) {
	    this.permission = permission;
	}

    @Override
	public boolean hasPermission(final Player player, final String node, boolean defaultPerm) {
	    return permission.has(player, node);
	}

	@Override
	public int getInteger(final Player player, final String node, final int defaultInt) {
		return defaultInt;
	}
}

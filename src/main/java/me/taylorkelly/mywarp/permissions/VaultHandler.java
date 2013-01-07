package me.taylorkelly.mywarp.permissions;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

/**
 * Handler for Vault
 * 
 */
public class VaultHandler implements PermissionsHandler {

    private Permission permission;

    public VaultHandler(Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean playerHasGroup(Player player, String group) {
        return permission.playerInGroup(player, group);
    }
}

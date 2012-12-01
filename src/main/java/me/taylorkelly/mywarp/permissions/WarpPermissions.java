package me.taylorkelly.mywarp.permissions;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.timer.Cooldown;
import me.taylorkelly.mywarp.timer.Warmup;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WarpPermissions {
    private PermissionsHandler permissionsHandler;

    public WarpPermissions(Plugin plugin) {
        permissionsHandler = new PermissionsHandler(plugin);
    }

    public boolean hasPermission(Player player, final String node) {
        return permissionsHandler.hasPermission(player, node);
    }

    public boolean playerHasGroup(Player player, String group) {
        return permissionsHandler.playerHasGroup(player, group);
    }

    public boolean canAccessAll(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.admin.accessall");
    }

    public boolean canModifyAll(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.admin.modifyall");
    }

    public boolean createSignWarp(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.warp.sign.create");
    }

    public boolean signWarp(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.warp.sign.warp");
    }

    public boolean disobeyTotalLimit(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.limit.total.unlimited");
    }

    public boolean disobeyPrivateLimit(Player player) {
        return permissionsHandler
                .hasPermission(player, "mywarp.limit.private.unlimited");
    }

    public boolean disobeyPublicLimit(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.limit.public.unlimited");
    }

    public int maxTotalWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (permissionsHandler.hasPermission(player, "mywarp.limit."
                    + warpLimit.name)) {
                return warpLimit.maxTotal;
            }
        }
        return WarpSettings.defaultLimit.maxTotal;
    }

    public int maxPrivateWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (permissionsHandler.hasPermission(player, "mywarp.limit."
                    + warpLimit.name)) {
                return warpLimit.maxPrivate;
            }
        }
        return WarpSettings.defaultLimit.maxPrivate;
    }

    public int maxPublicWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (permissionsHandler.hasPermission(player, "mywarp.limit."
                    + warpLimit.name)) {
                return warpLimit.maxPublic;
            }
        }
        return WarpSettings.defaultLimit.maxPublic;
    }

    public boolean disobeyCooldown(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.cooldown.disobey");
    }

    public Cooldown getCooldown(Player player) {
        for (int i = 0; i < WarpSettings.warpCooldowns.size(); i++) {
            if (permissionsHandler.hasPermission(player, "mywarp.cooldown."
                    + WarpSettings.warpCooldowns.get(i).name)) {
                return WarpSettings.warpCooldowns.get(i);
            }
        }
        return WarpSettings.defaultCooldown;
    }

    public boolean disobeyWarmup(Player player) {
        return permissionsHandler.hasPermission(player, "mywarp.warmup.disobey");
    }

    public boolean disobeyWarmupMoveAbort(Player player) {
        return permissionsHandler.hasPermission(player,
                "mywarp.warmup.disobey.moveabort");
    }

    public boolean disobeyWarmupDmgAbort(Player player) {
        return permissionsHandler
                .hasPermission(player, "mywarp.warmup.disobey.dmgabort");
    }

    public Warmup getWarmup(Player player) {
        for (int i = 0; i < WarpSettings.warpWarmups.size(); i++) {
            if (permissionsHandler.hasPermission(player, "mywarp.warmup."
                    + WarpSettings.warpWarmups.get(i).name)) {
                return WarpSettings.warpWarmups.get(i);
            }
        }
        return WarpSettings.defaultWarmup;
    }
}
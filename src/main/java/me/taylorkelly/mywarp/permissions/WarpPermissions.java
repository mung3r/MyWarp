package me.taylorkelly.mywarp.permissions;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.timer.Cooldown;
import me.taylorkelly.mywarp.timer.Warmup;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Stores all non-command permission in one place
 * 
 */
public class WarpPermissions {
    private PermissionsManager permissionsManager;

    public WarpPermissions(Plugin plugin) {
        permissionsManager = new PermissionsManager(plugin);
    }

    public boolean playerHasGroup(Player player, final String group) {
        return permissionsManager.playerHasGroup(player, group);
    }

    public boolean hasPermission(final CommandSender executor, final String node) {
        return permissionsManager.hasPermission(executor, node);
    }

    public boolean canAccessAll(Player player) {
        return hasPermission(player,
                "mywarp.admin.accessall");
    }

    public boolean canInviteGroup(CommandSender executor) {
        return hasPermission(executor,
                "mywarp.warp.soc.invite.group");
    }

    public boolean canModifyAll(Player player) {
        return hasPermission(player,
                "mywarp.admin.modifyall");
    }

    public boolean canUninviteGroup(CommandSender executor) {
        return hasPermission(executor,
                "mywarp.warp.soc.uninvite.group");
    }

    public boolean canWarpInsideWorld(Player player) {
        return hasPermission(player,
                "mywarp.warp.world.currentworld");
    }

    public boolean canWarpToWorld(Player player, String worldName) {
        return hasPermission(player, "mywarp.warp.world."
                + worldName);
    }

    public boolean createSignWarp(Player player) {
        return hasPermission(player,
                "mywarp.warp.sign.create");
    }

    public boolean createSignWarpAll(Player player) {
        return hasPermission(player,
                "mywarp.warp.sign.create.all");
    }

    public boolean disobeyCooldown(Player player) {
        return hasPermission(player,
                "mywarp.cooldown.disobey");
    }

    public boolean disobeyPrivateLimit(Player player) {
        return hasPermission(player,
                "mywarp.limit.private.unlimited");
    }

    public boolean disobeyPublicLimit(Player player) {
        return hasPermission(player,
                "mywarp.limit.public.unlimited");
    }

    public boolean disobeyTotalLimit(Player player) {
        return hasPermission(player,
                "mywarp.limit.total.unlimited");
    }

    public boolean disobeyWarmup(Player player) {
        return hasPermission(player, "mywarp.warmup.disobey");
    }

    public boolean disobeyWarmupDmgAbort(Player player) {
        return hasPermission(player,
                "mywarp.warmup.disobey.dmgabort");
    }

    public boolean disobeyWarmupMoveAbort(Player player) {
        return hasPermission(player,
                "mywarp.warmup.disobey.moveabort");
    }

    public Cooldown getCooldown(Player player) {
        for (Cooldown cooldown : WarpSettings.warpCooldowns) {
            if (hasPermission(player, "mywarp.cooldown."
                    + cooldown.name)) {
                return cooldown;
            }
        }
        return WarpSettings.defaultCooldown;
    }

    public Warmup getWarmup(Player player) {
        for (Warmup warmup : WarpSettings.warpWarmups) {
            if (hasPermission(player, "mywarp.warmup."
                    + warmup.name)) {
                return warmup;
            }
        }
        return WarpSettings.defaultWarmup;
    }

    public int maxPrivateWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (hasPermission(player, "mywarp.limit."
                    + warpLimit.name)) {
                return warpLimit.maxPrivate;
            }
        }
        return WarpSettings.defaultLimit.maxPrivate;
    }

    public int maxPublicWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (hasPermission(player, "mywarp.limit."
                    + warpLimit.name)) {
                return warpLimit.maxPublic;
            }
        }
        return WarpSettings.defaultLimit.maxPublic;
    }

    public int maxTotalWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (hasPermission(player, "mywarp.limit."
                    + warpLimit.name)) {
                return warpLimit.maxTotal;
            }
        }
        return WarpSettings.defaultLimit.maxTotal;
    }

    public boolean useSignWarp(Player player) {
        return hasPermission(player, "mywarp.warp.sign.use");
    }
}
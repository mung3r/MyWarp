package me.taylorkelly.mywarp.permissions;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.economy.WarpFees;
import me.taylorkelly.mywarp.timer.Time;

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

    public boolean hasPermission(final CommandSender sender, final String node) {
        return permissionsManager.hasPermission(sender, node);
    }

    public boolean canAccessAll(Player player) {
        return hasPermission(player, "mywarp.admin.accessall");
    }

    public boolean canInviteGroup(CommandSender sender) {
        return hasPermission(sender, "mywarp.warp.soc.invite.group");
    }

    public boolean canModifyAll(Player player) {
        return hasPermission(player, "mywarp.admin.modifyall");
    }

    public boolean canUninviteGroup(CommandSender sender) {
        return hasPermission(sender, "mywarp.warp.soc.uninvite.group");
    }

    public boolean canWarpInsideWorld(Player player) {
        return hasPermission(player, "mywarp.warp.world.currentworld");
    }

    public boolean canWarpToWorld(Player player, String worldName) {
        return hasPermission(player, "mywarp.warp.world." + worldName);
    }

    public boolean createSignWarp(Player player) {
        return hasPermission(player, "mywarp.warp.sign.create");
    }

    public boolean createSignWarpAll(Player player) {
        return hasPermission(player, "mywarp.warp.sign.create.all");
    }

    public boolean disobeyCooldown(Player player) {
        return hasPermission(player, "mywarp.cooldown.disobey");
    }

    public boolean disobeyPrivateLimit(Player player) {
        return hasPermission(player, "mywarp.limit.private.unlimited");
    }

    public boolean disobeyPublicLimit(Player player) {
        return hasPermission(player, "mywarp.limit.public.unlimited");
    }

    public boolean disobeyTotalLimit(Player player) {
        return hasPermission(player, "mywarp.limit.total.unlimited");
    }

    public boolean disobeyWarmup(Player player) {
        return hasPermission(player, "mywarp.warmup.disobey");
    }

    public boolean disobeyWarmupDmgAbort(Player player) {
        return hasPermission(player, "mywarp.warmup.disobey.dmgabort");
    }

    public boolean disobeyWarmupMoveAbort(Player player) {
        return hasPermission(player, "mywarp.warmup.disobey.moveabort");
    }

    public Time getCooldown(Player player) {
        for (Time cooldown : WarpSettings.warpCooldowns) {
            if (hasPermission(player, "mywarp.cooldown." + cooldown.getName())) {
                return cooldown;
            }
        }
        return WarpSettings.defaultCooldown;
    }

    public Time getWarmup(Player player) {
        for (Time warmup : WarpSettings.warpWarmups) {
            if (hasPermission(player, "mywarp.warmup." + warmup.getName())) {
                return warmup;
            }
        }
        return WarpSettings.defaultWarmup;
    }

    public int maxPrivateWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit.getMaxPrivate();
            }
        }
        return WarpSettings.defaultLimit.getMaxPrivate();
    }

    public int maxPublicWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit.getMaxPublic();
            }
        }
        return WarpSettings.defaultLimit.getMaxPublic();
    }

    public int maxTotalWarps(Player player) {
        for (WarpLimit warpLimit : WarpSettings.warpLimits) {
            if (hasPermission(player, "mywarp.limit." + warpLimit.getName())) {
                return warpLimit.getMaxTotal();
            }
        }
        return WarpSettings.defaultLimit.getMaxTotal();
    }

    public boolean useSignWarp(Player player) {
        return hasPermission(player, "mywarp.warp.sign.use");
    }

    public boolean disobeyEconomyFees(CommandSender sender) {
        return hasPermission(sender, "mywarp.economy.free");
    }

    public WarpFees getEconomyPrices(CommandSender sender) {
        for (WarpFees warpFees : WarpSettings.warpFees) {
            if (hasPermission(sender, "mywarp.economy." + warpFees.getName())) {
                return warpFees;
            }
        }
        return WarpSettings.defaultWarpFees;
    }
}
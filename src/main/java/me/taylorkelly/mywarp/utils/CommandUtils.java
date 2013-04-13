package me.taylorkelly.mywarp.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.commands.CommandException;

public class CommandUtils {

    private static MyWarp plugin;

    public CommandUtils(MyWarp plugin) {
        CommandUtils.plugin = plugin;
    }

    public static Warp getWarp(CommandSender sender, String query)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        MatchList matches = plugin.getWarpList().getMatches(query, player,
                new PopularityWarpComparator());

        Warp warp = matches.getMatch();

        if (warp == null) {
            Warp match = matches.getLikliestMatch();

            if (WarpSettings.suggestWarps && match != null) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "error.noSuchWarp.suggestion", "%warp%", query,
                        "%suggestion%", match.name));
            } else {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "error.noSuchWarp", "%warp%", query));
            }
        }
        return warp;
    }

    public static Warp getWarpForModification(CommandSender sender, String query)
            throws CommandException {
        Warp warp = getWarp(sender, query);

        if (sender instanceof Player && !warp.playerCanModify((Player) sender)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.noPermission.modify", "%warp%", warp.name));
        }
        return warp;
    }

    public static Warp getWarpForUsage(CommandSender sender, String query)
            throws CommandException {
        Warp warp = getWarp(sender, query);

        if (WarpSettings.worldAccess
                && sender instanceof Player
                && !plugin.getWarpList().playerCanAccessWorld((Player) sender,
                        warp.world)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.noPermission.world", "%world%", warp.world));
        }
        return warp;
    }

    public static void checkTotalLimit(CommandSender sender)
            throws CommandException {
        if (!WarpSettings.useWarpLimits || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!plugin.getWarpList().playerCanBuildWarp(player)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "limit.total.reached",
                    "%maxTotal%",
                    Integer.toString(MyWarp.getWarpPermissions().maxTotalWarps(
                            player))));
        }
    }

    public static void checkPublicLimit(CommandSender sender)
            throws CommandException {
        if (!WarpSettings.useWarpLimits || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!plugin.getWarpList().playerCanBuildPublicWarp(player)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "limit.public.reached", "%maxPublic%", Integer
                            .toString(MyWarp.getWarpPermissions()
                                    .maxPublicWarps(player))));
        }
    }

    public static void checkPrivateLimit(CommandSender sender)
            throws CommandException {
        if (!WarpSettings.useWarpLimits || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!plugin.getWarpList().playerCanBuildPrivateWarp(player)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "limit.private.reached.player", "%maxPrivate%", Integer
                            .toString(MyWarp.getWarpPermissions()
                                    .maxPrivateWarps(player))));
        }
    }

    // TODO simplify
    public static void checkPlayerLimits(CommandSender sender, boolean publicAll)
            throws CommandException {
        if (!WarpSettings.useWarpLimits || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!plugin.getWarpList().playerCanBuildWarp(player)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "limit.total.reached.player",
                    "%maxTotal%",
                    Integer.toString(MyWarp.getWarpPermissions().maxTotalWarps(
                            player)), "%player%", player.getName()));
        }
        if (publicAll) {
            if (!plugin.getWarpList().playerCanBuildPublicWarp(player)) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "limit.public.reached.player", "%maxPublic%", Integer
                                .toString(MyWarp.getWarpPermissions()
                                        .maxPublicWarps(player)), "%player%",
                        player.getName()));
            }
        } else if (!plugin.getWarpList().playerCanBuildPrivateWarp(player)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "limit.private.reached.player", "%maxPrivate%", Integer
                            .toString(MyWarp.getWarpPermissions()
                                    .maxPrivateWarps(player)), "%player%",
                    player.getName()));
        }
    }

    public static Player checkPlayer(String player) throws CommandException {
        Player actuallPlayer = plugin.getServer().getPlayer(player);
        if (actuallPlayer == null) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.player.offline", "%player%", player));
        }
        return actuallPlayer;
    }
}

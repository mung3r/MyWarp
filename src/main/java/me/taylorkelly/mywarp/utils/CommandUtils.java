package me.taylorkelly.mywarp.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.commands.CommandException;
import me.taylorkelly.mywarp.data.Warp;

public class CommandUtils {

    private static MyWarp plugin;

    public CommandUtils(MyWarp plugin) {
        CommandUtils.plugin = plugin;
    }

    public static String toWarpName(String[] args) {
        return toWarpName(args, 0);
    }

    public static String toWarpName(String[] args, int index) {
        return StringUtils.join(args, ' ', index, args.length);
    }

    public static Warp getWarp(CommandSender sender, String query)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        MatchList matches = plugin.getWarpList().getMatches(query, player,
                new popularityWarpComperator());

        Warp warp = matches.getMatch();

        if (warp == null) {
            Warp match = matches.getLikliestMatch();

            if (WarpSettings.suggestWarps && match != null) {
                throw new CommandException(LanguageManager
                        .getString("error.noSuchWarp.suggestion")
                        .replaceAll("%warp%", query)
                        .replaceAll("%suggestion%", match.name));
            } else {
                throw new CommandException(LanguageManager.getString(
                        "error.noSuchWarp").replaceAll("%warp%", query));
            }
        }
        return warp;
    }

    public static Warp getWarpForModification(CommandSender sender, String query)
            throws CommandException {
        Warp warp = getWarp(sender, query);

        if (sender instanceof Player && !warp.playerCanModify((Player) sender)) {
            throw new CommandException(
                    LanguageManager.getString("error.noPermission.modify"));
        }
        return warp;
    }

    public static Warp getWarpForUsage(CommandSender sender, String query)
            throws CommandException {
        Warp warp = getWarp(sender, query);

        if (sender instanceof Player && !warp.playerCanWarp((Player) sender)) {
            throw new CommandException(
                    LanguageManager.getString("error.noPermission.use"));
        }
        if (WarpSettings.worldAccess
                && sender instanceof Player
                && !plugin.getWarpList().playerCanAccessWorld((Player) sender,
                        warp.world)) {
            throw new CommandException(LanguageManager.getString(
                    "error.noPermission.world").replaceAll("%world%",
                    warp.world));
        }
        return warp;
    }

    // TODO simplify
    public static void checkLimits(CommandSender sender, boolean publicAll)
            throws CommandException {
        if (!WarpSettings.useWarpLimits || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!plugin.getWarpList().playerCanBuildWarp(player)) {
            throw new CommandException(LanguageManager.getString(
                    "limit.total.reached").replaceAll(
                    "%maxTotal%",
                    Integer.toString(MyWarp.getWarpPermissions().maxTotalWarps(
                            player))));
        }
        if (publicAll) {
            if (plugin.getWarpList().playerCanBuildPublicWarp(player)) {
                throw new CommandException(LanguageManager.getString(
                        "limit.public.reached").replaceAll(
                        "%maxPublic%",
                        Integer.toString(MyWarp.getWarpPermissions()
                                .maxPublicWarps(player))));
            }
        } else if (!plugin.getWarpList().playerCanBuildPrivateWarp(player)) {
            throw new CommandException(LanguageManager.getString(
                    "limit.private.reached.player").replaceAll(
                    "%maxPrivate%",
                    Integer.toString(MyWarp.getWarpPermissions()
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
            throw new CommandException(LanguageManager.getString(
                    "limit.total.reached.player").replaceAll(
                    "%maxTotal%",
                    Integer.toString(
                            MyWarp.getWarpPermissions().maxTotalWarps(player))
                            .replaceAll("%player%", player.getName())));
        }
        if (publicAll) {
            if (plugin.getWarpList().playerCanBuildPublicWarp(player)) {
                throw new CommandException(LanguageManager.getString(
                        "limit.public.reached.player").replaceAll(
                        "%maxPublic%",
                        Integer.toString(
                                MyWarp.getWarpPermissions().maxPublicWarps(
                                        player)).replaceAll("%player%",
                                player.getName())));
            }
        } else if (!plugin.getWarpList().playerCanBuildPrivateWarp(player)) {
            throw new CommandException(LanguageManager.getString(
                    "limit.private.reached.player")
                    .replaceAll(
                            "%maxPrivate%",
                            Integer.toString(
                                    MyWarp.getWarpPermissions()
                                            .maxPrivateWarps(player))
                                    .replaceAll("%player%", player.getName())));
        }
    }

    public static Player checkPlayer(String player) throws CommandException {
        Player actuallPlayer = plugin.getServer().getPlayer(player);
        if (actuallPlayer == null) {
            throw new CommandException(LanguageManager.getString(
                    "error.player.offline").replaceAll("%player%", player));
        }
        return actuallPlayer;
    }
}

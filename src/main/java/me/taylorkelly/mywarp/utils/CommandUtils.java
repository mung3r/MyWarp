package me.taylorkelly.mywarp.utils;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.CommandPermissionsException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class bundles all methods that are only used to simplify certain task
 * when writing commands. All methods should be static.
 */
public class CommandUtils {

    /**
     * This method will try to get a warp using the given query as name or part
     * of the name. Internally it creates a {@link MatchList} with all possible
     * warps, respecting if the given CommandSender can access a warp or not.
     * 
     * If multiple valid warps are found, this method will throw a
     * CommandException. If enabled it will try to recommend one of the warps.
     * 
     * @param sender
     *            the command sender
     * @param query
     *            the (part) of the searched warp's name
     * @return the warp identified by the query
     * @throws CommandException
     *             if not warp could be found (could either mean a warp does not
     *             exist in the network or the sender has no right to access it)
     */
    public static Warp getWarpForUsage(CommandSender sender, String query)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        MatchList matches = MyWarp.inst().getWarpList()
                .getMatches(query, player, new PopularityWarpComparator());

        Warp warp = matches.getMatch();

        if (warp == null) {
            Warp match = matches.getLikliestMatch();

            if (MyWarp.inst().getWarpSettings().suggestWarps && match != null) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("error.noSuchWarp.suggestion",
                                "%warp%", query, "%suggestion%", match.getName()));
            } else {
                throw new CommandException(
                        MyWarp.inst()
                                .getLanguageManager()
                                .getEffectiveString("error.noSuchWarp",
                                        "%warp%", query));
            }
        }
        return warp;
    }

    /**
     * Gets a warp using {@link #getWarpForUsage(CommandSender, String)} and determines
     * if the given sender may modify it.
     * 
     * @param sender
     *            the command sender
     * @param query
     *            the (part) of the searched warp's name
     * @return the warp identified by the query
     * @throws CommandException
     *             if the given sender may not modify the warp
     */
    public static Warp getWarpForModification(CommandSender sender, String query)
            throws CommandException {
        Warp warp = getWarpForUsage(sender, query);

        if (sender instanceof Player && !warp.playerCanModify((Player) sender)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("error.noPermission.modify", "%warp%",
                            warp.getName()));
        }
        return warp;
    }

    /**
     * Checks the total warp-limit (private and public) of the given
     * command-sender. This method will do nothing if limits are disabled.
     * 
     * @param sender
     *            the command-sender
     * @throws CommandException
     *             if the sender has reached his total limit
     */
    public static void checkTotalLimit(CommandSender sender)
            throws CommandException {
        if (!MyWarp.inst().getWarpSettings().useWarpLimits
                || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpList().playerCanBuildWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString(
                            "limit.total.reached",
                            "%maxTotal%",
                            Integer.toString(MyWarp.inst()
                                    .getPermissionsManager()
                                    .maxTotalWarps(player))));
        }
    }

    /**
     * Checks the public warp-limit of the given command-sender. This method
     * will do nothing if limits are disabled.
     * 
     * @param sender
     *            the command-sender
     * @throws CommandException
     *             if the sender has reached his public limit
     */
    public static void checkPublicLimit(CommandSender sender)
            throws CommandException {
        if (!MyWarp.inst().getWarpSettings().useWarpLimits
                || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpList().playerCanBuildPublicWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString(
                            "limit.public.reached",
                            "%maxPublic%",
                            Integer.toString(MyWarp.inst()
                                    .getPermissionsManager()
                                    .maxPublicWarps(player))));
        }
    }

    /**
     * Checks the private warp-limit of the given command-sender. This method
     * will do nothing if limits are disabled.
     * 
     * @param sender
     *            the command-sender
     * @throws CommandException
     *             if the sender has reached his private limit
     */
    public static void checkPrivateLimit(CommandSender sender)
            throws CommandException {
        if (!MyWarp.inst().getWarpSettings().useWarpLimits
                || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpList().playerCanBuildPrivateWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString(
                            "limit.private.reached",
                            "%maxPrivate%",
                            Integer.toString(MyWarp.inst()
                                    .getPermissionsManager()
                                    .maxPrivateWarps(player))));
        }
    }

    /**
     * Checks all warp-limits of the given command-sender. This method will show
     * the player's name in error messages, rather than the pronoun ('you'). If
     * limits are disabled, this method will do nothing.
     * 
     * @param sender
     *            the command-sender
     * @param publicAll
     *            whether public (true) or private (false) limits should be
     *            checked
     * @throws CommandException
     *             if the command sender has reached one of his limits
     */
    public static void checkPlayerLimits(CommandSender sender, boolean publicAll)
            throws CommandException {
        if (!MyWarp.inst().getWarpSettings().useWarpLimits
                || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpList().playerCanBuildWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString(
                            "limit.total.reached.player",
                            "%maxTotal%",
                            Integer.toString(MyWarp.inst()
                                    .getPermissionsManager()
                                    .maxTotalWarps(player)), "%player%",
                            player.getName()));
        }
        if (publicAll) {
            if (!MyWarp.inst().getWarpList().playerCanBuildPublicWarp(player)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString(
                                "limit.public.reached.player",
                                "%maxPublic%",
                                Integer.toString(MyWarp.inst()
                                        .getPermissionsManager()
                                        .maxPublicWarps(player)), "%player%",
                                player.getName()));
            }
        } else if (!MyWarp.inst().getWarpList()
                .playerCanBuildPrivateWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString(
                            "limit.private.reached.player",
                            "%maxPrivate%",
                            Integer.toString(MyWarp.inst()
                                    .getPermissionsManager()
                                    .maxPrivateWarps(player)), "%player%",
                            player.getName()));
        }
    }

    /**
     * Gets a single player per name or a part of the name.
     * 
     * @param name
     *            the (part of) the player's name
     * @return the player found using the given name
     * @throws CommandException
     *             if no matching player could be found
     */
    public static Player matchPlayer(String name) throws CommandException {
        Player actuallPlayer = MyWarp.inst().getServer().getPlayer(name);
        if (actuallPlayer == null) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("error.player.offline", "%player%",
                            name));
        }
        return actuallPlayer;
    }

    /**
     * Checks if the given command-sender hat the given permission. If not, an
     * error is thrown.
     * 
     * @param sender
     *            the command-sender who should be checked
     * @param perm
     *            the permission that should be checked
     * @throws CommandPermissionsException
     *             if the sender does NOT have the given permission
     */
    public static void checkPermissions(CommandSender sender, String perm)
            throws CommandPermissionsException {
        if (!MyWarp.inst().getPermissionsManager().hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }
}

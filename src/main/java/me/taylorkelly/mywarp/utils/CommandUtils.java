package me.taylorkelly.mywarp.utils;

import java.util.Collection;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.CommandPermissionsException;

import org.apache.commons.lang.text.StrBuilder;
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
    public static Warp getWarpForUsage(CommandSender sender, String query) throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        MatchList matches = MyWarp.inst().getWarpManager()
                .getMatches(query, player, new PopularityWarpComparator());

        Warp warp = matches.getMatch();

        if (warp == null) {
            Warp match = matches.getLikliestMatch();

            if (MyWarp.inst().getWarpSettings().dynamicsSuggestWarps && match != null) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("commands.utils.warp-suggestion", sender, query, match.getName()));
            } else {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("commands.utils.warp-non-existent", sender, query));
            }
        }
        return warp;
    }

    /**
     * Gets a warp using {@link #getWarpForUsage(CommandSender, String)} and
     * determines if the given sender may modify it.
     * 
     * @param sender
     *            the command sender
     * @param query
     *            the (part) of the searched warp's name
     * @return the warp identified by the query
     * @throws CommandException
     *             if the given sender may not modify the warp
     */
    public static Warp getWarpForModification(CommandSender sender, String query) throws CommandException {
        Warp warp = getWarpForUsage(sender, query);

        if (!warp.isModifiable(sender)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.modify-permission", sender, warp.getName()));
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
    public static void checkTotalLimit(CommandSender sender) throws CommandException {
        if (!MyWarp.inst().getWarpSettings().limitsEnabled || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpManager().playerCanBuildWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.utils.limit-reached.total", sender,
                            (MyWarp.inst().getPermissionsManager().getWarpLimit(player).getMaxTotal())));
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
    public static void checkPublicLimit(CommandSender sender) throws CommandException {
        if (!MyWarp.inst().getWarpSettings().limitsEnabled || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpManager().playerCanBuildPublicWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.utils.limit-reached.public", sender,
                            MyWarp.inst().getPermissionsManager().getWarpLimit(player).getMaxPublic()));
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
    public static void checkPrivateLimit(CommandSender sender) throws CommandException {
        if (!MyWarp.inst().getWarpSettings().limitsEnabled || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpManager().playerCanBuildPrivateWarp(player)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.utils.limit-reached.private", sender,
                            MyWarp.inst().getPermissionsManager().getWarpLimit(player).getMaxPrivate()));
        }
    }

    /**
     * Checks all warp-limits of the given command-sender, returns true if the
     * player can build an additional warp, false if not. If limits are
     * disabled, this method will do nothing.
     * 
     * @param sender
     *            the command-sender
     * @param publicAll
     *            whether public (true) or private (false) limits should be
     *            checked
     * @throws CommandException
     *             if the command sender has reached one of his limits
     */
    public static boolean checkPlayerLimits(CommandSender sender, boolean publicAll) throws CommandException {
        if (!MyWarp.inst().getWarpSettings().limitsEnabled || !(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;

        if (!MyWarp.inst().getWarpManager().playerCanBuildWarp(player)) {
            return false;
        }
        if (publicAll) {
            if (!MyWarp.inst().getWarpManager().playerCanBuildPublicWarp(player)) {
                return false;
            }
        } else if (!MyWarp.inst().getWarpManager().playerCanBuildPrivateWarp(player)) {
            return false;
        }
        return true;
    }

    /**
     * Gets a single player per name or a part of the name.
     * 
     * @param name
     *            the (part of) the player's name
     * @param sender
     *            the CommandSender who initiated the matching
     * @return the player found using the given name
     * @throws CommandException
     *             if no matching player could be found
     */
    public static Player matchPlayer(CommandSender sender, String name) throws CommandException {
        Player actuallPlayer = MyWarp.inst().getServer().getPlayer(name);
        if (actuallPlayer == null) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.utils.player-offline", sender, name));
        }
        return actuallPlayer;
    }

    /**
     * Checks to see if the sender is a player, otherwise throws an exception.
     * 
     * @param sender
     *            the sender
     * @return The corresponding Player
     * @throws CommandException
     *             if the given sender is not a Player
     */
    public static Player checkPlayer(CommandSender sender) throws CommandException {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.library.invalid-sender", sender));
        }
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
    public static void checkPermissions(CommandSender sender, String perm) throws CommandPermissionsException {
        if (!MyWarp.inst().getPermissionsManager().hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }

    /**
     * Checks if the given name can be used as a name for a new warp. If not, a
     * CommandException is thrown.
     * 
     * @param sender
     *            the command-sender
     * @param name
     *            the name to check
     * @throws CommandException
     *             if the name cannot be used for a new warp
     */
    public static void checkWarpname(CommandSender sender, String name) throws CommandException {
        if (MyWarp.inst().getWarpManager().warpExists(name)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.utils.warp-exists", sender, name));
        }
        if (MyWarp.inst().getCommandsManager().hasSubCommand("mywarp", name)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.utils.name-is-cmd", sender, name));
        }
    }

    /**
     * Joins all warps in the given collection in one string, separated by
     * <code>", "</code>.
     * 
     * @param warps
     *            a collection of warps
     * @return a string with all warp-names
     */
    public static String joinWarps(Collection<Warp> warps) {
        if (warps.isEmpty()) {
            return "-";
        }

        StrBuilder ret = new StrBuilder();
        for (Warp warp : warps) {
            ret.appendSeparator(", ");
            ret.append(warp.getName());
        }
        return ret.toString();
    }
}

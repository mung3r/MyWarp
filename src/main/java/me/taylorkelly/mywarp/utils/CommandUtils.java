package me.taylorkelly.mywarp.utils;

import java.util.Collection;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.data.WarpLimit.Limit;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.CommandPermissionsException;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.base.Predicate;

/**
 * This class bundles all methods that are only used to simplify certain task
 * when writing commands. All methods should be static.
 */
public class CommandUtils {

    public static Warp getViewableWarp(final CommandSender sender, String filter) throws CommandException {
        return getWarp(sender, filter, new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isViewable(sender);
            }

        });
    }

    public static Warp getUsableWarp(final CommandSender sender, String filter) throws CommandException {
        return getWarp(sender, filter, new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return sender instanceof Entity && warp.isUsable((Entity) sender);
            }

        });
    }

    // TODO remove 'commands.modify-permission' localization string
    public static Warp getModifiableWarp(final CommandSender sender, String filter) throws CommandException {
        return getWarp(sender, filter, new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isModifiable(sender);
            }

        });
    }

    public static Warp getWarp(CommandSender sender, String filter, Predicate<Warp> predicate)
            throws CommandException {
        Matcher matcher = Matcher.match(filter, predicate);
        Warp warp = matcher.getExactMatch();

        if (warp == null) {
            Warp match = matcher.getMatch();

            if (MyWarp.inst().getWarpSettings().dynamicsSuggestWarps && match != null) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.utils.warp-suggestion", sender, filter, match.getName()));
            } else {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.utils.warp-non-existent", sender, filter));
            }
        }
        return warp;
    }

    public static void checkLimits(CommandSender sender, boolean newlyBuild, Type type)
            throws CommandException {
        if (!MyWarp.inst().getWarpSettings().limitsEnabled || !(sender instanceof Player)) {
            return;
        }
        Player player = (Player) sender;

        switch (MyWarp.inst().getWarpManager().canAddWarp(player, player.getWorld(), newlyBuild, type)) {
        case DENY_PRIVATE:
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString(
                            "commands.utils.limit-reached.private",
                            sender,
                            MyWarp.inst().getPermissionsManager().getWarpLimit(player)
                                    .getLimit(Limit.PRIVATE)));
        case DENY_PUBLIC:
            throw new CommandException(
                    MyWarp.inst()
                            .getLocalizationManager()
                            .getString(
                                    "commands.utils.limit-reached.public",
                                    sender,
                                    MyWarp.inst().getPermissionsManager().getWarpLimit(player)
                                            .getLimit(Limit.PUBLIC)));
        case DENY_TOTAL:
            throw new CommandException(
                    MyWarp.inst()
                            .getLocalizationManager()
                            .getString(
                                    "commands.utils.limit-reached.total",
                                    sender,
                                    (MyWarp.inst().getPermissionsManager().getWarpLimit(player)
                                            .getLimit(Limit.TOTAL))));
        default:
            return;

        }
    }

    public static boolean checkPlayerLimits(CommandSender sender, World world, Type type)
            throws CommandException {
        if (!MyWarp.inst().getWarpSettings().limitsEnabled || !(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;

        switch (MyWarp.inst().getWarpManager().canAddWarp(player, world, true, type)) {
        case ALLOW:
            return true;
        default:
            return false;
        }
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
                    .getString("commands.utils.player-offline", sender, name));
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
                    .getString("commands.utils.warp-exists", sender, name));
        }
        if (MyWarp.inst().getCommandsManager().hasSubCommand("mywarp", name)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.utils.name-is-cmd", sender, name));
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

    public static OfflinePlayer matchOfflinePlayer(String name) {
        OfflinePlayer actuallPlayer = MyWarp.inst().getServer().getPlayer(name);
        if (actuallPlayer == null) {
            actuallPlayer = MyWarp.server().getOfflinePlayer(name);
        }
        return actuallPlayer;
    }
}

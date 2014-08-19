package me.taylorkelly.mywarp.utils;

import java.util.Collection;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.LimitBundle;
import me.taylorkelly.mywarp.data.LimitBundle.Limit;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.CommandPermissionsException;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * This class bundles all methods that are only used to simplify certain task
 * when writing commands. All methods should be static.
 */
public class CommandUtils {

    private static final int WARP_NAME_LENGTH = 32;

    /**
     * Block initialization of this class.
     */
    private CommandUtils() {
    }

    /**
     * Gets a warp, matching the given name-filter, that is viewable by the
     * given command-sender.
     * 
     * @see Matcher#match(String, Predicate)
     * 
     * @param sender
     *            the command-sender
     * @param nameFilter
     *            the name-filter
     * @return a modifiable warp
     * @throws CommandException
     *             if there is no or if there are multiple warps matching the
     *             given criteria
     */
    public static Warp getViewableWarp(final CommandSender sender, String nameFilter) throws CommandException {
        return getWarp(sender, nameFilter, WarpUtils.isViewable(sender));
    }

    /**
     * Gets a warp, matching the given name-filter, that is usable by the given
     * command-sender.
     * 
     * @see Matcher#match(String, Predicate)
     * 
     * @param sender
     *            the command-sender
     * @param nameFilter
     *            the name-filter
     * @return a modifiable warp
     * @throws CommandException
     *             if there is no or if there are multiple warps matching the
     *             given criteria
     */
    public static Warp getUsableWarp(final CommandSender sender, String nameFilter) throws CommandException {
        return getWarp(sender, nameFilter, sender instanceof Entity ? WarpUtils.isUsable((Entity) sender)
                : Predicates.<Warp> alwaysTrue());
    }

    /**
     * Gets a warp, matching the given name-filter, that is modifiable by the
     * given command-sender.
     * 
     * @see Matcher#match(String, Predicate)
     * 
     * @param sender
     *            the command-sender
     * @param nameFilter
     *            the name-filter
     * @return a modifiable warp
     * @throws CommandException
     *             if there is no or if there are multiple warps matching the
     *             given criteria
     */
    public static Warp getModifiableWarp(final CommandSender sender, String nameFilter)
            throws CommandException {
        return getWarp(sender, nameFilter, WarpUtils.isModifiable(sender));
    }

    /**
     * Gets a warp that matches the given nameFilter and the given predicate.
     * 
     * @see Matcher#match(String, Predicate)
     * 
     * @param sender
     *            the command-sender
     * @param nameFilter
     *            the name-filter
     * @param predicate
     *            the predicate
     * @return a warp matching the given criteria
     * @throws CommandException
     *             if there is no or if there are multiple warps matching the
     *             given criteria
     */
    public static Warp getWarp(CommandSender sender, String nameFilter, Predicate<Warp> predicate)
            throws CommandException {
        Matcher matcher = Matcher.match(nameFilter, predicate);
        Warp warp = matcher.getExactMatch();

        if (warp == null) {
            Warp match = matcher.getMatch();

            if (MyWarp.inst().getSettings().isDynamicsSuggestWarps() && match != null) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.utils.warp-suggestion", sender, nameFilter, match.getName()));
            } else {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.utils.warp-non-existent", sender, nameFilter));
            }
        }
        return warp;
    }

    /**
     * Checks if the given command-sender would violate any warp-limit if he
     * adds a warp of the given type.
     * 
     * @param sender
     *            the command-sender
     * @param newlyBuild
     *            whether the warp would be entirely new or just an updated
     *            (type change) of an existing warp
     * @param type
     *            the type
     * @throws CommandException
     *             if this action would violate the limits of the sender
     */
    public static void checkLimits(CommandSender sender, boolean newlyBuild, Type type)
            throws CommandException {
        if (!MyWarp.inst().getSettings().isLimitsEnabled() || !(sender instanceof Player)) {
            return;
        }
        final Player player = (Player) sender;

        LimitBundle limitBundle = MyWarp.inst().getPermissionsManager().getLimitBundleManager()
                .getBundle(player);

        Collection<Warp> warps = MyWarp.inst().getWarpManager().getWarps(WarpUtils.isCreator(player));

        if (newlyBuild && limitBundle.exceedsLimit(Limit.TOTAL, player, player.getWorld(), warps)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.utils.limit-reached.total", sender,
                            limitBundle.getLimit(Limit.TOTAL)));
        }

        if (limitBundle.exceedsLimit(type.getLimit(), player, player.getWorld(), warps)) {
            switch (type.getLimit()) {
            case PRIVATE:
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString("commands.utils.limit-reached.private", sender,
                                limitBundle.getLimit(Limit.PRIVATE)));
            case PUBLIC:
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString("commands.utils.limit-reached.public", sender,
                                limitBundle.getLimit(Limit.PUBLIC)));
            default:
                break;
            }
        }
    }

    /**
     * Checks if the given command-sender would violate any warp-limit if he
     * adds a warp of the given type.
     * 
     * @param sender
     *            the command-sender
     * @param world
     *            the world of additional warp
     * @param type
     *            the type of the additional warp
     * @return true if this action would not violate the limits of the sender
     */
    public static boolean checkPlayerLimits(CommandSender sender, World world, Type type) {
        if (!MyWarp.inst().getSettings().isLimitsEnabled() || !(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player) sender;

        LimitBundle limitBundle = MyWarp.inst().getPermissionsManager().getLimitBundleManager()
                .getBundle(player);
        Collection<Warp> warps = MyWarp.inst().getWarpManager().getWarps(WarpUtils.isCreator(player));

        return !limitBundle.exceedsLimit(Limit.TOTAL, player, world, warps)
                && !limitBundle.exceedsLimit(type.getLimit(), player, world, warps);
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
     * Checks to see if the sender is a player.
     * 
     * @param sender
     *            the sender
     * @return the corresponding Player
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
     * Checks if the given command-sender has the given permission.
     * 
     * @param sender
     *            the command-sender
     * @param perm
     *            the permission-node
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
        if (name.length() > WARP_NAME_LENGTH) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.utils.name-too-long", sender, WARP_NAME_LENGTH));
        }
    }

    /**
     * Joins all warps in the given collection in one string, separated by
     * <code>", "</code>.
     * 
     * @param warps
     *            a collection of warps
     * @return a string with all warp-names or <code>-</code> if the collection
     *         was empty
     */
    public static String joinWarps(Collection<Warp> warps) {
        if (warps.isEmpty()) {
            return "-";
        }

        StrBuilder ret = new StrBuilder();
        for (Warp warp : warps) {
            ret.appendSeparator(", ");
            // XXX color warp names
            ret.append(warp.getName());
        }
        return ret.toString();
    }

    public static String joinWorlds(Collection<World> worlds) {
        if (worlds.isEmpty()) {
            return "-";
        }

        StrBuilder ret = new StrBuilder();
        for (World world : worlds) {
            ret.appendSeparator(", ");
            ret.append(world.getName());
        }
        return ret.toString();
    }

    /**
     * Matches an offline player via it's name. If a player who is online
     * matching the given name, this one will be returned.
     * 
     * @param name
     *            the player's name
     * @return an offline player matching the given name
     */
    public static OfflinePlayer matchOfflinePlayer(String name) {
        OfflinePlayer actuallPlayer = MyWarp.inst().getServer().getPlayer(name);
        if (actuallPlayer == null) {
            actuallPlayer = MyWarp.server().getOfflinePlayer(name);
        }
        return actuallPlayer;
    }
}

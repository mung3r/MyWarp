/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.mywarp.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.LimitBundle;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.economy.FeeBundle.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.FormattingUtils;
import me.taylorkelly.mywarp.utils.Matcher;
import me.taylorkelly.mywarp.utils.StringPaginator;
import me.taylorkelly.mywarp.utils.WarpUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

/**
 * This class contains all commands that cover basic tasks. They should be
 * included in the <code>mywarp.warp.basic.*</code> permission container.
 * 
 */
public class BasicCommands {

    /**
     * Creates a private warp.
     * 
     * @param args
     *            the command-arguments
     * @param player
     *            the player who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "pcreate", "pset" }, usage = "<name>", desc = "commands.create-private.description", fee = Fee.CREATE_PRIVATE, min = 1, permissions = { "mywarp.warp.basic.createprivate" })
    public void createPrivateWarp(CommandContext args, Player player) throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkLimits(player, true, Type.PRIVATE);
        CommandUtils.checkWarpname(player, name);

        MyWarp.inst().getWarpManager().addWarp(name, player, Type.PRIVATE);
        player.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.create-private.created-successful", player, name));
    }

    /**
     * Creates a public warp.
     * 
     * @param args
     *            the command-arguments
     * @param player
     *            the player who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "create", "set" }, usage = "<name>", desc = "commands.create.description", fee = Fee.CREATE, min = 1, permissions = { "mywarp.warp.basic.createpublic" })
    public void createPublicWarp(CommandContext args, Player player) throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkLimits(player, true, Type.PUBLIC);
        CommandUtils.checkWarpname(player, name);

        MyWarp.inst().getWarpManager().addWarp(name, player, Type.PUBLIC);
        player.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.create.created-successful", player, name));
    }

    /**
     * Removes a specific warp.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "delete", "remove" }, usage = "<name>", desc = "commands.delete.description", fee = Fee.DELETE, min = 1, permissions = { "mywarp.warp.basic.delete" })
    public void deleteWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(0));

        MyWarp.inst().getWarpManager().deleteWarp(warp);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.delete.deleted-successful", sender, warp.getName()));
    }

    /**
     * Displays the assets (warps ordered by limit and world) of a player.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "assets", "limits", "pstats", "pinfo" }, usage = "[player]", desc = "commands.assets.description", fee = Fee.ASSETS, max = 1, permissions = { "mywarp.warp.basic.assets" })
    public void showAssets(CommandContext args, final CommandSender sender) throws CommandException {
        final Player player;
        if (args.argsLength() == 0) {
            player = CommandUtils.checkPlayer(sender);
        } else {
            CommandUtils.checkPermissions(sender, "mywarp.warp.basic.assets.other");
            player = CommandUtils.matchPlayer(sender, args.getString(0));
        }

        sender.sendMessage(ChatColor.GOLD
                + FormattingUtils.center(
                        " "
                                + MyWarp.inst().getLocalizationManager()
                                        .getString("commands.assets.heading", sender, player.getName()) + " ",
                        '-'));

        Table<LimitBundle, Warp.Type, SortedSet<Warp>> mappedWarps = ArrayTable.create(
                MyWarp.inst().getSettings().isLimitsEnabled() ? MyWarp.inst().getPermissionsManager()
                        .getLimitBundleManager().getAffectiveBundles(player) : Arrays.asList(MyWarp.inst()
                        .getSettings().getLimitsDefaultLimitBundle()), Arrays.asList(Warp.Type.values()));
        for (Warp warp : MyWarp.inst().getWarpManager().getWarps(WarpUtils.isCreator(player))) {
            for (LimitBundle bundle : mappedWarps.rowKeySet()) {
                if (!bundle.isGlobal() && !bundle.getAffectedWorlds().contains(warp.getWorld())) {
                    continue;
                }
                SortedSet<Warp> storedWarps = mappedWarps.get(bundle, warp.getType());
                if (storedWarps == null) {
                    storedWarps = Sets.newTreeSet();
                    mappedWarps.put(bundle, warp.getType(), storedWarps);
                }
                storedWarps.add(warp);
            }
        }

        for (Entry<LimitBundle, Map<Type, SortedSet<Warp>>> entry : mappedWarps.rowMap().entrySet()) {
            SortedSet<Warp> publicWarps = entry.getValue().get(Warp.Type.PUBLIC);
            SortedSet<Warp> privateWarps = entry.getValue().get(Warp.Type.PRIVATE);
            LimitBundle bundle = entry.getKey();

            if (publicWarps == null) {
                publicWarps = Sets.newTreeSet();
            }
            if (privateWarps == null) {
                privateWarps = Sets.newTreeSet();
            }

            String publicEntry = MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.assets.public-warps", sender,
                            toLimitMax(publicWarps.size(), bundle.getLimit(LimitBundle.Limit.PUBLIC)),
                            CommandUtils.joinWarps(publicWarps));

            String privateEntry = MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.assets.private-warps", sender,
                            toLimitMax(privateWarps.size(), bundle.getLimit(LimitBundle.Limit.PRIVATE)),
                            CommandUtils.joinWarps(privateWarps));

            // send the messages
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString(
                            "commands.assets.total-warps",
                            sender,
                            CommandUtils.joinWorlds(bundle.getAffectedWorlds()),
                            toLimitMax(publicWarps.size() + privateWarps.size(),
                                    bundle.getLimit(LimitBundle.Limit.TOTAL))));
            sender.sendMessage(FormattingUtils.toList(publicEntry, privateEntry));
        }
    }

    /**
     * Returns a string representation of the given values, e.g.
     * <code>4/10</code>. {@code limitMax} is only added if limits are enabled.
     * 
     * @param count
     *            the count
     * @param limitMax
     *            the limit maximum
     * @return a readable string
     */
    private String toLimitMax(int count, int limitMax) {
        StringBuilder builder = new StringBuilder();
        builder.append(count);
        if (MyWarp.inst().getSettings().isLimitsEnabled()) {
            builder.append('/').append(limitMax);
        }
        return builder.toString();
    }

    /**
     * Lists warps.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "list", "alist" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "commands.list.description", fee = Fee.LIST, max = 1, permissions = { "mywarp.warp.basic.list" })
    public void listWarps(final CommandContext args, final CommandSender sender) throws CommandException {
        final OfflinePlayer creator = args.hasFlag('c') ? MyWarp.inst().getWarpManager()
                .getMatchingCreator(args.getFlag('c'), WarpUtils.isViewable(sender)) : null;
        final World world = args.hasFlag('w') ? MyWarp.inst().getWarpManager()
                .getMatchingWorld(args.getFlag('w'), WarpUtils.isViewable(sender)) : null;

        SortedSet<Warp> results = args.hasFlag('p') ? new TreeSet<Warp>(new Warp.PopularityComparator())
                : new TreeSet<Warp>();
        results.addAll(MyWarp.inst().getWarpManager().getWarps(new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                if (!warp.isViewable(sender)) {
                    return false;
                }
                if (creator != null && warp.isCreator(creator)) {
                    return false;
                }
                if (world != null && warp.getWorldId() != world.getUID()) {
                    return false;
                }
                return true;
            }

        }));
        
        StringPaginator<Warp> paginator = StringPaginator.of(results, MyWarp.inst().getLocalizationManager()
                .getColorlessString("commands.list.heading", sender));

        paginator.withMapping(new Function<Warp, String>() {

            @Override
            public String apply(Warp warp) {
                // 'name'(+) by player
                StringBuilder first = new StringBuilder();
                first.append("'");
                first.append(warp.getType().getColor());
                first.append(warp.getName());
                first.append(ChatColor.WHITE);
                first.append("' ");
                first.append(MyWarp.inst().getLocalizationManager()
                        .getColorlessString("commands.list.by", sender));
                first.append(" ");
                first.append(ChatColor.ITALIC);

                if (sender instanceof Player && warp.isCreator((Player) sender)) {
                    first.append(MyWarp.inst().getLocalizationManager()
                            .getColorlessString("commands.list.you", sender));
                } else {
                    first.append(warp.getCreator().getName());
                }
                // @(x, y, z)
                StringBuilder last = new StringBuilder();
                last.append(ChatColor.RESET);
                last.append("@(");
                last.append(Math.round(warp.getX()));
                last.append(", ");
                last.append(Math.round(warp.getY()));
                last.append(", ");
                last.append(Math.round(warp.getZ()));
                last.append(")");
                return (FormattingUtils.twoColumnAlign(first.toString(), last.toString()));
            }

        });
        try {
            paginator.displayPage(sender, args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invalid-number", sender, args.getCommandString()));
        }
    }

    /**
     * Points the compass of the player to a warp.
     * 
     * @param args
     *            the command-arguments
     * @param player
     *            the player who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "point" }, usage = "[name]", desc = "commands.point.description", fee = Fee.POINT, permissions = { "mywarp.warp.basic.compass" })
    public void pointToWarp(CommandContext args, Player player) throws CommandException {
        if (args.argsLength() == 0) {
            player.setCompassTarget(player.getWorld().getSpawnLocation());
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.point.reset", player));
        } else {
            Warp warp = CommandUtils.getUsableWarp(player, args.getJoinedStrings(0));
            warp.asCompassTarget(player);

            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.point.set", player, warp.getName()));
        }
    }

    /**
     * Searches for warps.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "search" }, flags = "p", usage = "<name>", desc = "commands.search.description", fee = Fee.SEARCH, min = 1, permissions = { "mywarp.warp.basic.search" })
    public void searchWarps(CommandContext args, final CommandSender sender) throws CommandException {
        Matcher matcher = Matcher.match(args.getJoinedStrings(0), WarpUtils.isViewable(sender));
        Warp exactMatch = matcher.getExactMatch();
        Collection<Warp> matches = args.hasFlag('p') ? matcher.getMatches(new Warp.PopularityComparator())
                : matcher.getMatches();

        if (exactMatch == null && matches.isEmpty()) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.search.no-matches", sender, args.getJoinedStrings(0)));
        } else {
            // REVIEW color warp names?
            sender.sendMessage(ChatColor.GOLD
                    + MyWarp.inst().getLocalizationManager()
                            .getString("commands.search.heading", sender, args.getJoinedStrings(0)));

            if (exactMatch != null) {
                sender.sendMessage(ChatColor.GRAY
                        + MyWarp.inst().getLocalizationManager()
                                .getString("commands.search.exact-heading", sender) + ": " + ChatColor.WHITE
                        + exactMatch.getName());
            }

            if (!matches.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY
                        + MyWarp.inst().getLocalizationManager()
                                .getString("commands.search.partital-heading", sender, matches.size()) + ": "
                        + ChatColor.WHITE + CommandUtils.joinWarps(matches));
            }
        }

    }

    /**
     * Changes the welcome-message of a warp.
     * 
     * @param args
     *            the command-arguments
     * @param player
     *            the player who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "welcome" }, usage = "<name>", desc = "commands.welcome.description", fee = Fee.WELCOME, min = 1, permissions = { "mywarp.warp.basic.welcome" })
    public void setWarpWelcome(CommandContext args, Player player) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(player, args.getJoinedStrings(0));

        WelcomeMessageConversation.initiate(player, warp);
    }

    /**
     * Displays the command-help that lists all commands available for the
     * sender.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "help" }, usage = "#", desc = "commands.help.description", fee = Fee.HELP, max = 1, permissions = { "mywarp.warp.basic.help" })
    public void showHelp(final CommandContext args, final CommandSender sender) throws CommandException {
        StringPaginator<Command> paginator = StringPaginator.of(MyWarp.inst().getCommandsManager()
                .getUsableCommands(sender, "warp"), MyWarp.inst().getLocalizationManager()
                .getColorlessString("commands.help.heading", sender));
        paginator.withNote(MyWarp.inst().getLocalizationManager()
                .getColorlessString("commands.help.note", sender));
        paginator.withMapping(new Function<Command, String>() {

            @Override
            public String apply(Command cmd) {
                // /root sub|sub [flags] <args>
                StrBuilder ret = new StrBuilder();
                ret.append(ChatColor.GOLD);
                ret.append("/");
                ret.append(args.getCommand()[0]);
                ret.append(" ");
                ret.appendWithSeparators(cmd.aliases(), "|");
                ret.append(" ");
                ret.append(ChatColor.GRAY);
                ret.append(MyWarp.inst().getCommandsManager().getArguments(cmd, sender));
                return (ret.toString());
            }

        });
        try {
            paginator.displayPage(sender, args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invalid-number", sender, args.getCommandString()));
        }
    }

    /**
     * Updates the location of a warp.
     * 
     * @param args
     *            the command-arguments
     * @param player
     *            the player who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "update" }, usage = "<name>", desc = "commands.update.description", fee = Fee.UPDATE, min = 1, permissions = { "mywarp.warp.basic.update" })
    public void updateWarp(CommandContext args, Player player) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(player, args.getJoinedStrings(0));

        warp.setLocation(player.getLocation());
        player.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.update.update-successful", player, warp.getName()));
    }

    /**
     * Displays information about a warp.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "info", "stats" }, usage = "<name>", desc = "commands.info.description", fee = Fee.INFO, min = 1, permissions = { "mywarp.warp.basic.info" })
    public void showWarpInfo(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getViewableWarp(sender, args.getJoinedStrings(0));

        StrBuilder infos = new StrBuilder();

        infos.append(ChatColor.GOLD);
        infos.append(MyWarp
                .inst()
                .getLocalizationManager()
                .getString("commands.info.heading", sender,
                        warp.getType().getColor() + warp.getName() + ChatColor.GOLD));
        infos.appendNewLine();

        infos.append(ChatColor.GRAY);
        infos.append(MyWarp.inst().getLocalizationManager().getString("commands.info.created-by", sender));
        infos.append(" ");
        infos.append(ChatColor.WHITE);
        // REVIEW also show the UUID?
        infos.append(warp.getCreator().getName());
        if (sender instanceof Player && warp.isCreator((Player) sender)) {
            infos.append(" ");
            infos.append(MyWarp.inst().getLocalizationManager()
                    .getString("commands.info.created-by-you", sender));
        }
        infos.appendNewLine();

        infos.append(ChatColor.GRAY);
        infos.append(MyWarp.inst().getLocalizationManager().getString("commands.info.location", sender));
        infos.append(" ");
        infos.append(ChatColor.WHITE);
        infos.append(Math.round(warp.getX()));
        infos.append(", ");
        infos.append(Math.round(warp.getY()));
        infos.append(", ");
        infos.append(Math.round(warp.getZ()));
        infos.append(" ");
        infos.append(MyWarp.inst().getLocalizationManager()
                .getString("commands.info.in-world", sender, warp.getWorld().getName()));
        infos.appendNewLine();

        if (warp.isModifiable(sender)) {
            infos.append(ChatColor.GRAY);
            infos.append(MyWarp.inst().getLocalizationManager()
                    .getString("commands.info.invited-players", sender));
            infos.append(" ");
            infos.append(ChatColor.WHITE);

            Set<UUID> invitedPlayerIds = warp.getInvitedPlayerIds();
            if (invitedPlayerIds.isEmpty()) {
                infos.append("-");
            } else {
                List<String> invitedPlayerNames = new ArrayList<String>();
                for (UUID playerId : invitedPlayerIds) {
                    String name = MyWarp.server().getOfflinePlayer(playerId).getName();
                    invitedPlayerNames.add(name);
                }
                Collections.sort(invitedPlayerNames);
                infos.appendWithSeparators(invitedPlayerNames, ", ");
            }
            infos.appendNewLine();

            infos.append(ChatColor.GRAY);
            infos.append(MyWarp.inst().getLocalizationManager()
                    .getString("commands.info.invited-groups", sender));
            infos.append(" ");
            infos.append(ChatColor.WHITE);

            List<String> invitedGroups = Ordering.natural().sortedCopy(warp.getInvitedGroups());
            if (invitedGroups.isEmpty()) {
                infos.append("-");
            } else {
                infos.appendWithSeparators(invitedGroups, ", ");
            }
            infos.appendNewLine();
        }

        infos.append(ChatColor.GRAY);
        infos.append(MyWarp.inst().getLocalizationManager()
                .getString("commands.info.creation-date", sender, warp.getCreationDate()));
        infos.appendNewLine();

        infos.append(ChatColor.GRAY);
        infos.append(MyWarp.inst().getLocalizationManager()
                .getString("commands.info.visits", sender, warp.getVisits(), warp.getVisitsPerDay()));

        sender.sendMessage(infos.toString());
    }
}

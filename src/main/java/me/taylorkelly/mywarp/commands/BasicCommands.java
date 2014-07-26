package me.taylorkelly.mywarp.commands;

import java.util.Arrays;
import java.util.Collection;
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
import me.taylorkelly.mywarp.data.WelcomeMessageHandler;
import me.taylorkelly.mywarp.economy.FeeBundle.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.FormattingUtils;
import me.taylorkelly.mywarp.utils.Matcher;
import me.taylorkelly.mywarp.utils.PaginatedResult;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    @Command(aliases = { "pcreate", "pset" }, usage = "<name>", desc = "commands.create-private.description", fee = Fee.CREATE_PRIVATE, min = 1, permissions = { "mywarp.warp.basic.createprivate" })
    public void createPrivateWarp(CommandContext args, Player sender) throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkLimits(sender, true, Type.PRIVATE);
        CommandUtils.checkWarpname(sender, name);

        MyWarp.inst().getWarpManager().addWarp(name, sender, Type.PRIVATE);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.create-private.created-successful", sender, name));
    }

    @Command(aliases = { "create", "set" }, usage = "<name>", desc = "commands.create.description", fee = Fee.CREATE, min = 1, permissions = { "mywarp.warp.basic.createpublic" })
    public void createPublicWarp(CommandContext args, Player sender) throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkLimits(sender, true, Type.PUBLIC);
        CommandUtils.checkWarpname(sender, name);

        MyWarp.inst().getWarpManager().addWarp(name, sender, Type.PUBLIC);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.create.created-successful", sender, name));
    }

    @Command(aliases = { "delete", "remove" }, usage = "<name>", desc = "commands.delete.description", fee = Fee.DELETE, min = 1, permissions = { "mywarp.warp.basic.delete" })
    public void deleteWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(0));

        MyWarp.inst().getWarpManager().deleteWarp(warp);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.delete.deleted-successful", sender, warp.getName()));
    }

    //TODO add support for usage with disabled limits
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

        Table<LimitBundle, Warp.Type, SortedSet<Warp>> mappedWarps = ArrayTable.create(MyWarp.inst()
                .getPermissionsManager().getLimitBundleManager().getAffectiveBundles(player),
                Arrays.asList(Warp.Type.values()));
        for (Warp warp : MyWarp.inst().getWarpManager().getWarps(new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isCreator(player);
            }

        })) {
            for (LimitBundle bundle : mappedWarps.rowKeySet()) {
                if (!bundle.getAffectedWorlds().contains(warp.getWorld())) {
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

            if (publicWarps == null) {
                publicWarps = Sets.newTreeSet();
            }
            if (privateWarps == null) {
                privateWarps = Sets.newTreeSet();
            }

            // create the strings
            String publicEntry = MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.assets.public-warps", sender,
                            publicWarps.size() + "/" + entry.getKey().getLimit(LimitBundle.Limit.PUBLIC),
                            CommandUtils.joinWarps(publicWarps));
            String privateEntry = MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.assets.private-warps", sender,
                            privateWarps.size() + "/" + entry.getKey().getLimit(LimitBundle.Limit.PRIVATE),
                            CommandUtils.joinWarps(privateWarps));

            // send the messages
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString(
                            "commands.assets.total-warps",
                            sender,
                            CommandUtils.joinWorlds(entry.getKey().getAffectedWorlds()),
                            (privateWarps.size() + publicWarps.size()) + "/"
                                    + entry.getKey().getLimit(LimitBundle.Limit.TOTAL)));
            sender.sendMessage(FormattingUtils.toList(publicEntry, privateEntry));
        }
    }

    @Command(aliases = { "list", "alist" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "commands.list.description", fee = Fee.LIST, max = 1, permissions = { "mywarp.warp.basic.list" })
    public void listWarps(final CommandContext args, final CommandSender sender) throws CommandException {
        final OfflinePlayer creator = args.hasFlag('c') ? MyWarp.inst().getWarpManager()
                .getMatchingCreator(args.getFlag('c'), new Predicate<Warp>() {

                    @Override
                    public boolean apply(Warp warp) {
                        return warp.isViewable(sender);
                    }

                }) : null;
        final World world = args.hasFlag('w') ? MyWarp.inst().getWarpManager()
                .getMatchingWorld(args.getFlag('w'), new Predicate<Warp>() {

                    @Override
                    public boolean apply(Warp warp) {
                        return warp.isViewable(sender);
                    }

                }) : null;

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

        PaginatedResult<Warp> warpList = new PaginatedResult<Warp>(MyWarp.inst().getLocalizationManager()
                .getColorlessString("commands.list.heading", sender)
                + ", ") {

            @Override
            public String format(Warp warp, CommandSender sender) {
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
                last.append(warp.getY());
                last.append(", ");
                last.append(Math.round(warp.getZ()));
                last.append(")");
                return (FormattingUtils.twoColumnAlign(first.toString(), last.toString()));
            }
        };

        try {
            warpList.display(sender, results, args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invalid-number", sender, args.getCommandString()));
        }
    }

    @Command(aliases = { "point" }, usage = "[name]", desc = "commands.point.description", fee = Fee.POINT, permissions = { "mywarp.warp.basic.compass" })
    public void pointToWarp(CommandContext args, Player sender) throws CommandException {
        if (args.argsLength() == 0) {
            sender.setCompassTarget(sender.getWorld().getSpawnLocation());
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.point.reset", sender));
        } else {
            Warp warp = CommandUtils.getUsableWarp(sender, args.getJoinedStrings(0));
            warp.asCompassTarget(sender);

            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.point.set", sender, warp.getName()));
        }
    }

    // XXX color warp names
    @Command(aliases = { "search" }, flags = "p", usage = "<name>", desc = "commands.search.description", fee = Fee.SEARCH, min = 1, permissions = { "mywarp.warp.basic.search" })
    public void searchWarps(CommandContext args, final CommandSender sender) throws CommandException {
        Matcher matcher = Matcher.match(args.getJoinedStrings(0), new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isViewable(sender);
            }

        });
        Warp exactMatch = matcher.getExactMatch();
        Collection<Warp> matches = args.hasFlag('p') ? matcher.getMatches(new Warp.PopularityComparator())
                : matcher.getMatches();

        if (exactMatch == null && matches.isEmpty()) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.search.no-matches", sender, args.getJoinedStrings(0)));
        } else {
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

    @Command(aliases = { "welcome" }, usage = "<name>", desc = "commands.welcome.description", fee = Fee.WELCOME, min = 1, permissions = { "mywarp.warp.basic.welcome" })
    public void setWarpWelcome(CommandContext args, Player sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(0));

        WelcomeMessageHandler.initiateWelcomeMessageChange(sender, warp);
    }

    @Command(aliases = { "help" }, usage = "#", desc = "commands.help.description", fee = Fee.HELP, max = 1, permissions = { "mywarp.warp.basic.help" })
    public void showHelp(final CommandContext args, CommandSender sender) throws CommandException {
        PaginatedResult<Command> cmdList = new PaginatedResult<Command>(MyWarp.inst()
                .getLocalizationManager().getColorlessString("commands.help.heading", sender)
                + ", ", MyWarp.inst().getLocalizationManager()
                .getColorlessString("commands.help.note", sender)) {

            @Override
            public String format(Command cmd, CommandSender sender) {
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
        };

        try {
            cmdList.display(sender, MyWarp.inst().getCommandsManager().getUsableCommands(sender, "warp"),
                    args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invalid-number", sender, args.getCommandString()));
        }
    }

    @Command(aliases = { "update" }, usage = "<name>", desc = "commands.update.description", fee = Fee.UPDATE, min = 1, permissions = { "mywarp.warp.basic.update" })
    public void updateWarp(CommandContext args, Player sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(0));

        warp.setLocation(sender.getLocation());
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.update.update-successful", sender, warp.getName()));
    }

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
        infos.append(warp.getY());
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

            // TODO use Ordering.natural().onResultsOf(new Function<UUID,
            // String>(){...}.sortedCopy(warp.getInvitedPlayerIds());
            Set<UUID> invitedPlayerIds = warp.getInvitedPlayerIds();
            if (invitedPlayerIds.isEmpty()) {
                infos.append("-");
            } else {
                SortedSet<String> invitedPlayerNames = new TreeSet<String>();
                for (UUID playerId : invitedPlayerIds) {
                    // FIXME investigate this bug.
                    String name = MyWarp.server().getOfflinePlayer(playerId).getName();
                    name = (name == null) ? "unknown-uuid:" + playerId.toString() : name;

                    invitedPlayerNames.add(name);
                }
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
        infos.append(MyWarp.inst().getLocalizationManager().getString("commands.info.visits", sender));
        infos.append(" ");
        infos.append(ChatColor.WHITE);
        infos.append(warp.getVisits());

        sender.sendMessage(infos.toString());
    }
}

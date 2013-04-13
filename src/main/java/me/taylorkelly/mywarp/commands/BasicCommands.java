package me.taylorkelly.mywarp.commands;

import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.MatchList;
import me.taylorkelly.mywarp.utils.MinecraftFontWidthCalculator;
import me.taylorkelly.mywarp.utils.PaginatedResult;
import me.taylorkelly.mywarp.utils.PopularityWarpComparator;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.Command;

/**
 * This class contains all commands that cover basic tasks. They should be
 * included in the <code>mywarp.warp.basic.*</code> permission container.
 * 
 */
public class BasicCommands {

    private final MyWarp plugin;

    public BasicCommands(MyWarp plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = { "pcreate", "pset" }, usage = "<name>", desc = "cmd.description.createPrivate", fee = Fee.CREATE_PRIVATE, min = 1, permissions = { "mywarp.warp.basic.createprivate" })
    public void createPrivateWarp(CommandContext args, Player sender)
            throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkTotalLimit(sender);
        CommandUtils.checkPrivateLimit(sender);

        if (plugin.getWarpList().warpExists(name)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.create.warpExists", "%warp%", name));
        }

        plugin.getWarpList().addWarpPrivate(name, sender);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.create.private", "%warp%", name));
    }

    @Command(aliases = { "create", "set" }, usage = "<name>", desc = "cmd.description.create", fee = Fee.CREATE, min = 1, permissions = { "mywarp.warp.basic.createpublic" })
    public void createPublicWarp(CommandContext args, Player sender)
            throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkTotalLimit(sender);
        CommandUtils.checkPublicLimit(sender);

        if (plugin.getWarpList().warpExists(name)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.create.warpExists", "%warp%", name));
        }

        plugin.getWarpList().addWarpPublic(name, sender);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.create.public", "%warp%", name));
    }

    @Command(aliases = { "delete", "remove" }, usage = "<name>", desc = "cmd.description.delete", fee = Fee.DELETE, min = 1, permissions = { "mywarp.warp.basic.delete" })
    public void deleteWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));

        plugin.getWarpList().deleteWarp(warp);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.delete",
                "%warp%", warp.name));
    }

    @Command(aliases = { "alist" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "cmd.description.listAll", fee = Fee.LISTALL, max = 0, permissions = { "mywarp.warp.basic.list" })
    public void listAllWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        TreeSet<Warp> results = plugin.getWarpList().warpsInvitedTo(player,
                args.getFlag('c'), args.getFlag('w'),
                args.hasFlag('p') ? new PopularityWarpComparator() : null);

        if (results.isEmpty()) {
            throw new CommandException(
                    LanguageManager.getString("lister.noResults"));
        }
        sender.sendMessage(LanguageManager.getString("listAll.list"));

        StrBuilder ret = new StrBuilder();
        for (Warp warp : results) {
            ret.appendSeparator(", ");
            if (sender instanceof Player
                    && warp.creator.equals(sender.getName())) {
                ret.append(ChatColor.AQUA);
            } else if (warp.publicAll) {
                ret.append(ChatColor.GREEN);
            } else {
                ret.append(ChatColor.RED);
            }
            ret.append(warp.name);
            ret.append(ChatColor.RESET);
        }
        sender.sendMessage(ret.toString());
    }

    @Command(aliases = { "list" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "cmd.description.list", fee = Fee.LIST, max = 1, permissions = { "mywarp.warp.basic.list" })
    public void listWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        TreeSet<Warp> results = plugin.getWarpList().warpsInvitedTo(player,
                args.getFlag('c'), args.getFlag('w'),
                args.hasFlag('p') ? new PopularityWarpComparator() : null);

        PaginatedResult<Warp> cmdList = new PaginatedResult<Warp>(
                LanguageManager.getColorlessString("lister.warp.head") + ", ") {

            @Override
            public String format(Warp warp, CommandSender sender) {
                // 'name'(+) by player
                StringBuilder first = new StringBuilder();

                if (sender instanceof Player
                        && warp.creator.equals(sender.getName())) {
                    first.append(ChatColor.AQUA);
                } else if (warp.publicAll) {
                    first.append(ChatColor.GREEN);
                } else {
                    first.append(ChatColor.RED);
                }
                first.append("'");
                first.append(warp.name);
                first.append("'");
                first.append(ChatColor.WHITE);
                first.append(" (");
                first.append(warp.publicAll ? "+" : "-");
                first.append(") ");
                first.append(LanguageManager
                        .getColorlessString("lister.warp.by"));
                first.append(" ");
                first.append(ChatColor.ITALIC);

                if (sender instanceof Player
                        && warp.creator.equals(sender.getName())) {
                    first.append(LanguageManager
                            .getColorlessString("lister.warp.you"));
                } else {
                    first.append(warp.creator);
                }
                // @(x, y, z)
                StringBuilder last = new StringBuilder();
                last.append(ChatColor.RESET);
                last.append("@(");
                last.append((int) warp.x);
                last.append(", ");
                last.append((int) warp.y);
                last.append(", ");
                last.append((int) warp.z);
                last.append(")");
                return (MinecraftFontWidthCalculator.rightLeftAlign(
                        first.toString(), last.toString()));
            }
        };

        try {
            cmdList.display(sender, results, args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.cmd.invalidNumber", "%command%",
                    StringUtils.join(args.getCommand(), ' ')));
        }
    }

    @Command(aliases = { "points" }, usage = "<name>", desc = "cmd.description.point", fee = Fee.POINT, min = 1, permissions = { "mywarp.warp.basic.compass" })
    public void pointToWarp(CommandContext args, Player sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForUsage(sender,
                args.getJoinedStrings(0));

        plugin.getWarpList().point(warp, sender);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.point",
                "%warp%", warp.name));
    }

    @Command(aliases = { "search" }, flags = "p", usage = "<name>", desc = "cmd.description.search", fee = Fee.SEARCH, min = 1, permissions = { "mywarp.warp.basic.search" })
    public void searchWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        MatchList matches = plugin.getWarpList().getMatches(
                args.getJoinedStrings(0),
                sender instanceof Player ? (Player) sender : null,
                args.hasFlag('p') ? new PopularityWarpComparator() : null);

        if (matches.exactMatches.size() == 0 && matches.matches.size() == 0) {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "search.noMatches", "%query%", args.getJoinedStrings(0)));
        } else {
            if (matches.exactMatches.size() > 0) {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "search.exactMatches", "%query%",
                        args.getJoinedStrings(0)));
                sendWarpMatches(matches.exactMatches, sender);
            }
            if (matches.matches.size() > 0) {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "search.partitalMatches", "%query%",
                        args.getJoinedStrings(0)));
                sendWarpMatches(matches.matches, sender);
            }
        }
    }

    @Command(aliases = { "welcome" }, usage = "<name>", desc = "cmd.description.welcome", fee = Fee.WELCOME, min = 1, permissions = { "mywarp.warp.basic.welcome" })
    public void setWarpWelcome(CommandContext args, Player sender)
            throws CommandException {

        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));

        plugin.getWarpList().welcomeMessage(warp, sender);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.welcome.enter", "%warp%", warp.name));
    }

    @Command(aliases = { "help" }, usage = "#", desc = "cmd.description.help", fee = Fee.HELP, max = 1, permissions = { "mywarp.warp.basic.help" })
    public void showHelp(final CommandContext args, CommandSender sender)
            throws CommandException {
        PaginatedResult<Command> cmdList = new PaginatedResult<Command>(
                LanguageManager.getColorlessString("lister.help.head") + ", ") {

            @Override
            public String format(Command cmd, CommandSender sender) {
                // /root sub|sub [flags] <args>
                StringBuilder ret = new StringBuilder();
                ret.append(ChatColor.GOLD);
                ret.append("/");
                ret.append(args.getCommand()[0]);
                ret.append(" ");
                ret.append(StringUtils.join(cmd.aliases(), '|'));
                ret.append(" ");
                ret.append(ChatColor.GRAY);
                ret.append(plugin.getCommandsManager().getArguments(cmd));
                return (ret.toString());
            }
        };

        try {
            cmdList.display(sender, plugin.getCommandsManager()
                    .getUsableCommands(sender, "warp"), args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.cmd.invalidNumber", "%command%",
                    StringUtils.join(args.getCommand(), ' ')));
        }
    }

    @Command(aliases = { "update" }, usage = "<name>", desc = "cmd.description.update", fee = Fee.UPDATE, min = 1, permissions = { "mywarp.warp.basic.update" })
    public void updateWarp(CommandContext args, Player sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));

        plugin.getWarpList().updateLocation(warp, sender);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.update",
                "%warp%", warp.name));
    }

    private void sendWarpMatches(TreeSet<Warp> warps, CommandSender sender) {
        for (Warp warp : warps) {
            StringBuilder ret = new StringBuilder();
            if (sender instanceof Player
                    && warp.creator.equals(sender.getName())) {
                ret.append(ChatColor.AQUA);
            } else if (warp.publicAll) {
                ret.append(ChatColor.GREEN);
            } else {
                ret.append(ChatColor.RED);
            }
            ret.append("'");
            ret.append(warp.name);
            ret.append("'");
            ret.append(ChatColor.WHITE);
            ret.append(" ");
            ret.append(LanguageManager.getColorlessString("lister.warp.by"));
            ret.append(" ");
            ret.append(ChatColor.ITALIC);

            if (sender instanceof Player
                    && warp.creator.equals(sender.getName())) {
                ret.append(LanguageManager
                        .getColorlessString("lister.warp.you"));
            } else {
                ret.append(warp.creator);
            }
            ret.append(" ");
            ret.append(ChatColor.RESET);
            ret.append("@(");
            ret.append((int) warp.x);
            ret.append(", ");
            ret.append((int) warp.y);
            ret.append(", ");
            ret.append((int) warp.z);
            ret.append(")");
            sender.sendMessage(ret.toString());
        }
    }
}

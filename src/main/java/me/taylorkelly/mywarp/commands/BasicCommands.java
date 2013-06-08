package me.taylorkelly.mywarp.commands;

import java.util.TreeSet;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.MatchList;
import me.taylorkelly.mywarp.utils.MinecraftFontWidthCalculator;
import me.taylorkelly.mywarp.utils.PaginatedResult;
import me.taylorkelly.mywarp.utils.PopularityWarpComparator;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class contains all commands that cover basic tasks. They should be
 * included in the <code>mywarp.warp.basic.*</code> permission container.
 * 
 */
public class BasicCommands {

    @Command(aliases = { "pcreate", "pset" }, usage = "<name>", desc = "cmd.description.createPrivate", fee = Fee.CREATE_PRIVATE, min = 1, permissions = { "mywarp.warp.basic.createprivate" })
    public void createPrivateWarp(CommandContext args, Player sender)
            throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkTotalLimit(sender);
        CommandUtils.checkPrivateLimit(sender);

        if (MyWarp.inst().getWarpList().warpExists(name)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("error.create.warpExists", "%warp%",
                            name));
        }

        MyWarp.inst().getWarpList().addWarpPrivate(name, sender);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.create.private", "%warp%", name));
    }

    @Command(aliases = { "create", "set" }, usage = "<name>", desc = "cmd.description.create", fee = Fee.CREATE, min = 1, permissions = { "mywarp.warp.basic.createpublic" })
    public void createPublicWarp(CommandContext args, Player sender)
            throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkTotalLimit(sender);
        CommandUtils.checkPublicLimit(sender);

        if (MyWarp.inst().getWarpList().warpExists(name)) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("error.create.warpExists", "%warp%",
                            name));
        }

        MyWarp.inst().getWarpList().addWarpPublic(name, sender);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.create.public", "%warp%", name));
    }

    @Command(aliases = { "delete", "remove" }, usage = "<name>", desc = "cmd.description.delete", fee = Fee.DELETE, min = 1, permissions = { "mywarp.warp.basic.delete" })
    public void deleteWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));

        MyWarp.inst().getWarpList().deleteWarp(warp);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.delete", "%warp%", warp.getName()));
    }

    @Command(aliases = { "alist" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "cmd.description.listAll", fee = Fee.LISTALL, max = 0, permissions = { "mywarp.warp.basic.list" })
    public void listAllWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        TreeSet<Warp> results = MyWarp
                .inst()
                .getWarpList()
                .warpsInvitedTo(
                        player,
                        args.getFlag('c'),
                        args.getFlag('w'),
                        args.hasFlag('p') ? new PopularityWarpComparator()
                                : null);

        if (results.isEmpty()) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getString("lister.noResults"));
        }
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getString("listAll.list"));

        StrBuilder ret = new StrBuilder();
        for (Warp warp : results) {
            ret.appendSeparator(", ");
            if (sender instanceof Player
                    && warp.getCreator().equals(sender.getName())) {
                ret.append(ChatColor.AQUA);
            } else if (warp.isPublicAll()) {
                ret.append(ChatColor.GREEN);
            } else {
                ret.append(ChatColor.RED);
            }
            ret.append(warp.getName());
            ret.append(ChatColor.RESET);
        }
        sender.sendMessage(ret.toString());
    }

    @Command(aliases = { "list" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "cmd.description.list", fee = Fee.LIST, max = 1, permissions = { "mywarp.warp.basic.list" })
    public void listWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        TreeSet<Warp> results = MyWarp
                .inst()
                .getWarpList()
                .warpsInvitedTo(
                        player,
                        args.getFlag('c'),
                        args.getFlag('w'),
                        args.hasFlag('p') ? new PopularityWarpComparator()
                                : null);

        PaginatedResult<Warp> cmdList = new PaginatedResult<Warp>(MyWarp.inst()
                .getLanguageManager().getColorlessString("lister.warp.head")
                + ", ") {

            @Override
            public String format(Warp warp, CommandSender sender) {
                // 'name'(+) by player
                StringBuilder first = new StringBuilder();

                if (sender instanceof Player
                        && warp.getCreator().equals(sender.getName())) {
                    first.append(ChatColor.AQUA);
                } else if (warp.isPublicAll()) {
                    first.append(ChatColor.GREEN);
                } else {
                    first.append(ChatColor.RED);
                }
                first.append("'");
                first.append(warp.getName());
                first.append("'");
                first.append(ChatColor.WHITE);
                first.append(" (");
                first.append(warp.isPublicAll() ? "+" : "-");
                first.append(") ");
                first.append(MyWarp.inst().getLanguageManager()
                        .getColorlessString("lister.warp.by"));
                first.append(" ");
                first.append(ChatColor.ITALIC);

                if (sender instanceof Player
                        && warp.getCreator().equals(sender.getName())) {
                    first.append(MyWarp.inst().getLanguageManager()
                            .getColorlessString("lister.warp.you"));
                } else {
                    first.append(warp.getCreator());
                }
                // @(x, y, z)
                StringBuilder last = new StringBuilder();
                last.append(ChatColor.RESET);
                last.append("@(");
                last.append((int) warp.getX());
                last.append(", ");
                last.append((int) warp.getY());
                last.append(", ");
                last.append((int) warp.getZ());
                last.append(")");
                return (MinecraftFontWidthCalculator.rightLeftAlign(
                        first.toString(), last.toString()));
            }
        };

        try {
            cmdList.display(sender, results, args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("error.cmd.invalidNumber", "%command%",
                            StringUtils.join(args.getCommand(), ' ')));
        }
    }

    @Command(aliases = { "point" }, usage = "[name]", desc = "cmd.description.point", fee = Fee.POINT, permissions = { "mywarp.warp.basic.compass" })
    public void pointToWarp(CommandContext args, Player sender)
            throws CommandException {
        if (args.argsLength() == 0) {
            sender.setCompassTarget(sender.getWorld().getSpawnLocation());
            sender.sendMessage(MyWarp.inst().getLanguageManager()
                    .getString("warp.point.reset"));
        } else {
            Warp warp = CommandUtils.getWarpForUsage(sender,
                    args.getJoinedStrings(0));

            MyWarp.inst().getWarpList().point(warp, sender);
            sender.sendMessage(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("warp.point", "%warp%", warp.getName()));
        }
    }

    @Command(aliases = { "search" }, flags = "p", usage = "<name>", desc = "cmd.description.search", fee = Fee.SEARCH, min = 1, permissions = { "mywarp.warp.basic.search" })
    public void searchWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        MatchList matches = MyWarp
                .inst()
                .getWarpList()
                .getMatches(
                        args.getJoinedStrings(0),
                        sender instanceof Player ? (Player) sender : null,
                        args.hasFlag('p') ? new PopularityWarpComparator()
                                : null);

        if (matches.exactMatches.size() == 0 && matches.matches.size() == 0) {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("search.noMatches", "%query%",
                            args.getJoinedStrings(0)));
        } else {
            if (matches.exactMatches.size() > 0) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("search.exactMatches", "%query%",
                                args.getJoinedStrings(0)));
                sendWarpMatches(matches.exactMatches, sender);
            }
            if (matches.matches.size() > 0) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("search.partitalMatches",
                                "%query%", args.getJoinedStrings(0)));
                sendWarpMatches(matches.matches, sender);
            }
        }
    }

    @Command(aliases = { "welcome" }, usage = "<name>", desc = "cmd.description.welcome", fee = Fee.WELCOME, min = 1, permissions = { "mywarp.warp.basic.welcome" })
    public void setWarpWelcome(CommandContext args, Player sender)
            throws CommandException {

        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));

        MyWarp.inst().getWarpList().welcomeMessage(warp, sender);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.welcome.enter", "%warp%", warp.getName()));
    }

    @Command(aliases = { "help" }, usage = "#", desc = "cmd.description.help", fee = Fee.HELP, max = 1, permissions = { "mywarp.warp.basic.help" })
    public void showHelp(final CommandContext args, CommandSender sender)
            throws CommandException {
        PaginatedResult<Command> cmdList = new PaginatedResult<Command>(MyWarp
                .inst().getLanguageManager()
                .getColorlessString("lister.help.head")
                + ", ") {

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
                ret.append(MyWarp.inst().getCommandsManager().getArguments(cmd));
                return (ret.toString());
            }
        };

        try {
            cmdList.display(sender, MyWarp.inst().getCommandsManager()
                    .getUsableCommands(sender, "warp"), args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("error.cmd.invalidNumber", "%command%",
                            StringUtils.join(args.getCommand(), ' ')));
        }
    }

    @Command(aliases = { "update" }, usage = "<name>", desc = "cmd.description.update", fee = Fee.UPDATE, min = 1, permissions = { "mywarp.warp.basic.update" })
    public void updateWarp(CommandContext args, Player sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));

        MyWarp.inst().getWarpList().updateLocation(warp, sender);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.update", "%warp%", warp.getName()));
    }

    private void sendWarpMatches(TreeSet<Warp> warps, CommandSender sender) {
        for (Warp warp : warps) {
            StringBuilder ret = new StringBuilder();
            if (sender instanceof Player
                    && warp.getCreator().equals(sender.getName())) {
                ret.append(ChatColor.AQUA);
            } else if (warp.isPublicAll()) {
                ret.append(ChatColor.GREEN);
            } else {
                ret.append(ChatColor.RED);
            }
            ret.append("'");
            ret.append(warp.getName());
            ret.append("'");
            ret.append(ChatColor.WHITE);
            ret.append(" ");
            ret.append(MyWarp.inst().getLanguageManager()
                    .getColorlessString("lister.warp.by"));
            ret.append(" ");
            ret.append(ChatColor.ITALIC);

            if (sender instanceof Player
                    && warp.getCreator().equals(sender.getName())) {
                ret.append(MyWarp.inst().getLanguageManager()
                        .getColorlessString("lister.warp.you"));
            } else {
                ret.append(warp.getCreator());
            }
            ret.append(" ");
            ret.append(ChatColor.RESET);
            ret.append("@(");
            ret.append((int) warp.getX());
            ret.append(", ");
            ret.append((int) warp.getY());
            ret.append(", ");
            ret.append((int) warp.getZ());
            ret.append(")");
            sender.sendMessage(ret.toString());
        }
    }
}

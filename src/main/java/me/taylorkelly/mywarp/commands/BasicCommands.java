package me.taylorkelly.mywarp.commands;

import java.util.Collection;
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

    @Command(aliases = { "pcreate", "pset" }, usage = "<name>", desc = "commands.create-private.description", fee = Fee.CREATE_PRIVATE, min = 1, permissions = { "mywarp.warp.basic.createprivate" })
    public void createPrivateWarp(CommandContext args, Player sender) throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkTotalLimit(sender);
        CommandUtils.checkPrivateLimit(sender);
        CommandUtils.checkWarpname(sender, name);

        MyWarp.inst().getWarpManager().addWarpPrivate(name, sender);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.create-private.created-succesfull", sender, name));
    }

    @Command(aliases = { "create", "set" }, usage = "<name>", desc = "commands.create.description", fee = Fee.CREATE, min = 1, permissions = { "mywarp.warp.basic.createpublic" })
    public void createPublicWarp(CommandContext args, Player sender) throws CommandException {
        String name = args.getJoinedStrings(0);

        CommandUtils.checkTotalLimit(sender);
        CommandUtils.checkPublicLimit(sender);
        CommandUtils.checkWarpname(sender, name);

        MyWarp.inst().getWarpManager().addWarpPublic(name, sender);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.create.created-succesfull", sender, name));
    }

    @Command(aliases = { "delete", "remove" }, usage = "<name>", desc = "commands.delete.description", fee = Fee.DELETE, min = 1, permissions = { "mywarp.warp.basic.delete" })
    public void deleteWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));

        MyWarp.inst().getWarpManager().deleteWarp(warp);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.delete.deleted-succesfull", sender, warp.getName()));
    }

    @Command(aliases = { "assets", "pstats", "pinfo", "limits" }, usage = "[player]", desc = "commands.assets.description", fee = Fee.ASSETS, max = 1, permissions = { "mywarp.warp.basic.assets" })
    public void listPlayerWarps(CommandContext args, CommandSender sender) throws CommandException {
        Player player = null;
        if (args.argsLength() == 0) {
            player = CommandUtils.checkPlayer(sender);
        } else {
            CommandUtils.checkPermissions(sender, "mywarp.warp.basic.assets.other");
            player = CommandUtils.matchPlayer(sender, args.getString(0));
        }

        TreeSet<Warp> publicWarps = MyWarp.inst().getWarpManager().getWarps(true, player.getName());
        TreeSet<Warp> privateWarps = MyWarp.inst().getWarpManager().getWarps(false, player.getName());

        String header = MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.assets.heading", sender, player.getName());
        String publicHeader = MyWarp.inst().getLocalizationManager()
                .getString("commands.assets.private-heading", sender);
        String privateHeader = MyWarp.inst().getLocalizationManager()
                .getString("commands.assets.public-heading", sender);

        if (MyWarp.inst().getWarpSettings().limitsEnabled) {
            header = header
                    + " ("
                    + (publicWarps.size() + privateWarps.size())
                    + "/"
                    + (MyWarp.inst().getPermissionsManager()
                            .hasPermission(player, "mywarp.limit.total.unlimited") ? "-" : MyWarp.inst()
                            .getPermissionsManager().maxTotalWarps(player)) + ")";

            privateHeader = privateHeader
                    + " ("
                    + privateWarps.size()
                    + "/"
                    + (MyWarp.inst().getPermissionsManager()
                            .hasPermission(player, "mywarp.limit.private.unlimited") ? "-" : MyWarp.inst()
                            .getPermissionsManager().maxPrivateWarps(player)) + ")";

            publicHeader = publicHeader
                    + " ("
                    + publicWarps.size()
                    + "/"
                    + (MyWarp.inst().getPermissionsManager()
                            .hasPermission(player, "mywarp.limit.public.unlimited") ? "-" : MyWarp.inst()
                            .getPermissionsManager().maxPublicWarps(player)) + ")";
        }

        sender.sendMessage(ChatColor.GOLD + MinecraftFontWidthCalculator.centralize(" " + header + " ", '-'));
        sender.sendMessage(ChatColor.GRAY + publicHeader + ": " + ChatColor.WHITE
                + joinWarps(publicWarps, ", "));
        sender.sendMessage(ChatColor.GRAY + privateHeader + ": " + ChatColor.WHITE
                + joinWarps(privateWarps, ", "));
    }

    @Command(aliases = { "list", "alist" }, flags = "c:pw:", usage = "[-c creator] [-w world]", desc = "commands.list.description", fee = Fee.LIST, max = 1, permissions = { "mywarp.warp.basic.list" })
    public void listWarps(CommandContext args, CommandSender sender) throws CommandException {
        Player player = sender instanceof Player ? (Player) sender : null;
        TreeSet<Warp> results = MyWarp
                .inst()
                .getWarpManager()
                .warpsInvitedTo(player, args.getFlag('c'), args.getFlag('w'),
                        args.hasFlag('p') ? new PopularityWarpComparator() : null);

        PaginatedResult<Warp> cmdList = new PaginatedResult<Warp>(MyWarp.inst().getLocalizationManager()
                .getColorlessString("commands.list.heading", sender)
                + ", ") {

            @Override
            public String format(Warp warp, CommandSender sender) {
                // 'name'(+) by player
                StringBuilder first = new StringBuilder();

                if (sender instanceof Player && warp.getCreator().equals(sender.getName())) {
                    if (warp.isPublicAll()) {
                        first.append(ChatColor.AQUA);
                    } else {
                        first.append(ChatColor.DARK_AQUA);
                    }
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
                first.append(MyWarp.inst().getLocalizationManager()
                        .getColorlessString("commands.list.by", sender));
                first.append(" ");
                first.append(ChatColor.ITALIC);

                if (sender instanceof Player && warp.getCreator().equals(sender.getName())) {
                    first.append(MyWarp.inst().getLocalizationManager()
                            .getColorlessString("commands.list.you", sender));
                } else {
                    first.append(warp.getCreator());
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
                return (MinecraftFontWidthCalculator.rightLeftAlign(first.toString(), last.toString()));
            }
        };

        try {
            cmdList.display(sender, results, args.getInteger(0, 1));
        } catch (NumberFormatException e) {
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.invalid-number", sender,
                            StringUtils.join(args.getCommand(), ' ')));
        }
    }

    @Command(aliases = { "point" }, usage = "[name]", desc = "commands.point.description", fee = Fee.POINT, permissions = { "mywarp.warp.basic.compass" })
    public void pointToWarp(CommandContext args, Player sender) throws CommandException {
        if (args.argsLength() == 0) {
            sender.setCompassTarget(sender.getWorld().getSpawnLocation());
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.point.reset", sender));
        } else {
            Warp warp = CommandUtils.getWarpForUsage(sender, args.getJoinedStrings(0));

            MyWarp.inst().getWarpManager().point(warp, sender);
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.point.set", sender, warp.getName()));
        }
    }

    @Command(aliases = { "search" }, flags = "p", usage = "<name>", desc = "commands.search.description", fee = Fee.SEARCH, min = 1, permissions = { "mywarp.warp.basic.search" })
    public void searchWarps(CommandContext args, CommandSender sender) throws CommandException {
        MatchList matches = MyWarp
                .inst()
                .getWarpManager()
                .getMatches(args.getJoinedStrings(0), sender instanceof Player ? (Player) sender : null,
                        args.hasFlag('p') ? new PopularityWarpComparator() : null);

        if (matches.exactMatches.size() == 0 && matches.matches.size() == 0) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.search.no-matches", sender, args.getJoinedStrings(0)));
        } else {
            if (matches.exactMatches.size() > 0) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("commands.search.exact-matches", sender, args.getJoinedStrings(0)));
                sendWarpMatches(matches.exactMatches, sender);
            }
            if (matches.matches.size() > 0) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("commands.search.partital-matches", sender,
                                args.getJoinedStrings(0)));
                sendWarpMatches(matches.matches, sender);
            }
        }
    }

    @Command(aliases = { "welcome" }, usage = "<name>", desc = "commands.welcome.description", fee = Fee.WELCOME, min = 1, permissions = { "mywarp.warp.basic.welcome" })
    public void setWarpWelcome(CommandContext args, Player sender) throws CommandException {

        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));

        MyWarp.inst().getWarpManager().welcomeMessage(warp, sender);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.welcome.enter", sender, warp.getName()));
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
                StringBuilder ret = new StringBuilder();
                ret.append(ChatColor.GOLD);
                ret.append("/");
                ret.append(args.getCommand()[0]);
                ret.append(" ");
                ret.append(StringUtils.join(cmd.aliases(), '|'));
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
            throw new CommandException(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.invalid-number", sender,
                            StringUtils.join(args.getCommand(), ' ')));
        }
    }

    @Command(aliases = { "update" }, usage = "<name>", desc = "commands.update.description", fee = Fee.UPDATE, min = 1, permissions = { "mywarp.warp.basic.update" })
    public void updateWarp(CommandContext args, Player sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));

        warp.setLocation(sender.getLocation());
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.update.update-succesfull", sender, warp.getName()));
    }

    @Command(aliases = { "info", "stats" }, usage = "<name>", desc = "commands.info.description", fee = Fee.INFO, min = 1, permissions = { "mywarp.warp.basic.info" })
    public void showWarpInfo(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForUsage(sender, args.getJoinedStrings(0));
        StringBuilder infos = new StringBuilder();

        infos.append(ChatColor.GOLD);
        // color the warp depending on its visibility
        infos.append(MyWarp
                .inst()
                .getLocalizationManager()
                .getEffectiveString(
                        "commands.info.heading",
                        sender,
                        (warp.isPublicAll() ? ChatColor.GREEN : ChatColor.RED) + warp.getName()
                                + ChatColor.GOLD));
        infos.append("\n");

        infos.append(ChatColor.GRAY);
        infos.append(MyWarp.inst().getLocalizationManager().getString("commands.info.created-by", sender));
        infos.append(" ");
        infos.append(ChatColor.WHITE);
        infos.append(warp.getCreator());
        if (warp.getCreator().equals(sender.getName())) {
            infos.append(" ");
            infos.append(MyWarp.inst().getLocalizationManager()
                    .getString("commands.info.created-by-you", sender));
        }
        infos.append("\n");

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
                .getEffectiveString("commands.info.world", sender, warp.getWorld()));
        infos.append("\n");

        if (!(sender instanceof Player) || warp.playerCanModify((Player) sender)) {
            infos.append(ChatColor.GRAY);
            infos.append(MyWarp.inst().getLocalizationManager()
                    .getString("commands.info.invited-players", sender));
            infos.append(" ");
            infos.append(ChatColor.WHITE);
            infos.append(warp.getAllInvitedPlayers().isEmpty() ? "-" : StringUtils.join(
                    warp.getAllInvitedPlayers(), ", "));
            infos.append("\n");

            infos.append(ChatColor.GRAY);
            infos.append(MyWarp.inst().getLocalizationManager()
                    .getString("commands.info.invited-groups", sender));
            infos.append(" ");
            infos.append(ChatColor.WHITE);
            infos.append(warp.getAllInvitedGroups().isEmpty() ? "-" : StringUtils.join(
                    warp.getAllInvitedGroups(), ", "));
            infos.append("\n");
        }

        infos.append(ChatColor.GRAY);
        infos.append(MyWarp.inst().getLocalizationManager().getString("commands.info.visits", sender));
        infos.append(" ");
        infos.append(ChatColor.WHITE);
        infos.append(warp.getVisits());

        sender.sendMessage(infos.toString());
    }

    private void sendWarpMatches(TreeSet<Warp> warps, CommandSender sender) {
        for (Warp warp : warps) {
            StringBuilder ret = new StringBuilder();
            if (sender instanceof Player && warp.getCreator().equals(sender.getName())) {
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
            ret.append(MyWarp.inst().getLocalizationManager().getColorlessString("lister.warp.by", sender));
            ret.append(" ");
            ret.append(ChatColor.ITALIC);

            if (sender instanceof Player && warp.getCreator().equals(sender.getName())) {
                ret.append(MyWarp.inst().getLocalizationManager()
                        .getColorlessString("lister.warp.you", sender));
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

    private String joinWarps(Collection<Warp> warps, String separator) {
        if (warps.isEmpty()) {
            return "-";
        }

        StrBuilder ret = new StrBuilder();
        for (Warp warp : warps) {
            ret.appendSeparator(separator);
            ret.append(warp.getName());
        }
        return ret.toString();
    }
}

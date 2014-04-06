package me.taylorkelly.mywarp.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.ConnectionManager;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class contains all commands that cover admin tasks. They should be
 * included in the <code>mywarp.admin.*</code> permission container.
 */
public class AdminCommands {

    @Command(aliases = { "import" }, usage = "<sqlite/mysql>", desc = "commands.import.description", min = 1, max = 1, flags = "f", permissions = { "mywarp.admin.import" })
    public void importWarps(CommandContext args, CommandSender sender) throws CommandException {
        boolean importMySQL;

        if (args.getString(0).equalsIgnoreCase("mysql")) {
            importMySQL = true;
        } else if (args.getString(0).equalsIgnoreCase("sqlite")) {
            importMySQL = false;
        } else {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.import.invalid-option", sender, args.getString(0)));
        }

        try {
            ConnectionManager importConnection = new ConnectionManager(importMySQL, false, true);
            Collection<Warp> importedWarps = importConnection.getWarps();
            Set<Warp> notImportedWarps = new HashSet<Warp>();

            Iterator<Warp> iterator = importedWarps.iterator();
            while (iterator.hasNext()) {
                Warp warp = iterator.next();
                if (MyWarp.inst().getWarpManager().warpExists(iterator.next().getName())) {
                    if (args.hasFlag('f')) {
                        // remove the old warp
                        // XXX That seems somewhat ugly.
                        MyWarp.inst().getWarpManager()
                                .deleteWarp(MyWarp.inst().getWarpManager().getWarp(warp.getName()));
                    } else {
                        // skip
                        notImportedWarps.add(warp);
                        iterator.remove();
                    }
                }
            }

            MyWarp.inst().getWarpManager().populate(importedWarps);

            if (notImportedWarps.isEmpty()) {
                sender.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.import.import-successful", sender, importedWarps.size()));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString("commands.import.import-with-skips", sender, importedWarps.size(),
                                notImportedWarps.size())
                        + " " + StringUtils.join(notImportedWarps, ", "));
            }
        } catch (DataConnectionException ex) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.import.no-connection", sender)
                    + ex.getMessage());
        }
    }

    @Command(aliases = { "reload" }, usage = "", desc = "commands.reload.description", max = 0, permissions = { "mywarp.admin.reload" })
    public void reload(CommandContext args, CommandSender sender) throws CommandException {
        MyWarp.inst().reload();
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.reload.reload-message", sender));
    }

    @Command(aliases = { "player" }, usage = "<player> <name>", desc = "commands.warp-player.description", fee = Fee.WARP_PLAYER, min = 2, permissions = { "mywarp.admin.warpto" })
    public void warpPlayer(CommandContext args, CommandSender sender) throws CommandException {
        Player invitee = CommandUtils.matchPlayer(sender, args.getString(0));
        Warp warp = CommandUtils.getUsableWarp(sender, args.getJoinedStrings(1));

        warp.teleport(invitee, false);
        sender.sendMessage(MyWarp
                .inst()
                .getLocalizationManager()
                .getString("commands.warp-player.teleport-successful", sender, invitee.getName(),
                        warp.getName()));
    }
}

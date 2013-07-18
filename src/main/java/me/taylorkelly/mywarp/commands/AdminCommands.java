package me.taylorkelly.mywarp.commands;

import java.util.Map;
import java.util.Map.Entry;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.ConnectionManager;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class contains all commands that cover admin tasks. They should be
 * included in the <code>mywarp.admin.*</code> permission container.
 * 
 */
public class AdminCommands {

    @Command(aliases = { "import" }, usage = "<sqlite/mysql>", desc = "cmd.description.import", min = 1, max = 1, flags = "f", permissions = { "mywarp.admin.import" })
    public void importWarps(CommandContext args, CommandSender sender) throws CommandException {
        boolean importMySQL;

        if (args.getString(0).equalsIgnoreCase("mysql")) {
            importMySQL = true;
        } else if (args.getString(0).equalsIgnoreCase("sqlite")) {
            importMySQL = false;
        } else {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.import.invalid", "%query%", args.getString(0)));
        }

        try {
            int counter = 0;
            ConnectionManager importConnection = new ConnectionManager(importMySQL, false, true);
            Map<String, Warp> importedWarps = importConnection.getMap();

            for (Entry<String, Warp> importedWarpEntry : importedWarps.entrySet()) {
                String name = importedWarpEntry.getKey();
                Warp importedWarp = importedWarpEntry.getValue();

                if (MyWarp.inst().getWarpManager().warpExists(name)) {
                    if (!args.hasFlag('f')) {
                        sender.sendMessage(MyWarp.inst().getLanguageManager()
                                .getEffectiveString("error.import.exists", "%warp%", name));
                        continue;
                    }
                    // remove the old warp before adding the new one
                    MyWarp.inst().getWarpManager().deleteWarp(MyWarp.inst().getWarpManager().getWarp(name));
                } else {
                    MyWarp.inst().getWarpManager().addWarp(name, importedWarp);
                    counter++;
                }
            }
            sender.sendMessage(counter + " warps were imported sucessfully.");
        } catch (DataConnectionException ex) {
            sender.sendMessage(MyWarp.inst().getLanguageManager().getString("error.import.noConnection")
                    + ex.getMessage());
        }
    }

    @Command(aliases = { "reload" }, usage = "", desc = "cmd.description.reload", max = 0, permissions = { "mywarp.admin.reload" })
    public void reload(CommandContext args, CommandSender sender) throws CommandException {
        MyWarp.inst().getWarpSettings().reload();
        MyWarp.inst().setupConfigurableFunctions();

        sender.sendMessage(MyWarp.inst().getLanguageManager().getString("reload.config"));
    }

    @Command(aliases = { "player" }, usage = "<player> <name>", desc = "cmd.description.adminWarpTo", fee = Fee.WARP_PLAYER, min = 2, permissions = { "mywarp.admin.warpto" })
    public void warpPlayer(CommandContext args, CommandSender sender) throws CommandException {
        Player invitee = CommandUtils.matchPlayer(args.getString(0));
        Warp warp = CommandUtils.getWarpForUsage(sender, args.getJoinedStrings(1));

        warp.warp(invitee, false);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.warpto.player", "%player%", invitee.getName()));
    }
}

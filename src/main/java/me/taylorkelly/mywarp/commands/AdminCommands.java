package me.taylorkelly.mywarp.commands;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.ConnectionManager;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

/**
 * This class contains all commands that cover admin tasks. They should be
 * included in the <code>mywarp.admin.*</code> permission container.
 * 
 */
public class AdminCommands {

    private MyWarp plugin;

    public AdminCommands(MyWarp plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = { "import" }, usage = "<sqlite/mysql>", desc = "cmd.description.import", min = 1, max = 1, flags = "f", permissions = { "mywarp.admin.import" })
    public void importWarps(CommandContext args, CommandSender sender)
            throws CommandException {
        boolean importMySQL;

        if (args.getString(0).equalsIgnoreCase("mysql")) {
            importMySQL = true;
        } else if (args.getString(0).equalsIgnoreCase("sqlite")) {
            importMySQL = false;
        } else {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.import.invalid", "%query%", args.getString(0)));
        }

        try {
            int counter = 0;
            ConnectionManager importConnection = new ConnectionManager(
                    importMySQL, false, true, plugin);
            HashMap<String, Warp> importedWarps = importConnection.getMap();

            for (Entry<String, Warp> importedWarpEntry : importedWarps
                    .entrySet()) {
                String name = importedWarpEntry.getKey();
                Warp importedWarp = importedWarpEntry.getValue();

                if (plugin.getWarpList().warpExists(name)) {
                    if (!args.hasFlag('f')) {
                        sender.sendMessage(LanguageManager.getEffectiveString(
                                "error.import.exists", "%warp%", name));
                        continue;
                    }
                    //remove the old warp before adding the new one
                    plugin.getWarpList().deleteWarp(
                            plugin.getWarpList().getWarp(name));
                } else {
                    plugin.getWarpList().addWarp(name, importedWarp);
                    counter++;
                }
            }
            sender.sendMessage(counter + " warps were imported sucessfully.");
        } catch (DataConnectionException ex) {
            sender.sendMessage(LanguageManager
                    .getString("error.import.noConnection") + ex.getMessage());
        }
    }

    @Command(aliases = { "reload" }, usage = "", desc = "cmd.description.reload", max = 0, permissions = { "mywarp.admin.reload" })
    public void reload(CommandContext args, CommandSender sender)
            throws CommandException {
        plugin.reloadConfig();
        WarpSettings.initialize(plugin);
        LanguageManager.initialize(plugin);
        sender.sendMessage(LanguageManager.getString("reload.config"));
    }

    @Command(aliases = { "player" }, usage = "<player> <name>", desc = "cmd.description.adminWarpTo", min = 2, permissions = { "mywarp.admin.warpto" })
    public void warpPlayer(CommandContext args, CommandSender sender)
            throws CommandException {
        Player invitee = CommandUtils.checkPlayer(args.getString(0));
        Warp warp = CommandUtils.getWarpForUsage(sender,
                args.getJoinedStrings(1));

        plugin.getWarpList().warpTo(warp, invitee);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.warpto.player", "%player%", invitee.getName()));
    }
}

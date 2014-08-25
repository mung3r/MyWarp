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

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.MySQLConnection;
import me.taylorkelly.mywarp.dataconnections.SQLiteConnection;
import me.taylorkelly.mywarp.dataconnections.migrators.DataConnectionMigrator;
import me.taylorkelly.mywarp.dataconnections.migrators.DataMigrator;
import me.taylorkelly.mywarp.dataconnections.migrators.LegacyMySQLMigrator;
import me.taylorkelly.mywarp.dataconnections.migrators.LegacySQLiteMigrator;
import me.taylorkelly.mywarp.economy.FeeBundle.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * This class contains all commands that cover admin tasks. They should be
 * included in the <code>mywarp.admin.*</code> permission container.
 */
public class AdminCommands {

    /**
     * Imports warps from an additional datasource.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "import" }, usage = "<sqlite path/to/db|mysql host port database user password|legacy-sqlite path/to/db|legacy-mysql host port database user password table-name>", desc = "commands.import.description", min = 2, max = 7, flags = "f", permissions = { "mywarp.admin.import" })
    public void importWarps(final CommandContext args, final CommandSender sender) throws CommandException {
        DataMigrator migrator;
        // at this point things get messy - the current command manager is
        // pushed over its boundaries
        if (args.getString(0).equalsIgnoreCase("legacy-mysql")) {
            if (args.argsLength() != 7) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.library.too-few-args", sender));
            }
            try {
                migrator = new LegacyMySQLMigrator(args.getString(1), args.getInteger(2), args.getString(3),
                        args.getString(4), args.getString(5), args.getString(6));
            } catch (NumberFormatException e) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.invalid-number", sender, args.getCommandString()));
            }
        } else if (args.getString(0).equalsIgnoreCase("legacy-sqlite")) {
            File database = new File(MyWarp.inst().getDataFolder(), args.getString(1));
            if (!database.exists()) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.import.file-non-existent", sender, database.getAbsolutePath()));
            }
            migrator = new LegacySQLiteMigrator(database);
        } else if (args.getString(0).equalsIgnoreCase("mysql")) {
            if (args.argsLength() != 6) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.library.too-few-args", sender));
            }
            try {
                migrator = new DataConnectionMigrator(MySQLConnection.getConnection(args.getString(1),
                        args.getInteger(2), args.getString(3), args.getString(4), args.getString(5), false));
            } catch (NumberFormatException e) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.invalid-number", sender, args.getCommandString()));
            }
        } else if (args.getString(0).equalsIgnoreCase("sqlite")) {
            File database = new File(MyWarp.inst().getDataFolder(), args.getString(1));
            if (!database.exists()) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.import.file-non-existent", sender, database.getAbsolutePath()));
            }
            migrator = new DataConnectionMigrator(SQLiteConnection.getConnection(database, false));
        } else {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.import.invalid-option", sender, args.getString(0)));
        }

        ListenableFuture<Collection<Warp>> futureWarps = migrator.getWarps();

        // import the warps when they are ready - executed async!
        Futures.addCallback(futureWarps, new FutureCallback<Collection<Warp>>() {

            @Override
            public void onFailure(final Throwable t) {

                // back to the main thread...
                MyWarp.server().getScheduler().runTask(MyWarp.inst(), new Runnable() {

                    @Override
                    public void run() {
                        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                                .getString("commands.import.no-connection", sender, t.getMessage()));
                    }

                });
            }

            @Override
            public void onSuccess(final Collection<Warp> warps) {
                // back to the main thread...
                MyWarp.server().getScheduler().runTask(MyWarp.inst(), new Runnable() {

                    @Override
                    public void run() {
                        Set<Warp> notImportedWarps = new HashSet<Warp>();
                        for (Warp warp : warps) {
                            // REVIEW this process seems a bit ugly.
                            if (MyWarp.inst().getWarpManager().warpExists(warp.getName())) {
                                if (!args.hasFlag('f')) {
                                    // skip the warp
                                    notImportedWarps.add(warp);
                                    continue;
                                }
                                // remove the old warp
                                MyWarp.inst().getWarpManager()
                                        .deleteWarp(MyWarp.inst().getWarpManager().getWarp(warp.getName()));
                            }
                            MyWarp.inst().getWarpManager().addWarp(warp);
                        }

                        if (notImportedWarps.isEmpty()) {
                            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                                    .getString("commands.import.import-successful", sender, warps.size()));
                        } else {
                            sender.sendMessage(MyWarp
                                    .inst()
                                    .getLocalizationManager()
                                    .getString("commands.import.import-with-skips", sender, warps.size(),
                                            notImportedWarps.size())
                                    + " " + CommandUtils.joinWarps(notImportedWarps));
                        }
                    }

                });
            }

        });
    }

    /**
     * Reloads the plugin.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     */
    @Command(aliases = { "reload" }, usage = "", desc = "commands.reload.description", max = 0, permissions = { "mywarp.admin.reload" })
    public void reload(CommandContext args, CommandSender sender) {
        MyWarp.inst().reload();
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.reload.reload-message", sender));
    }

    /**
     * Teleports a player to a warp.
     * 
     * @param args
     *            the command-arguments
     * @param sender
     *            the sender who initiated the command
     * @throws CommandException
     *             if the command is cancelled
     */
    @Command(aliases = { "player" }, usage = "<player> <name>", desc = "commands.warp-player.description", fee = Fee.WARP_PLAYER, min = 2, permissions = { "mywarp.admin.warpto" })
    public void warpPlayer(CommandContext args, CommandSender sender) throws CommandException {
        Player invitee = CommandUtils.matchPlayer(sender, args.getString(0));
        Warp warp = CommandUtils.getUsableWarp(sender, args.getJoinedStrings(1));

        switch (warp.teleport(invitee)) {
        case NONE:
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.warp-player.teleport-failed", sender, invitee.getName(),
                            warp.getName()));
            break;
        case ORIGINAL_LOC:
        case SAFE_LOC:
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.warp-player.teleport-successful", sender, invitee.getName(),
                            warp.getName()));
            break;

        }
    }
}

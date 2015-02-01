/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

package me.taylorkelly.mywarp.bukkit.commands;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.util.CommandUtils;
import me.taylorkelly.mywarp.dataconnections.MySQLConnection;
import me.taylorkelly.mywarp.dataconnections.SQLiteConnection;
import me.taylorkelly.mywarp.dataconnections.migrators.DataConnectionMigrator;
import me.taylorkelly.mywarp.dataconnections.migrators.DataMigrator;
import me.taylorkelly.mywarp.dataconnections.migrators.LegacyMySQLMigrator;
import me.taylorkelly.mywarp.dataconnections.migrators.LegacySQLiteMigrator;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;

/**
 * Bundles commands used to import Warps from an external source.
 */
public class ImportCommands {

    private static final String IMPORT_PERMISSION = "mywarp.admin.import"; // NON-NLS
    private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

    /**
     * Imports Warps from an SQLite database.
     * 
     * @param actor
     *            the Actor
     * @param databasePath
     *            the path to the database file
     * @throws CommandException
     *             if the file does not exist
     */
    @Command(aliases = { "sqlite" }, desc = "import.sqlite.description", help = "import.sqlite.help")
    @Require(IMPORT_PERMISSION)
    public void sqlite(Actor actor, String databasePath) throws CommandException {
        File database = new File(MyWarp.getInstance().getPlatform().getDataFolder(), databasePath);
        if (!database.exists()) {
            throw new CommandException(MESSAGES.getString("import.file-non-existent",
                    database.getAbsolutePath()));
        }
        migrate(actor, new DataConnectionMigrator(SQLiteConnection.getConnection(database, false)));
    }

    /**
     * Imports Warps from an MySQL database.
     * 
     * @param actor
     *            the Actor
     * @param dsn
     *            the dsn of the database
     * @param user
     *            the MySQL user to use
     * @param password
     *            the user's password
     */
    @Command(aliases = { "mysql" }, desc = "import.mysql.description", help = "import.mysql.help")
    @Require(IMPORT_PERMISSION)
    public void mysql(Actor actor, String dsn, String user, String password) {
        migrate(actor, new DataConnectionMigrator(MySQLConnection.getConnection(dsn, user, password, false)));
    }

    /**
     * Imports Warps from an SQLite database with an old (pre 3.0) scheme.
     * 
     * @param actor
     *            the Actor
     * @param databasePath
     *            the path to the database file
     * @throws CommandException
     *             if the file does not exist
     */
    @Command(aliases = { "pre3-sqlite" }, desc = "import.pre3-sqlite.description", help = "import.pre3-sqlite.help")
    @Require(IMPORT_PERMISSION)
    public void pre3Sqlite(Actor actor, String databasePath) throws CommandException {
        File database = new File(MyWarp.getInstance().getPlatform().getDataFolder(), databasePath);
        if (!database.exists()) {
            throw new CommandException(MESSAGES.getString("commands.import.file-non-existent", // NON-NLS
                    database.getAbsolutePath()));
        }
        migrate(actor, new LegacySQLiteMigrator(database));
    }

    /**
     * Imports Warps from an MySQL database with an old (pre 3.0) scheme.
     * 
     * @param actor
     *            the Actor
     * @param dsn
     *            the dsn of the database
     * @param user
     *            the MySQL user to use
     * @param password
     *            the user's password
     * @param tableName
     *            the name of the table that contains the data
     */
    @Command(aliases = { "pre3-mysql" }, desc = "import.pre3-mysql.description", help = "import.pre3-mysql.help")
    @Require(IMPORT_PERMISSION)
    public void pre3Mysql(Actor actor, String dsn, String user, String password, String tableName) {
        migrate(actor, new LegacyMySQLMigrator(dsn, user, password, tableName));
    }

    /**
     * Migrates Warps from the given DataMigrator.
     * 
     * @param initiator
     *            the Actor who initiated the migration
     * @param migrator
     *            the DataMigrator
     */
    private void migrate(final Actor initiator, DataMigrator migrator) {
        ListenableFuture<Collection<Warp>> futureWarps = migrator.getWarps();

        // The callback is called when the warps are loaded. It is executed in
        // the server-thread.
        Futures.addCallback(futureWarps, new FutureCallback<Collection<Warp>>() {

            @Override
            public void onFailure(final Throwable t) {
                initiator.sendError(MESSAGES.getString("import.no-connection", t.getMessage()));
            }

            @Override
            public void onSuccess(final Collection<Warp> warps) {
                Set<Warp> notImportedWarps = new HashSet<Warp>();
                for (Warp warp : warps) {
                    if (MyWarp.getInstance().getWarpManager().contains(warp.getName())) {
                        // skip the warp
                        notImportedWarps.add(warp);
                        continue;
                    }
                    MyWarp.getInstance().getWarpManager().add(warp);
                }

                if (notImportedWarps.isEmpty()) {
                    initiator.sendMessage(ChatColor.AQUA
                            + MESSAGES.getString("import.import-successful", warps.size()));
                } else {
                    initiator.sendError(MESSAGES.getString("import.import-with-skips", warps.size(),
                            notImportedWarps.size()));
                    initiator.sendMessage(CommandUtils.joinWarps(notImportedWarps));
                }

            }

        }, MyWarp.getInstance().getServerExecutor());
    }

}

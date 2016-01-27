/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.util.jdbc.DataSourceFactory;
import me.taylorkelly.mywarp.bukkit.util.jdbc.SingleConnectionDataSource;
import me.taylorkelly.mywarp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.storage.LegacyWarpSource;
import me.taylorkelly.mywarp.storage.StorageInitializationException;
import me.taylorkelly.mywarp.storage.WarpSource;
import me.taylorkelly.mywarp.storage.WarpStorageFactory;
import me.taylorkelly.mywarp.util.CommandUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import org.bukkit.ChatColor;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bundles commands used to import Warps from an external source.
 */
public class ImportCommands {

  private static final String IMPORT_PERMISSION = "mywarp.cmd.import";
  private static final DynamicMessages MESSAGES = new DynamicMessages(CommandUtils.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;

  /**
   * Creates an instance.
   *
   * @param myWarp the MyWarp instance
   */
  public ImportCommands(MyWarp myWarp) {
    this.myWarp = myWarp;
  }

  /**
   * Imports Warps from a relational database with an up-to-date table structure.
   *
   * @param actor  the Actor
   * @param config the config of the relational database
   * @throws CommandException if the import fails
   */
  @Command(aliases = {"current", "curr"}, desc = "import.current.description", help = "import.current.help")
  @Require(IMPORT_PERMISSION)
  public void current(Actor actor, ConnectionConfiguration config) throws CommandException {
    SingleConnectionDataSource dataSource;
    try {
      dataSource = DataSourceFactory.createSingleConnectionDataSource(config);
      start(actor, WarpStorageFactory.create(myWarp, dataSource, config), dataSource);
    } catch (SQLException e) {
      throw new CommandException(MESSAGES.getString("import.no-connection", e.getMessage()));
    } catch (StorageInitializationException e) {
      throw new CommandException(MESSAGES.getString("import.no-connection", e.getMessage()));
    }
  }

  /**
   * Imports Warps from an SQLite database with an old (pre 3.0) scheme.
   *
   * @param actor    the Actor
   * @param database the database file
   * @throws CommandException if the import fails
   */
  @Command(aliases = {"pre3-sqlite"}, desc = "import.pre3-sqlite.description", help = "import.pre3-sqlite.help")
  @Require(IMPORT_PERMISSION)
  public void pre3Sqlite(Actor actor, File database) throws CommandException {
    ConnectionConfiguration config = new ConnectionConfiguration("jdbc:sqlite:" + database.getAbsolutePath());
    try {
      SingleConnectionDataSource dataSource = DataSourceFactory.createSingleConnectionDataSource(config);
      start(actor, new LegacyWarpSource(myWarp, dataSource, config, "warpTable", getWorldSnapshot()), dataSource);
    } catch (SQLException e) {
      throw new CommandException(MESSAGES.getString("import.no-connection", e.getMessage()));
    }
  }

  /**
   * Imports Warps from an MySQL database with an old (pre 3.0) scheme.
   *
   * @param actor     the Actor
   * @param dsn       the dsn of the database
   * @param user      the MySQL user to use
   * @param password  the user's password
   * @param tableName the name of the table that contains the data
   * @throws CommandException if the import fails
   */
  @Command(aliases = {"pre3-mysql"}, desc = "import.pre3-mysql.description", help = "import.pre3-mysql.help")
  @Require(IMPORT_PERMISSION)
  public void pre3Mysql(Actor actor, String dsn, String schema, String user, String password, String tableName)
      throws CommandException {
    ConnectionConfiguration
        config =
        new ConnectionConfiguration(dsn).setSchema(schema).setUser(user).setPassword(password);
    try {
      SingleConnectionDataSource dataSource = DataSourceFactory.createSingleConnectionDataSource(config);
      start(actor, new LegacyWarpSource(myWarp, dataSource, config, tableName, getWorldSnapshot()), dataSource);
    } catch (SQLException e) {
      throw new CommandException(MESSAGES.getString("import.no-connection", e.getMessage()));
    }
  }

  /**
   * Starts the import from the given {@code WarpSource}.
   *
   * @param initiator        the {@code Actor} who initated the import
   * @param warpSource       the {@code WarpSource} to import from
   * @param importDataSource the {@code DataSource} used to import
   */
  private void start(final Actor initiator, final WarpSource warpSource,
                     final SingleConnectionDataSource importDataSource) {
    initiator.sendMessage(ChatColor.AQUA + MESSAGES.getString("import.started"));

    final ListeningExecutorService
        executorService =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    ListenableFuture<List<Warp>> futureWarps = executorService.submit(new Callable<List<Warp>>() {
      @Override
      public List<Warp> call() throws Exception {
        return warpSource.getWarps();
      }
    });

    Futures.addCallback(futureWarps, new FutureCallback<List<Warp>>() {

      @Override
      public void onFailure(final Throwable throwable) {
        initiator.sendError(MESSAGES.getString("import.no-connection", throwable.getMessage()));
        close(executorService, importDataSource);
      }

      @Override
      public void onSuccess(final List<Warp> warps) {
        Set<Warp> notImportedWarps = new HashSet<Warp>();
        WarpManager warpManager = myWarp.getWarpManager();

        for (Warp warp : warps) {
          if (warpManager.contains(warp.getName())) {
            // skip the warp
            notImportedWarps.add(warp);
            continue;
          }
          warpManager.add(warp);
        }

        if (notImportedWarps.isEmpty()) {
          initiator.sendMessage(ChatColor.AQUA + MESSAGES.getString("import.import-successful", warps.size()));
        } else {
          int successfullyImported = warps.size() - notImportedWarps.size();
          initiator
              .sendError(MESSAGES.getString("import.import-with-skips", successfullyImported, notImportedWarps.size()));
          initiator.sendError(CommandUtils.joinWarps(notImportedWarps));
        }

        close(executorService, importDataSource);
      }

    }, myWarp.getGame().getExecutor());
  }

  /**
   * Gets a mapping of the names to uniqueIds from all worlds currently existing.
   *
   * @return a mapping of the names to uniqueIds from all worlds
   */
  private Map<String, UUID> getWorldSnapshot() {
    Map<String, UUID> snapshot = new HashMap<String, UUID>();
    for (LocalWorld world : myWarp.getGame().getWorlds()) {
      snapshot.put(world.getName(), world.getUniqueId());
    }
    return snapshot;
  }

  /**
   * Closes the given {@code ExecutorService} and {@code SingleConnectionDataSource} in this order.
   *
   * @param executorService the {@code ExecutorService} to close
   * @param dataSource      the {@code SingleConnectionDataSource} to close
   */
  private void close(ExecutorService executorService, SingleConnectionDataSource dataSource) {
    //REVIEW block until shutdown?
    executorService.shutdown();
    dataSource.close();
  }

}

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

package me.taylorkelly.mywarp.dataconnections.migrators;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.warp.Warp;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A migrator for legacy (pre 2.7) SQLite databases.
 */
public class LegacySqLiteMigrator extends LegacyMigrator implements DataMigrator {

  private static final Logger log = Logger.getLogger(LegacySqLiteMigrator.class.getName());
  private static final String TABLE_NAME = "warpTable";

  private final String dsn;

  /**
   * Initiates this LegacySqLiteMigrator.
   *
   * @param myWarp         the MyWarp instance
   * @param worldsSnapshot a mapping of world names to uniqueIds
   * @param database       the database file
   */
  public LegacySqLiteMigrator(MyWarp myWarp, ImmutableMap<String, UUID> worldsSnapshot, File database) {
    super(myWarp, worldsSnapshot);
    this.dsn = "jdbc:sqlite://" + database.getAbsolutePath();
  }

  @Override
  public ListenableFuture<Collection<Warp>> getWarps() {
    ListenableFutureTask<Collection<Warp>> ret = ListenableFutureTask.create(new Callable<Collection<Warp>>() {
      @Override
      public Collection<Warp> call() throws DataConnectionException {
        try {
          // Manually load SQLite driver. DriveManager is
          // unable to identify it as the driver does not
          // follow JDBC 4.0 standards.
          Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
          throw new DataConnectionException("Unable to find SQLite library.", e);
        }

        Connection conn;
        try {
          conn = DriverManager.getConnection(dsn);
        } catch (SQLException e) {
          throw new DataConnectionException("Failed to connect to the database.", e);
        }

        DSLContext create = DSL.using(conn, SQLDialect.SQLITE);
        Collection<Warp> ret = null;
        try {
          ret = migrateLegacyWarps(create, TABLE_NAME);
        } finally {
          try {
            conn.close();
          } catch (SQLException e) {
            log.log(Level.WARNING, "Failed to close import SQL connection.", e);
          }
        }

        return ret;
      }
    });
    new Thread(ret).start();
    return ret;
  }
}

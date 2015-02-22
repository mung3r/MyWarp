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

package me.taylorkelly.mywarp.dataconnections;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import me.taylorkelly.mywarp.MyWarp;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * The connection to a MySQL database.
 */
public class MySqlConnection {

  /**
   * Block initialization of this class.
   */
  private MySqlConnection() {
  }

  /**
   * Gets a valid connection to the given MySQL database. The connection is created asynchronous, the returned
   * CheckedFuture either contains the ready-to-use connection or throws a {@link DataConnectionException}.
   *
   * @param myWarp          the MyWarp instance
   * @param dsn             the dsn of the database
   * @param user            the MySQL user to use
   * @param password        the user's password
   * @param controlDbLayout whether the implementation should create tables and execute updates, if necessary
   * @return a CheckedFuture containing a valid, setup connection
   */
  public static CheckedFuture<DataConnection, DataConnectionException> getConnection(final MyWarp myWarp,
                                                                                     final String dsn,
                                                                                     final String user,
                                                                                     final String password,
                                                                                     final boolean controlDbLayout) {
    final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    ListenableFuture<DataConnection> future = executor.submit(new Callable<DataConnection>() {

      @Override
      public DataConnection call() throws DataConnectionException {

        if (controlDbLayout) {
          Flyway flyway = new Flyway();

          flyway.setClassLoader(getClass().getClassLoader());
          flyway.setDataSource(dsn, user, password);
          flyway.setLocations("migrations/mysql");

          try {
            flyway.migrate();
          } catch (FlywayException e) {
            throw new DataConnectionException("Failed to execute migration process.", e);
          }
        }

        Connection conn;
        try {
          conn = DriverManager.getConnection(dsn, user, password);
        } catch (SQLException e) {
          throw new DataConnectionException("Failed to connect to the database.", e);
        }

        // the database scheme can be configured by users
        Settings settings = new Settings().withRenderSchema(false);
        DSLContext create = DSL.using(conn, SQLDialect.MYSQL, settings);

        return new JooqConnection(myWarp, conn, executor, create);
      }

    });
    return Futures.makeChecked(future, new Function<Exception, DataConnectionException>() {

      @Override
      public DataConnectionException apply(Exception ex) {
        if (ex instanceof DataConnectionException) {
          return (DataConnectionException) ex;
        }
        return new DataConnectionException(ex);
      }
    });
  }
}

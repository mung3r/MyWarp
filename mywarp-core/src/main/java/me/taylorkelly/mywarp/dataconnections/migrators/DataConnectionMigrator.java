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

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import me.taylorkelly.mywarp.dataconnections.DataConnection;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.Collection;

/**
 * Migrates data from a {@link DataConnection}.
 */
public class DataConnectionMigrator implements DataMigrator {

  private final ListenableFuture<DataConnection> futureConnection;
  private DataConnection storedConn;

  /**
   * Initializes this migrator with the given data-connection. Every further action that directly involves the
   * data-connection waits at least until the data-connection is ready.
   *
   * @param futureConnection the connection, wrapped in a listenable-future
   */
  public DataConnectionMigrator(ListenableFuture<DataConnection> futureConnection) {
    this.futureConnection = futureConnection;
  }

  @Override
  public ListenableFuture<Collection<Warp>> getWarps() {
    ListenableFuture<Collection<Warp>>
        futureWarps =
        Futures.chain(futureConnection, new Function<DataConnection, ListenableFuture<Collection<Warp>>>() {

          @Override
          public ListenableFuture<Collection<Warp>> apply(DataConnection conn) {
            storedConn = conn;
            return conn.getWarps();
          }

        });
    // of the function above fails the database connection remains open
    Futures.addCallback(futureWarps, new FutureCallback<Collection<Warp>>() {

      @Override
      public void onFailure(Throwable throwable) {
        if (storedConn != null) {
          storedConn.close();
        }
      }

      @Override
      public void onSuccess(Collection<Warp> warps) {
        if (storedConn != null) {
          storedConn.close();
        }
      }

    });
    return futureWarps;
  }

}

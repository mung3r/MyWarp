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

package me.taylorkelly.mywarp.bukkit;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import me.taylorkelly.mywarp.bukkit.util.jdbc.DataSourceFactory;
import me.taylorkelly.mywarp.bukkit.util.jdbc.SingleConnectionDataSource;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.warp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.warp.storage.RelationalDataService;

import org.slf4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * An {@link RelationalDataService} that uses a {@link SingleConnectionDataSource}.
 */
public class SingleConnectionDataService implements RelationalDataService {

  private static final Logger log = MyWarpLogger.getLogger(SingleConnectionDataService.class);

  private final ConnectionConfiguration config;

  @Nullable
  private SingleConnectionDataSource dataSource;
  @Nullable
  private ListeningExecutorService executorService;

  /**
   * Creates an instance that uses the given {@code config}.
   *
   * @param config the config
   */
  SingleConnectionDataService(ConnectionConfiguration config) {
    this.config = config;
  }

  @Override
  public DataSource getDataSource() throws SQLException {
    if (dataSource == null) {
      dataSource = DataSourceFactory.createSingleConnectionDataSource(config);
    }
    return dataSource;
  }

  @Override
  public ListeningExecutorService getExecutorService() {
    if (executorService == null) {
      executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    }
    return executorService;
  }

  /**
   * Initiates an shutdown that closes the {@code ExecutorService} and the {@code DataSource}, blocking until either all
   * remaining tasks are executed or 30 seconds have passed or the thread is interrupted.
   */
  @Override
  public void close() {
    if (executorService != null) {
      executorService.shutdown();

      try {
        if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
          List<Runnable> droppedTasks = executorService.shutdownNow();
          log.warn("SQL executor did not terminate within 30 seconds and is terminated. {} tasks will not be "
                  + "executed, recent changes may be missing in the database.", droppedTasks.size());
        }
      } catch (InterruptedException e) {
        log.error("Failed to terminate SQL executor as the process was interrupted.", e);
      }
    }

    if (dataSource != null) {
      try {
        dataSource.close();
      } catch (IOException e) {
        log.warn("Failed to close data source", e);
      }
    }

  }
}

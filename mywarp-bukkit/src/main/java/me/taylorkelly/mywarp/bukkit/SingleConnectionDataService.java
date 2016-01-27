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

import me.taylorkelly.mywarp.bukkit.util.jdbc.SingleConnectionDataSource;
import me.taylorkelly.mywarp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.storage.RelationalDataService;
import me.taylorkelly.mywarp.util.MyWarpLogger;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * An {@link RelationalDataService} that uses a {@link SingleConnectionDataSource}.
 */
public class SingleConnectionDataService implements RelationalDataService {

  private static final Logger log = MyWarpLogger.getLogger(SingleConnectionDataService.class);

  private final SingleConnectionDataSource dataSource;
  private final ConnectionConfiguration config;
  private final ListeningExecutorService executorService;

  /**
   * Creates an instance that uses the given {@code dataSource}, the given {@code config} and the given {@code
   * executorService}.
   *
   * @param dataSource      the data-source
   * @param config          the config
   * @param executorService the executor-service
   */
  public SingleConnectionDataService(SingleConnectionDataSource dataSource, ConnectionConfiguration config,
                                     ListeningExecutorService executorService) {
    this.dataSource = dataSource;
    this.config = config;
    this.executorService = executorService;
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public ListeningExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public ConnectionConfiguration getConfiguration() {
    return config;
  }

  /**
   * Initiates an shutdown that closes the {@code ExecutorService} and the {@code DataSource}, blocking until either all
   * remaining tasks are executed or 30 seconds have passed or the thread is interrupted.
   */
  void shutdown() {
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

    dataSource.close();
  }
}

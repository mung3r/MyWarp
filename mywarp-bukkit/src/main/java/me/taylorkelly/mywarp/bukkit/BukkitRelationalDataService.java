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

package me.taylorkelly.mywarp.bukkit;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import me.taylorkelly.mywarp.bukkit.util.jdbc.DataSourceFactory;
import me.taylorkelly.mywarp.bukkit.util.jdbc.SingleConnectionDataSource;
import me.taylorkelly.mywarp.storage.RelationalDataService;
import me.taylorkelly.mywarp.util.MyWarpLogger;

import org.slf4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * A {@link RelationalDataService} implementation for Bukkit that connects to either an SQLite or MySQL database. If
 * {@link
 * BukkitSettings#isMysqlEnabled()} is set, MySQL is used. Otherwise an SQLite database is created in
 * MyWarp's plugin directory.</p>
 * <p>Both databases are accessed via a single connection using a  {@link SingleConnectionDataSource}. Since this is
 * not thread-safe, {@link #getExecutorService()} retruns a single thread executor.</p>
 */
public class BukkitRelationalDataService implements RelationalDataService {

  private static final Logger log = MyWarpLogger.getLogger(BukkitRelationalDataService.class);

  private final SingleConnectionDataSource dataSource;
  private final ListeningExecutorService executorService;

  /**
   * Creates a new instance, based on the given {@code Settings}.
   *
   * @param settings     the {@code BukkitSettings}
   * @param pluginFolder the plugin folder that contains the SQLite database
   * @throws SQLException on a database error
   */
  public BukkitRelationalDataService(BukkitSettings settings, File pluginFolder) throws SQLException {
    if (settings.isMysqlEnabled()) {
      this.dataSource =
          DataSourceFactory.createMySqlSingleConnectionDataSource(settings.getMysqlDsn(), settings.getMysqlUsername(),
                                                                  settings.getMysqlPassword());
    } else {
      //use SQLite
      this.dataSource = DataSourceFactory.createSqliteSingleConnectionDataSource(new File(pluginFolder, "mywarp.db"));
    }

    this.executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public ListeningExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Initiates an shutdown that closes the {@code ExecutorService} and the {@code DataSource}, blocking until either
   * all remaining tasks are executed or 30 seconds have passed or the thread is interrupted.
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

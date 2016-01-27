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

package me.taylorkelly.mywarp.storage;

import com.google.common.util.concurrent.ListeningExecutorService;

import javax.sql.DataSource;

/**
 * A service that provides access to a relational database.
 */
public interface RelationalDataService {

  /**
   * Gets a {@code DataSource} that provides a connection to an SQL database.
   *
   * @return a {@code DataSource}
   */
  DataSource getDataSource();

  /**
   * Gets the {@code ConnectionConfiguration} of the underling database management system.
   *
   * @return the configuration
   */
  ConnectionConfiguration getConfiguration();

  /**
   * Gets a {@code ListeningExecutorService} that should execute database calls.
   *
   * @return a {@code ListeningExecutorService}
   */
  ListeningExecutorService getExecutorService();

}

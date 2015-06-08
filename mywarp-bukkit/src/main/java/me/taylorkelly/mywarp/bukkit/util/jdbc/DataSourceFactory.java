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

package me.taylorkelly.mywarp.bukkit.util.jdbc;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Creates pre-configured {@link DataSource}s to supported databases.
 */
public class DataSourceFactory {

  /**
   * Creates a new {@link SingleConnectionDataSource} to the SQLite database found in the given file.
   *
   * @param database the database {@code File}
   * @return a new {@code SingleConnectionDataSource}
   * @throws SQLException on a database error
   */
  public static SingleConnectionDataSource createSqliteSingleConnectionDataSource(final File database)
      throws SQLException {
    Properties config = new Properties();
    config.setProperty("foreign_keys", "on");

    return createSingleConnectionDataSource("jdbc:sqlite://" + database.getAbsolutePath(), config);
  }

  /**
   * Creates a new {@link SingleConnectionDataSource} to the MySQL server available under the given {@code url}, using
   * the given credentials.
   *
   * @param url      the URL of the MySQL server
   * @param user     the name of the user
   * @param password the password of the user
   * @return a new {@code SingleConnectionDataSource}
   * @throws SQLException on a database error
   */
  public static SingleConnectionDataSource createMySqlSingleConnectionDataSource(String url, String user,
                                                                                 String password) throws SQLException {
    Properties config = new Properties();
    config.setProperty("user", user);
    config.setProperty("password", password);

    return createSingleConnectionDataSource(url, config);
  }

  /**
   * Creates a new {@code SingleConnectionDataSource} from the given {@code url}, using the given {@code Properties} as
   * configuration.
   *
   * @param url    the url
   * @param config the {@code Properties}
   * @return a new {@code SingleConnectionDataSource}
   * @throws SQLException on a database error
   */
  private static SingleConnectionDataSource createSingleConnectionDataSource(String url, Properties config)
      throws SQLException {
    return new SingleConnectionDataSource(DriverManager.getConnection(url, config));
  }

}

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

import me.taylorkelly.mywarp.storage.ConnectionConfiguration;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Creates pre-configured {@link DataSource}s to supported databases.
 */
public class DataSourceFactory {

  /**
   * Creates a new {@code SingleConnectionDataSource} with the given {@code config}.
   *
   * @param config the config of the relational database
   * @return a new {@code SingleConnectionDataSource}
   * @throws SQLException on a database error
   */
  public static SingleConnectionDataSource createSingleConnectionDataSource(ConnectionConfiguration config)
      throws SQLException {
    Properties properties = new Properties();

    if (config.getDriver().equals("org.sqlite.JDBC")) {
      properties.setProperty("foreign_keys", "on");
    } else if (config.getDriver().equals("org.h2.Driver")) {
      try {
        Class.forName("org.h2.Driver");
      } catch (ClassNotFoundException e) {
        //REVIEW throw SQLException?
        throw new IllegalStateException("H2 driver class not found.");
      }
      properties.setProperty("user", config.getUser());
      properties.setProperty("password", config.getPassword());
    } else {
      properties.setProperty("user", config.getUser());
      properties.setProperty("password", config.getPassword());
    }

    return createSingleConnectionDataSource(config.getUrl(), properties);
  }

  /**
   * Creates a new {@code SingleConnectionDataSource} from the given {@code dsn}, using the given {@code Properties} as
   * configuration.
   *
   * @param dsn        the data source name
   * @param properties the {@code Properties}
   * @return a new {@code SingleConnectionDataSource}
   * @throws SQLException on a database error
   */
  private static SingleConnectionDataSource createSingleConnectionDataSource(String dsn, Properties properties)
      throws SQLException {
    return new SingleConnectionDataSource(DriverManager.getConnection(dsn, properties));
  }

}

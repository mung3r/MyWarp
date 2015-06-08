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

import me.taylorkelly.mywarp.MyWarp;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.jdbc.JDBCUtils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Creates {@link DataConnection} instances.
 */
public class DataConnectionFactory {

  /**
   * Block initialization of this class.
   */
  private DataConnectionFactory() {
  }

  /**
   * Creates a new {@code DataConnection} to the given {@code DataSource}.
   * <p>Use {@link #createInitialized(MyWarp, DataSource)} to create an initialized {@code
   * DataConnection} that guarantees existence of MyWarp's table structure.</p>
   *
   * @param myWarp     the MyWarp instance
   * @param dataSource the DataSource
   * @return the {@code DataConnection}
   * @throws DataConnectionException if the given {@code DataSource} connects to a database that is not supported
   */
  public static DataConnection create(MyWarp myWarp, DataSource dataSource) throws DataConnectionException {
    SQLDialect dialect = getDialect(dataSource);

    return createJooqConnection(myWarp, dialect, dataSource);

  }

  /**
   * Creates a new initialized {@code DataConnection} to the given {@code DataSource}, attempting to create or update
   * MyWarp's table structure if necessary.
   * <p>Use {@link #create(MyWarp, DataSource,)} to create a {@code DataConnection} that does not create or update the
   * table structure.</p>
   *
   * @param myWarp     the MyWarp instance
   * @param dataSource the DataSource
   * @return the {@code DataConnection}
   * @throws DataConnectionException if the given {@code DataSource} connects to a database that is not supported or
   *                                 the initialization of MyWarp's table structure fails
   */
  public static DataConnection createInitialized(final MyWarp myWarp, final DataSource dataSource)
      throws DataConnectionException {
    SQLDialect dialect = getDialect(dataSource);

    Flyway flyway = new Flyway();
    flyway.setClassLoader(myWarp.getClass().getClassLoader());
    flyway.setDataSource(dataSource);
    flyway.setLocations("migrations/" + dialect.getNameLC());

    try {
      flyway.migrate();
    } catch (FlywayException e) {
      throw new DataConnectionException("Failed to execute migration process.", e);
    }
    return createJooqConnection(myWarp, dialect, dataSource);
  }

  /**
   * Creates a new {@code JooqConnection} using the given parameters.
   *
   * @param myWarp     the MyWarp instance
   * @param dataSource the DataSource
   * @return a new {@code JooqConnection}
   */
  private static JooqConnection createJooqConnection(MyWarp myWarp, SQLDialect dialect, DataSource dataSource) {
    Configuration
        config =
        new DefaultConfiguration().set(dialect).set(new Settings().withRenderSchema(false)).set(dataSource);

    return new JooqConnection(myWarp, config);
  }

  /**
   * Gets the {@link SQLDialect} from the given {@code DataSource}, if the dialect is supported.
   *
   * @param dataSource the {@code DataSource} to get the dialect from
   * @return the {@code SQLDialect}
   * @throws DataConnectionException if there is no connection to the database or the database's dialect is not
   *                                 supported
   */
  private static SQLDialect getDialect(DataSource dataSource) throws DataConnectionException {
    SQLDialect dialect = SQLDialect.DEFAULT;
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      dialect = JDBCUtils.dialect(connection);
    } catch (SQLException e) {
      throw new DataConnectionException("Failed to connect to the database.", e);
    } finally {
      JDBCUtils.safeClose(connection);
    }

    if (!(dialect.equals(SQLDialect.MYSQL) || dialect.equals(SQLDialect.SQLITE))) {
      throw new DataConnectionException(dialect.getName() + "is not supported.");
    }

    return dialect;
  }

}

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

package me.taylorkelly.mywarp.storage;

import com.google.common.collect.ImmutableSet;

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
 * Creates {@link WarpStorage} instances.
 */
public class WarpStorageFactory {

  private static final ImmutableSet<SQLDialect>
      SUPPORRTED_DIALECTS =
      ImmutableSet.of(SQLDialect.MYSQL, SQLDialect.SQLITE);

  /**
   * Block initialization of this class.
   */
  private WarpStorageFactory() {
  }

  /**
   * Creates a new {@code WarpStorage} to the given {@code DataSource}.
   * <p>Use {@link #createInitialized(MyWarp, DataSource)} to create an initialized {@code
   * WarpStorage} that guarantees existence of MyWarp's table structure.</p>
   *
   * @param myWarp     the MyWarp instance
   * @param dataSource the DataSource
   * @return the {@code WarpStorage}
   * @throws StorageInitializationException if a database error occurs or the underling database management system is
   *                                        not supported
   */
  public static WarpStorage create(MyWarp myWarp, DataSource dataSource) throws StorageInitializationException {
    return createRelationalWarpStorage(myWarp, getSupportedDialectOrFail(dataSource), dataSource);
  }

  /**
   * Creates a new initialized {@code WarpStorage} to the given {@code DataSource}, attempting to create or update
   * MyWarp's table structure if necessary.
   * <p>Use {@link #create(MyWarp, DataSource,)} to create a {@code WarpStorage} that does not create or update the
   * table structure.</p>
   *
   * @param myWarp     the MyWarp instance
   * @param dataSource the DataSource
   * @return the {@code WarpStorage}
   * @throws StorageInitializationException if a database error occurs, the underling database management system is
   *                                        not supported or initialization of MyWarp's table structure fails
   */
  public static WarpStorage createInitialized(MyWarp myWarp, DataSource dataSource)
      throws StorageInitializationException {
    SQLDialect dialect = getSupportedDialectOrFail(dataSource);

    Flyway flyway = new Flyway();
    flyway.setClassLoader(myWarp.getClass().getClassLoader());
    flyway.setDataSource(dataSource);
    flyway.setLocations("migrations/" + dialect.getNameLC());

    try {
      flyway.migrate();
    } catch (FlywayException e) {
      throw new StorageInitializationException("Failed to execute migration process.", e);
    }
    return createRelationalWarpStorage(myWarp, dialect, dataSource);
  }

  /**
   * Creates a new {@code RelationalWarpStorage} using the given parameters.
   *
   * @param myWarp     the MyWarp instance
   * @param dataSource the DataSource
   * @return a new {@code RelationalWarpStorage}
   */
  private static RelationalWarpStorage createRelationalWarpStorage(MyWarp myWarp, SQLDialect dialect,
                                                                   DataSource dataSource) {
    Configuration
        config =
        new DefaultConfiguration().set(dialect).set(new Settings().withRenderSchema(false)).set(dataSource);

    return new RelationalWarpStorage(myWarp, config);
  }

  /**
   * Gets the {@link SQLDialect} from the given {@code DataSource} or fails fast if the dialect is not supported or a
   * database error occurs.
   *
   * @param dataSource the {@code DataSource} to get the dialect from
   * @return the {@code SQLDialect}
   * @throws StorageInitializationException if a database error occurs or the dialect is not supported
   */
  private static SQLDialect getSupportedDialectOrFail(DataSource dataSource) throws StorageInitializationException {
    SQLDialect dialect;
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      dialect = JDBCUtils.dialect(connection);
    } catch (SQLException e) {
      throw new StorageInitializationException("Failed to connect to the database.", e);
    } finally {
      JDBCUtils.safeClose(connection);
    }
    if (!SUPPORRTED_DIALECTS.contains(dialect)) {
      throw new StorageInitializationException(String.format("%s is not supported!", dialect.getName()));
    }
    return dialect;
  }

}

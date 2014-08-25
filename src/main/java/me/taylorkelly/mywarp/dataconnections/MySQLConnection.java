/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * The connection to a MySQL database.
 */
public class MySQLConnection {

    /**
     * Block initialization of this class.
     */
    private MySQLConnection() {
    }

    /**
     * Gets a valid connection to the given MySQL database. The connection is
     * created asynchronous, the returned CheckedFuture either contains the
     * ready-to-use connection or throws a {@link DataConnectionException}.
     * 
     * @param hostAdress
     *            the host adress of the MySQL server
     * @param port
     *            the port the MySQL server listens to
     * @param databaseName
     *            the name of the MySQL database to use
     * @param user
     *            the MySQL user to use
     * @param password
     *            the user's password
     * @param controlDBLayout
     *            whether the implementation should create tables and execute
     *            updates, if necessary
     * @return a CheckedFuture containing a valid, setup connection
     */
    public static CheckedFuture<DataConnection, DataConnectionException> getConnection(
            final String hostAdress, final int port, final String databaseName, final String user,
            final String password, final boolean controlDBLayout) {
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors
                .newSingleThreadExecutor());

        ListenableFuture<DataConnection> future = executor.submit(new Callable<DataConnection>() {

            @Override
            public DataConnection call() throws DataConnectionException {
                String dsn = "jdbc:mysql://" + hostAdress + ":" + port + "/" + databaseName;

                Connection conn;
                try {
                    conn = DriverManager.getConnection(dsn, user, password);
                } catch (SQLException e) {
                    throw new DataConnectionException("Failed to connect to the database.", e);
                }

                // the database scheme can be configured by users
                Settings settings = new Settings().withRenderSchema(false);

                DSLContext create = DSL.using(conn, SQLDialect.MYSQL, settings);

                if (controlDBLayout) {
                    // @formatter:off
                    // Table `Player`
                    create.execute("CREATE TABLE IF NOT EXISTS `player` (" + 
                            "  `player-id` INT UNSIGNED NOT NULL AUTO_INCREMENT," + 
                            "  `player` BINARY(16) NOT NULL," + 
                            "  PRIMARY KEY (`player-id`)," + 
                            "  UNIQUE INDEX `U_player` (`player` ASC))" + 
                            "ENGINE = InnoDB;");

                    // Table `World`
                    create.execute("CREATE TABLE IF NOT EXISTS `world` (" + 
                            "  `world-id` INT UNSIGNED NOT NULL AUTO_INCREMENT," + 
                            "  `world` BINARY(16) NOT NULL," + 
                            "  PRIMARY KEY (`world-id`)," + 
                            "  UNIQUE INDEX `U_world` (`world` ASC))" + 
                            "ENGINE = InnoDB;");

                    // Table `Warp`
                    create.execute("CREATE TABLE IF NOT EXISTS `warp` (" + 
                            "  `warp-id` INT UNSIGNED NOT NULL AUTO_INCREMENT," + 
                            "  `name` VARCHAR(32) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL," + 
                            "  `player-id` INT UNSIGNED NOT NULL," + 
                            "  `x` DOUBLE NOT NULL," + 
                            "  `y` DOUBLE NOT NULL," + 
                            "  `z` DOUBLE NOT NULL," + 
                            "  `pitch` FLOAT NOT NULL," + 
                            "  `yaw` FLOAT NOT NULL," + 
                            "  `world-id` INT UNSIGNED NOT NULL," + 
                            "  `creation-date` DATETIME NOT NULL," + 
                            "  `type` TINYINT UNSIGNED NOT NULL," + 
                            "  `visits` INT UNSIGNED NOT NULL DEFAULT 0," + 
                            "  `fee` DOUBLE UNSIGNED NULL DEFAULT NULL," + 
                            "  `welcome-message` TINYTEXT NULL DEFAULT NULL," + 
                            "  UNIQUE INDEX `U_name` (`name` ASC)," + 
                            "  PRIMARY KEY (`warp-id`)," + 
                            "  INDEX `fk_warp_player_idx` (`player-id` ASC)," + 
                            "  INDEX `fk_warp_world1_idx` (`world-id` ASC)," + 
                            "  CONSTRAINT `fk_warp_player`" + 
                            "    FOREIGN KEY (`player-id`)" + 
                            "    REFERENCES `player` (`player-id`)" + 
                            "    ON DELETE NO ACTION" + 
                            "    ON UPDATE NO ACTION," + 
                            "  CONSTRAINT `fk_warp_world1`" + 
                            "    FOREIGN KEY (`world-id`)" + 
                            "    REFERENCES `world` (`world-id`)" + 
                            "    ON DELETE NO ACTION" + 
                            "    ON UPDATE NO ACTION)" + 
                            "ENGINE = InnoDB;");

                    // Table `Group`
                    create.execute("CREATE TABLE IF NOT EXISTS `group` (" + 
                            "  `group-id` INT UNSIGNED NOT NULL AUTO_INCREMENT," + 
                            "  `group` VARCHAR(32) NOT NULL," + 
                            "  PRIMARY KEY (`group-id`)," + 
                            "  UNIQUE INDEX `U_group` (`group` ASC))" + 
                            "ENGINE = InnoDB;");

                    // Table `warp2player`
                    create.execute("CREATE TABLE IF NOT EXISTS `warp2player` (" + 
                            "  `player-id` INT UNSIGNED NOT NULL," + 
                            "  `warp-id` INT UNSIGNED NOT NULL," + 
                            "  INDEX `fk_table1_player1_idx` (`player-id` ASC)," + 
                            "  INDEX `fk_table1_warp1_idx` (`warp-id` ASC)," + 
                            "  PRIMARY KEY (`player-id`, `warp-id`)," + 
                            "  CONSTRAINT `fk_table1_player1`" + 
                            "    FOREIGN KEY (`player-id`)" + 
                            "    REFERENCES `player` (`player-id`)" + 
                            "    ON DELETE NO ACTION" + 
                            "    ON UPDATE NO ACTION," + 
                            "  CONSTRAINT `fk_table1_warp1`" + 
                            "    FOREIGN KEY (`warp-id`)" + 
                            "    REFERENCES `warp` (`warp-id`)" + 
                            "    ON DELETE NO ACTION" + 
                            "    ON UPDATE NO ACTION)" + 
                            "ENGINE = InnoDB;");

                    // Table `warp2group`
                    create.execute("CREATE TABLE IF NOT EXISTS `warp2group` (" + 
                            "  `group-id` INT UNSIGNED NOT NULL," + 
                            "  `warp-id` INT UNSIGNED NOT NULL," + 
                            "  INDEX `fk_table1_group1_idx` (`group-id` ASC)," + 
                            "  INDEX `fk_table1_warp2_idx` (`warp-id` ASC)," + 
                            "  PRIMARY KEY (`group-id`, `warp-id`)," + 
                            "  CONSTRAINT `fk_table1_group1`" + 
                            "    FOREIGN KEY (`group-id`)" + 
                            "    REFERENCES `group` (`group-id`)" + 
                            "    ON DELETE NO ACTION" + 
                            "    ON UPDATE NO ACTION," + 
                            "  CONSTRAINT `fk_table1_warp2`" + 
                            "    FOREIGN KEY (`warp-id`)" + 
                            "    REFERENCES `warp` (`warp-id`)" + 
                            "    ON DELETE NO ACTION" + 
                            "    ON UPDATE NO ACTION)" + 
                            "ENGINE = InnoDB;");
                    // @formatter:on

                    // updates should be executed at this point
                    // create.execute(...);
                }

                return new JOOQConnection(create, conn, executor);
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

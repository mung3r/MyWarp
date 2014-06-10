package me.taylorkelly.mywarp.dataconnections;

import java.io.File;
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
 * The connection to a SQlite database.
 */
public class SQLiteConnection {

    /**
     * Enforce factory usage.
     */
    private SQLiteConnection() {
    }

    /**
     * Gets a valid connection to the given SQLite database. The connection is
     * created asynchronous, the returned CheckedFuture either contains the
     * ready-to-use connection or throws a {@link DataConnectionException}.
     * 
     * @param database
     *            the database file
     * @param controlDBLayout
     *            whether the implementation should create tables and execute
     *            updates, if necessary
     * @return a valid, setup connection to the SQLite database
     */
    public static CheckedFuture<DataConnection, DataConnectionException> getConnection(final File database,
            final boolean controlDBLayout) {
        final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors
                .newSingleThreadExecutor());

        ListenableFuture<DataConnection> futureConnection = executor.submit(new Callable<DataConnection>() {


            @Override
            public DataConnection call() throws DataConnectionException {
                String dsn = "jdbc:sqlite://" + database.getAbsolutePath();
                try {
                    // Manually load SQLite driver. DriveManager is unable to
                    // identify it as the driver does not follow JDBC 4.0 standards.
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    throw new DataConnectionException("Unable to find SQLite library.", e);
                }

                Connection conn;
                try {
                    conn = DriverManager.getConnection(dsn);
                } catch (SQLException e) {
                    throw new DataConnectionException("Failed to connect to the database.", e);
                }

                // the database scheme can be configured by users
                Settings settings = new Settings().withRenderSchema(false);

                DSLContext create = DSL.using(conn, SQLDialect.SQLITE, settings);

                if (controlDBLayout) {
                    // @formatter:off
                    // Table `Player`
                    create.execute("CREATE TABLE IF NOT EXISTS `player`(" + 
                            "  `player-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`player-id`>=0)," + 
                            "  `player` BINARY(16) NOT NULL," + 
                            "  CONSTRAINT `U_player`" + 
                            "    UNIQUE(`player`)" + 
                            ");");

                    // Table `World`
                    create.execute("CREATE TABLE IF NOT EXISTS `world`(" + 
                            "  `world-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`world-id`>=0)," + 
                            "  `world` BINARY(16) NOT NULL," + 
                            "  CONSTRAINT `U_world`" + 
                            "    UNIQUE(`world`)" + 
                            ");");

                    // Table `Group`
                    create.execute("CREATE TABLE IF NOT EXISTS `group`(" + 
                            "  `group-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`group-id`>=0)," + 
                            "  `group` VARCHAR(32) NOT NULL," + 
                            "  CONSTRAINT `U_group`" + 
                            "    UNIQUE(`group`)" + 
                            ");");

                    // Table `Warp`
                    create.execute("CREATE TABLE IF NOT EXISTS `warp`(" + 
                            "  `warp-id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL CHECK(`warp-id`>=0)," + 
                            "  `name` VARCHAR(32) NOT NULL," + 
                            "  `player-id` INTEGER NOT NULL CHECK(`player-id`>=0)," + 
                            "  `x` DOUBLE NOT NULL," + 
                            "  `y` DOUBLE NOT NULL," + 
                            "  `z` DOUBLE NOT NULL," + 
                            "  `pitch` FLOAT NOT NULL," + 
                            "  `yaw` FLOAT NOT NULL," + 
                            "  `world-id` INTEGER NOT NULL CHECK(`world-id`>=0)," + 
                            "  `creation-date` DATETIME NOT NULL," + 
                            "  `type` INTEGER NOT NULL CHECK(`type`>=0)," + 
                            "  `visits` INTEGER NOT NULL CHECK(`visits`>=0) DEFAULT 0," + 
                            "  `fee` DOUBLE DEFAULT NULL," + 
                            "  `welcome-message` TINYTEXT DEFAULT NULL," + 
                            "  CONSTRAINT `U_name`" + 
                            "    UNIQUE(`name`)," + 
                            "  CONSTRAINT `fk_warp_player`" + 
                            "    FOREIGN KEY(`player-id`)" + 
                            "    REFERENCES `player`(`player-id`)," + 
                            "  CONSTRAINT `fk_warp_world1`" + 
                            "    FOREIGN KEY(`world-id`)" + 
                            "    REFERENCES `world`(`world-id`)" + 
                            ");");
                    create.execute("CREATE INDEX IF NOT EXISTS `warp.fk_warp_player_idx` ON `warp`(`player-id`);");
                    create.execute("CREATE INDEX IF NOT EXISTS `warp.fk_warp_world1_idx` ON `warp`(`world-id`);");

                    //Table `warp2player`
                    create.execute("CREATE TABLE IF NOT EXISTS `warp2player`(" + 
                            "  `player-id` INTEGER NOT NULL CHECK(`player-id`>=0)," + 
                            "  `warp-id` INTEGER NOT NULL CHECK(`warp-id`>=0)," + 
                            "  PRIMARY KEY(`player-id`,`warp-id`)," + 
                            "  CONSTRAINT `fk_table1_player1`" + 
                            "    FOREIGN KEY(`player-id`)" + 
                            "    REFERENCES `player`(`player-id`)," + 
                            "  CONSTRAINT `fk_table1_warp1`" + 
                            "    FOREIGN KEY(`warp-id`)" + 
                            "    REFERENCES `warp`(`warp-id`)" + 
                            ");");
                    create.execute("CREATE INDEX IF NOT EXISTS `warp2player.fk_table1_player1_idx` ON `warp2player`(`player-id`);");
                    create.execute("CREATE INDEX IF NOT EXISTS `warp2player.fk_table1_warp1_idx` ON `warp2player`(`warp-id`);");

                    //Table `warp2player`
                    create.execute("CREATE TABLE IF NOT EXISTS `warp2group`(" + 
                            "  `group-id` INTEGER NOT NULL CHECK(`group-id`>=0)," + 
                            "  `warp-id` INTEGER NOT NULL CHECK(`warp-id`>=0)," + 
                            "  PRIMARY KEY(`group-id`,`warp-id`)," + 
                            "  CONSTRAINT `fk_table1_group1`" + 
                            "    FOREIGN KEY(`group-id`)" + 
                            "    REFERENCES `group`(`group-id`)," + 
                            "  CONSTRAINT `fk_table1_warp2`" + 
                            "    FOREIGN KEY(`warp-id`)" + 
                            "    REFERENCES `warp`(`warp-id`)" + 
                            ");");
                    create.execute("CREATE INDEX IF NOT EXISTS `warp2group.fk_table1_group1_idx` ON `warp2group`(`group-id`);");
                    create.execute("CREATE INDEX IF NOT EXISTS `warp2group.fk_table1_warp2_idx` ON `warp2group`(`warp-id`);");
                    // @formatter:on

                    // updates should be executed at this point
                    // create.execute(...);
                }

                return new JOOQConnection(create, conn, executor);
            }

        });
        return Futures.makeChecked(futureConnection, new Function<Exception, DataConnectionException>() {

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

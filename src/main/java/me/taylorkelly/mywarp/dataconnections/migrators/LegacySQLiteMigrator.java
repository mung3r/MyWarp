package me.taylorkelly.mywarp.dataconnections.migrators;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * A migrator for legacy (pre 2.7) SQLite databases.
 */
public class LegacySQLiteMigrator extends LegacyMigrator implements DataMigrator {

    private final String dsn;

    private final static String TABLE_NAME = "warpTable";

    /**
     * Initiates this LegacySQLiteMigrator.
     * 
     * @param database
     *            the database file
     */
    public LegacySQLiteMigrator(final File database) {
        this.dsn = "jdbc:sqlite://" + database.getAbsolutePath();
    }

    @Override
    public ListenableFuture<Collection<Warp>> getWarps() {
        ListenableFutureTask<Collection<Warp>> ret = ListenableFutureTask
                .create(new Callable<Collection<Warp>>() {
                    @Override
                    public Collection<Warp> call() throws DataConnectionException {
                        try {
                            // Manually load SQLite driver. DriveManager is
                            // unable to identify it as the driver does not
                            // follow JDBC 4.0 standards.
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

                        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
                        Collection<Warp> ret = null;
                        try {
                            ret = migrateLegacyWarps(create, TABLE_NAME);
                        } finally {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                MyWarp.logger().log(Level.WARNING, "Failed to close import SQL connection.",
                                        e);
                            }
                        }

                        return ret;
                    }
                });
        new Thread(ret).start();
        return ret;
    }
}

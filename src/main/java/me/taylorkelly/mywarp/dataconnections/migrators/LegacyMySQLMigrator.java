package me.taylorkelly.mywarp.dataconnections.migrators;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

/**
 * A migrator for legacy (pre 2.7) MySQL databases.
 */
public class LegacyMySQLMigrator extends LegacyMigrator implements DataMigrator {

    private final String dsn;

    private final String user;

    private final String password;

    private final String tableName;

    /**
     * Initiates this LegacyMySQLMigrator.
     * 
     * @param host
     *            the host of the MySQL server
     * @param port
     *            the port the MySQL server listens to
     * @param database
     *            the name of the MySQL database to use
     * @param user
     *            the MySQL user to use
     * @param password
     *            the user's password
     * @param tableName
     *            the name of the table that contains the data
     */
    public LegacyMySQLMigrator(final String host, final int port, final String database, final String user,
            final String password, final String tableName) {
        this.dsn = "jdbc:mysql://" + host + ":" + port + "/" + database;
        this.user = user;
        this.password = password;
        this.tableName = tableName;
    }

    @Override
    public ListenableFuture<Collection<Warp>> getWarps() {
        ListenableFutureTask<Collection<Warp>> ret = ListenableFutureTask
                .create(new Callable<Collection<Warp>>() {
                    @Override
                    public Collection<Warp> call() throws DataConnectionException {
                        Connection conn;
                        try {
                            conn = DriverManager.getConnection(dsn, user, password);
                        } catch (SQLException e) {
                            throw new DataConnectionException("Failed to connect to the database.", e);
                        }

                        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
                        Collection<Warp> ret = null;
                        try {
                            ret = migrateLegacyWarps(create, tableName);
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

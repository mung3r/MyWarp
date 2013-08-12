package me.taylorkelly.mywarp.dataconnections;

import java.io.File;
import java.util.Map;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

/**
 * This class manages data connections and provides a centralized (and asyc)
 * access to their methods. Each ConnectionManager handles one data connection
 * only.
 */
public class ConnectionManager implements DataConnection {

    /**
     * The data connection that provides the database access
     */
    private final DataConnection handler;

    /**
     * Initializes this connection-manager by initializing the
     * {@link DataConnection} that should be used. Upon success the handler
     * checks and updates the database, if necessary.
     * 
     * @param useMySQL
     *            whether MySQL should be used (true) or SQLite (false)
     * @param createIfNotExist
     *            whether the database should be created if it does not exist
     * @param updateIfNecessary
     *            whether the database should be updated if necessary
     * @throws DataConnectionException
     *             if any circumstance prevents working with the database
     */
    public ConnectionManager(boolean useMySQL, boolean createIfNotExist, boolean updateIfNecessary)
            throws DataConnectionException {

        if (useMySQL) {
            // Use MySQL
            handler = new MySQLConnection("jdbc:mysql://" + MyWarp.inst().getWarpSettings().mysqlHost + ":"
                    + MyWarp.inst().getWarpSettings().mysqlPort + "/"
                    + MyWarp.inst().getWarpSettings().mysqlDatabase, MyWarp.inst().getWarpSettings().mysqlUsername,
                    MyWarp.inst().getWarpSettings().mysqlPassword, MyWarp.inst().getWarpSettings().mysqlTable);
        } else {
            // Use SQLite
            try {
                // Manually load SQLite driver. DriveManager is unable to
                // identify it as the driver does not follow JDBC 4.0 standards.
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                MyWarp.logger().severe("Unable to find SQLite library.");
                throw new DataConnectionException();
            }
            handler = new SQLiteConnection("jdbc:sqlite://" + MyWarp.inst().getDataFolder().getAbsolutePath()
                    + File.separator + "warps.db", "warpTable");
        }

        checkDB(createIfNotExist);
        updateDB(updateIfNecessary);
    }

    @Override
    public void close() {
        handler.close();
    }

    @Override
    public void checkDB(boolean createIfNotExist) throws DataConnectionException {
        handler.checkDB(createIfNotExist);
    }

    @Override
    public void updateDB(boolean updateIfNecessary) throws DataConnectionException {
        handler.updateDB(updateIfNecessary);
    }

    @Override
    public Map<String, Warp> getMap() {
        return handler.getMap();
    }

    @Override
    public void addWarp(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.addWarp(warp);
            }
        });
    }

    @Override
    public void deleteWarp(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.deleteWarp(warp);
            }
        });
    }

    @Override
    public void publicizeWarp(final Warp warp, final boolean publicAll) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.publicizeWarp(warp, publicAll);
            }
        });
    }

    @Override
    public void updateCreator(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.updateCreator(warp);
            }
        });
    }

    @Override
    public void updateLocation(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.updateLocation(warp);
            }
        });
    }

    @Override
    public void updatePermissions(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.updatePermissions(warp);
            }
        });
    }

    @Override
    public void updateGroupPermissions(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.updateGroupPermissions(warp);
            }
        });
    }

    @Override
    public void updateVisits(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.updateVisits(warp);
            }
        });
    }

    @Override
    public void updateWelcomeMessage(final Warp warp) {
        MyWarp.server().getScheduler().runTaskAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                handler.updateWelcomeMessage(warp);
            }
        });
    }
}

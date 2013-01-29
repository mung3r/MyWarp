package me.taylorkelly.mywarp.dataconnections;

import java.io.File;
import java.util.HashMap;

import org.bukkit.scheduler.BukkitScheduler;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.WarpLogger;

public class ConnectionManager implements DataConnection {

    private DataConnection handler;
    private MyWarp plugin;
    private BukkitScheduler scheduler;

    public ConnectionManager(boolean useMySQL, boolean createIfNotExist,
            boolean updateIfNecessary, MyWarp plugin)
            throws DataConnectionException {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();

        if (useMySQL) {
            // Use MySQL
            handler = new MySQLConnection("jdbc:mysql://"
                    + WarpSettings.mySQLhost + ":" + WarpSettings.mySQLport
                    + "/" + WarpSettings.mySQLdb, WarpSettings.mySQLuname,
                    WarpSettings.mySQLpass, WarpSettings.mySQLtable);
        } else {
            // Use SQLite
            try {
                // Manually load SQLite driver. DriveManager is unable to
                // identify it as the driver does not follow JDBC 4.0 standards.
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                WarpLogger.severe("Unable to find SQLite library.");
                throw new DataConnectionException();
            }
            handler = new SQLiteConnection("jdbc:sqlite://"
                    + WarpSettings.dataDir.getAbsolutePath() + File.separator
                    + "warps.db", "warpTable");
        }

        checkDB(createIfNotExist);
        updateDB(updateIfNecessary);
    }

    @Override
    public void close() {
        handler.close();
    }

    @Override
    public void checkDB(boolean createIfNotExist)
            throws DataConnectionException {
        handler.checkDB(createIfNotExist);
    }

    @Override
    public void updateDB(boolean updateIfNecessary)
            throws DataConnectionException {
        handler.updateDB(updateIfNecessary);
    }

    @Override
    public HashMap<String, Warp> getMap() {
        return handler.getMap();
    }

    @Override
    public void addWarp(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.addWarp(warp);
            }
        });
    }

    @Override
    public void deleteWarp(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.deleteWarp(warp);
            }
        });
    }

    @Override
    public void publicizeWarp(final Warp warp, final boolean publicAll) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.publicizeWarp(warp, publicAll);
            }
        });
    }

    @Override
    public void updateCreator(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.updateCreator(warp);
            }
        });
    }

    @Override
    public void updateLocation(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.updateLocation(warp);
            }
        });
    }

    @Override
    public void updatePermissions(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.updatePermissions(warp);
            }
        });
    }

    @Override
    public void updateGroupPermissions(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.updateGroupPermissions(warp);
            }
        });
    }

    @Override
    public void updateVisits(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.updateVisits(warp);
            }
        });
    }

    @Override
    public void updateWelcomeMessage(final Warp warp) {
        scheduler.runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                handler.updateWelcomeMessage(warp);
            }
        });
    }
}

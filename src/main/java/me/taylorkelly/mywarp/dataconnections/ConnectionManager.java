package me.taylorkelly.mywarp.dataconnections;

import java.io.File;
import java.util.HashMap;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.WarpLogger;

public class ConnectionManager implements DataConnection {

    private DataConnection handler;

    public ConnectionManager() throws NoConnectionException {
        if (WarpSettings.usemySQL) {
            // Use MySQL
            handler = new MySQLConnection("jdbc:mysql://" + WarpSettings.mySQLhost + ":"
                    + WarpSettings.mySQLport + "/" + WarpSettings.mySQLdb,
                    WarpSettings.mySQLuname, WarpSettings.mySQLpass,
                    WarpSettings.mySQLtable);
        } else {
            // Use SQLite
            try {
                // Manually load SQLite driver. DriveManager is unable to
                // identify it as the driver does not follow JDBC 4.0 standards.
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                WarpLogger.severe("Unable to find SQLite library.");
                throw new NoConnectionException();
            }
            handler = new SQLiteConnection("jdbc:sqlite://"
                    + WarpSettings.dataDir.getAbsolutePath() + File.separator
                    + "warps.db", "warpTable");
        }

        if (!checkDB()) {
            throw new NoConnectionException();
        }
        if (!updateDB()) {
            throw new NoConnectionException();
        }
    }

    @Override
    public void close() {
        handler.close();
    }

    @Override
    public boolean checkDB() {
        return handler.checkDB();
    }

    @Override
    public boolean updateDB() {
        return handler.updateDB();
    }

    @Override
    public HashMap<String, Warp> getMap() {
        return handler.getMap();
    }

    @Override
    public void addWarp(Warp warp) {
        handler.addWarp(warp);
    }

    @Override
    public void deleteWarp(Warp warp) {
        handler.deleteWarp(warp);
    }

    @Override
    public void publicizeWarp(Warp warp, boolean publicAll) {
        handler.publicizeWarp(warp, publicAll);
    }

    @Override
    public void updateCreator(Warp warp) {
        handler.updateCreator(warp);
    }

    @Override
    public void updateLocation(Warp warp) {
        handler.updateLocation(warp);
    }

    @Override
    public void updatePermissions(Warp warp) {
        handler.updatePermissions(warp);
    }

    @Override
    public void updateGroupPermissions(Warp warp) {
        handler.updateGroupPermissions(warp);
    }

    @Override
    public void updateVisits(Warp warp) {
        handler.updateVisits(warp);
    }

    @Override
    public void updateWelcomeMessage(Warp warp) {
        handler.updateWelcomeMessage(warp);
    }
}

package me.taylorkelly.mywarp.dataconnections;

import java.util.HashMap;

import me.taylorkelly.mywarp.data.Warp;

public interface DataConnection {

    public void close();

    public void checkDB(boolean createIfNotExist) throws DataConnectionException;

    public void updateDB(boolean updateIfNecessary) throws DataConnectionException;

    public HashMap<String, Warp> getMap();

    public void addWarp(Warp warp);

    public void deleteWarp(Warp warp);

    public void publicizeWarp(Warp warp, boolean publicAll);

    public void updateCreator(Warp warp);

    public void updateLocation(Warp warp);

    public void updatePermissions(Warp warp);

    public void updateGroupPermissions(Warp warp);

    public void updateVisits(Warp warp);

    public void updateWelcomeMessage(Warp warp);
}

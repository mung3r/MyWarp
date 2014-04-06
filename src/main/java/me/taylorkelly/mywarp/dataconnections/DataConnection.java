package me.taylorkelly.mywarp.dataconnections;

import java.util.Collection;

import me.taylorkelly.mywarp.data.Warp;

/**
 * This interface defines all usable methods for data-connections used by
 * MyWarp. Implementations are expected to be threadsafe!
 */
public interface DataConnection {

    /**
     * Closes all pending database connections
     */
    public void close();

    /**
     * Checks the given database. Depending on the implementation this could be
     * used to check if the database exists, if it contains all values etc.
     * 
     * @param createIfNotExist
     *            whether the method should create the database if it does not
     *            exist
     * @throws DataConnectionException
     *             if any circumstance prevents working with the database
     */
    public void checkDB(boolean createIfNotExist) throws DataConnectionException;

    /**
     * Executes updates to the database. Depending on the implementation this
     * could be used to add missing values, change database-layouts etc.
     * 
     * @param updateIfNecessary
     *            whether the method should update the database if necessary
     * @throws DataConnectionException
     *             if any circumstance prevents working with the database
     */
    public void updateDB(boolean updateIfNecessary) throws DataConnectionException;

    /**
     * Loads all warps from the database and returns them as map. Each warp is
     * stored under their name.
     * 
     * @return a map with all warps
     */
    public Collection<Warp> getWarps();

    /**
     * Adds the warp to the database
     * 
     * @param warp
     *            the warp
     */
    public void addWarp(Warp warp);

    /**
     * Deletes the warp from the database
     * 
     * @param warp
     *            the warp
     */
    public void deleteWarp(Warp warp);

    /**
     * Update's a warp's type in the databse
     * 
     * @param warp
     *            the warp
     */
    public void updateType(Warp warp);

    /**
     * Updates a warp's creator in the database
     * 
     * @param warp
     *            the warp
     */
    public void updateCreator(Warp warp);

    /**
     * Updates the location of the given warp in the database
     * 
     * @param warp
     *            the warp
     */
    public void updateLocation(Warp warp);

    /**
     * Updates the permissions (list of invited players) of the given warp in
     * the database
     * 
     * @param warp
     *            the warp
     */
    public void updateInvitedPlayers(Warp warp);

    /**
     * Updates the group-permissions (list of invited groups) of the given warp
     * in the database
     * 
     * @param warp
     *            the warp
     */
    public void updateInvitedGroups(Warp warp);

    /**
     * Updates the visits of the given warp in the database
     * 
     * @param warp
     *            the warp
     */
    public void updateVisits(Warp warp);

    /**
     * Updates the welcome message of the given warp in the database
     * 
     * @param warp
     *            the warp
     */
    public void updateWelcomeMessage(Warp warp);
}

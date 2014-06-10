package me.taylorkelly.mywarp.dataconnections;

import java.util.Collection;
import java.util.UUID;

import com.google.common.util.concurrent.ListenableFuture;

import me.taylorkelly.mywarp.data.Warp;

/**
 * A connection to a data-source.
 */
public interface DataConnection {

    /**
     * Closes any connection to the underlying data-source.
     */
    public void close();

    /**
     * Adds the given warp to the underlying data-source.
     * 
     * @param warp
     *            the warp
     */
    public void addWarp(Warp warp);

    /**
     * Deletes the given warp from the underlying data-source.
     * 
     * @param warp
     *            the warp
     */
    public void deleteWarp(Warp warp);

    /**
     * Gets all warps from the underlying data-source.
     * 
     * @return a ListenableFuture that contains a collection of warps
     */
    public ListenableFuture<Collection<Warp>> getWarps();

    /**
     * Adds the given group to the invited groups for the given warp.
     * 
     * @param warp
     *            the warp
     * @param group
     *            the identifier of the group
     */
    public void inviteGroup(Warp warp, String group);

    /**
     * Adds the given playerId to the invited playerIds for the given players.
     * 
     * @param warp
     *            the warp
     * @param playerId
     *            the player-id
     */
    public void invitePlayer(Warp warp, UUID playerId);

    /**
     * Removes the given group from the invited groups for the given warp.
     * 
     * @param warp
     *            the warp
     * @param group
     *            the group identifier
     */
    public void uninviteGroup(Warp warp, String group);

    /**
     * Removes the given playerId from the invited playerIds for the given warp
     * 
     * @param warp
     *            the warp
     * @param playerId
     *            the player-id
     */
    public void uninvitePlayer(Warp warp, UUID playerId);

    /**
     * Updates the creator of the given warp-
     * 
     * @param warp
     *            the warp
     */
    public void updateCreator(Warp warp);

    /**
     * Updates the location of the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void updateLocation(Warp warp);

    /**
     * Updates the type of the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void updateType(Warp warp);

    /**
     * Updates the visits of the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void updateVisits(Warp warp);

    /**
     * Update the welcome-message of the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void updateWelcomeMessage(Warp warp);

}

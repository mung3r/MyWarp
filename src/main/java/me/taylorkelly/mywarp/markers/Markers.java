package me.taylorkelly.mywarp.markers;

import me.taylorkelly.mywarp.data.Warp;

/**
 * Sets location-markers for warps
 */
public interface Markers {

    /**
     * Represents the way a warp was updated.
     */
    public enum UpdateType {
        /**
         * The warp's creator was updated.
         */
        CREATOR,
        /**
         * The groups invited to the warp were updated.
         */
        INVITED_GROUPS,
        /**
         * The players invited to the warp where updated.
         */
        INVITED_PLAYERS,
        /**
         * The warp's location was updated.
         */
        LOCATION,
        /**
         * The warp's type was updated.
         */
        TYPE,
        /**
         * The warp's visit-counter was updated.
         */
        VISITS,
        /**
         * The warp's welcome-message was updated.
         */
        WELCOME_MESSAGE;
    }

    /**
     * Adds a marker for the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void addMarker(Warp warp);

    /**
     * Deletes the marker of the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void deleteMarker(Warp warp);

    /**
     * Updates the given warp's marker.
     * 
     * @see Warp#replacePlaceholders(String)
     * @param warp
     *            the warp
     * @param type
     *            the type of updated
     */
    public void updateMarker(Warp warp, UpdateType type);

}

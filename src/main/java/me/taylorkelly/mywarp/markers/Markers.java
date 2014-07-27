package me.taylorkelly.mywarp.markers;

import me.taylorkelly.mywarp.data.Warp;

/**
 * Sets location-markers for warps
 */
public interface Markers {

    /**
     * Adds markers for the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void addMarker(Warp warp);

    /**
     * Deletes markers previously set for the given warp.
     * 
     * @param warp
     */
    public void deleteMarker(Warp warp);

    /**
     * Handles type changes of the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void handleTypeChange(Warp warp);

    /**
     * Updates existing markers for the given warp.
     * 
     * @param warp
     *            the warp
     */
    public void updateMarker(Warp warp);

}

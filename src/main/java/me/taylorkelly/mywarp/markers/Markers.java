/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.mywarp.markers;

import me.taylorkelly.mywarp.data.Warp;

/**
 * Sets location-markers for warps.
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
    void addMarker(Warp warp);

    /**
     * Deletes the marker of the given warp.
     * 
     * @param warp
     *            the warp
     */
    void deleteMarker(Warp warp);

    /**
     * Updates the given warp's marker.
     * 
     * @see Warp#replacePlaceholders(String)
     * @param warp
     *            the warp
     * @param type
     *            the type of updated
     */
    void updateMarker(Warp warp, UpdateType type);

}

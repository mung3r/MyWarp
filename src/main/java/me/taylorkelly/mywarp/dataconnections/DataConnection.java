/*
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
package me.taylorkelly.mywarp.dataconnections;

import java.util.Collection;

import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * A connection to a data-source.
 */
public interface DataConnection {

    /**
     * Closes any connection to the underlying data-source.
     */
    void close();

    /**
     * Adds the given warp to the underlying data-source.
     * 
     * @param warp
     *            the warp
     */
    void addWarp(Warp warp);

    /**
     * Removes the given warp from the underlying data-source.
     * 
     * @param warp
     *            the warp
     */
    void removeWarp(Warp warp);

    /**
     * Gets all warps from the underlying data-source.
     * 
     * @return a ListenableFuture that contains a collection of warps
     */
    ListenableFuture<Collection<Warp>> getWarps();

    /**
     * Adds the given groupId to the list of invited groupIdss for the given
     * warp.
     * 
     * @param warp
     *            the warp
     * @param groupId
     *            the ID of the group
     */
    void inviteGroup(Warp warp, String groupId);

    /**
     * Adds the given player-profile to the invited player-profiles for the
     * given warp.
     * 
     * @param warp
     *            the warp
     * @param playerProfile
     *            the player-profile
     */
    void invitePlayer(Warp warp, Profile playerProfile);

    /**
     * Removes the given groupId from the list of invited groupIds for the given
     * warp.
     * 
     * @param warp
     *            the warp
     * @param groupId
     *            the ID of the group
     */
    void uninviteGroup(Warp warp, String groupId);

    /**
     * Removes the given player-profile from the invited player-profiles for the
     * given warp.
     * 
     * @param warp
     *            the warp
     * @param playerProfile
     *            the profile of the player
     */
    void uninvitePlayer(Warp warp, Profile playerProfile);

    /**
     * Updates the creator of the given warp.
     * 
     * @param warp
     *            the warp
     */
    void updateCreator(Warp warp);

    /**
     * Updates the location of the given warp.
     * 
     * @param warp
     *            the warp
     */
    void updateLocation(Warp warp);

    /**
     * Updates the type of the given warp.
     * 
     * @param warp
     *            the warp
     */
    void updateType(Warp warp);

    /**
     * Updates the visits of the given warp.
     * 
     * @param warp
     *            the warp
     */
    void updateVisits(Warp warp);

    /**
     * Update the welcome-message of the given warp.
     * 
     * @param warp
     *            the warp
     */
    void updateWelcomeMessage(Warp warp);

}

/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

import me.taylorkelly.mywarp.warp.event.WarpAdditionEvent;
import me.taylorkelly.mywarp.warp.event.WarpGroupInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpPlayerInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpRemovalEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent;

import com.google.common.eventbus.Subscribe;

/**
 * Provides a bridge between the various
 * {@link me.taylorkelly.mywarp.warp.event.WarpEvent} implementations and a
 * {@link DataConnection}, transforming relevant events into calls on the
 * data-connection.
 */
public class EventConnectionBridge {

    private final DataConnection conn;

    /**
     * Initializing this bridge on the given DataConnection.
     * 
     * @param conn
     *            the DataConnection
     */
    public EventConnectionBridge(DataConnection conn) {
        this.conn = conn;
    }

    /**
     * Called when a Warp is added.
     * 
     * @param event
     *            the event
     */
    @Subscribe
    public void onWarpAddition(WarpAdditionEvent event) {
        conn.addWarp(event.getWarp());
    }

    /**
     * Called when a Warp is removed.
     * 
     * @param event
     *            the event
     */
    @Subscribe
    public void onWarpRemoval(WarpRemovalEvent event) {
        conn.removeWarp(event.getWarp());
    }

    /**
     * Called when a Warp is updated in some way.
     * 
     * @param event
     *            the event
     */
    @Subscribe
    public void onWarpUpdate(WarpUpdateEvent event) {
        switch (event.getType()) {
        case CREATOR:
            conn.updateCreator(event.getWarp());
            break;
        case LOCATION:
            conn.updateLocation(event.getWarp());
            break;
        case TYPE:
            conn.updateType(event.getWarp());
            break;
        case VISITS:
            conn.updateVisits(event.getWarp());
            break;
        case WELCOME_MESSAGE:
            conn.updateWelcomeMessage(event.getWarp());
            break;
        }
    }

    /**
     * Called when the groups invited to a Warp are changed.
     * 
     * @param event
     *            the event
     */
    @Subscribe
    public void onWarpGroupInvites(WarpGroupInvitesEvent event) {
        switch (event.getInvitationStatus()) {
        case INVITE:
            conn.inviteGroup(event.getWarp(), event.getGroupId());
            break;
        case UNINVITE:
            conn.uninviteGroup(event.getWarp(), event.getGroupId());
            break;
        }
    }

    /**
     * Called when the player invited to a Warp are changed.
     * 
     * @param event
     *            the event
     */
    @Subscribe
    public void onWarpPlayerInvites(WarpPlayerInvitesEvent event) {
        switch (event.getInvitationStatus()) {
        case INVITE:
            conn.invitePlayer(event.getWarp(), event.getProfile());
            break;
        case UNINVITE:
            conn.uninvitePlayer(event.getWarp(), event.getProfile());
            break;
        }
    }

}

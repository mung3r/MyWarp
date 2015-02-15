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

package me.taylorkelly.mywarp.warp;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.safety.TeleportService.TeleportStatus;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.event.WarpGroupInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpInvitesEvent.InvitationStatus;
import me.taylorkelly.mywarp.warp.event.WarpPlayerInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent.UpdateType;

/**
 * Forwards method calls to an existing Warp and posts {@link me.taylorkelly.mywarp.warp.event.WarpEvent}s to an
 * EventWarpManager.
 */
public class EventfullWarp extends ForwardingWarp {

  private final Warp warp;
  private final EventWarpManager eventManager;

  /**
   * Creates the instance that forwards calls to the given Warp. Events will be posted to the given EventWarpManager.
   *
   * @param warp         the Warp
   * @param eventManager the EventWarpManager
   */
  public EventfullWarp(Warp warp, EventWarpManager eventManager) {
    this.warp = warp;
    this.eventManager = eventManager;
  }

  @Override
  protected Warp delegate() {
    return warp;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity) {
    TeleportStatus ret = super.teleport(entity);
    switch (ret) {
      case ORIGINAL_LOC:
      case SAFE_LOC:
        eventManager.postEvent(new WarpUpdateEvent(this, UpdateType.VISITS));
        break;
      case NONE:
        break;
    }
    return ret;
  }

  @Override
  public void inviteGroup(String groupId) {
    super.inviteGroup(groupId);
    eventManager.postEvent(new WarpGroupInvitesEvent(this, InvitationStatus.INVITE, groupId));

  }

  @Override
  public void invitePlayer(Profile player) {
    super.invitePlayer(player);
    eventManager.postEvent(new WarpPlayerInvitesEvent(this, InvitationStatus.INVITE, player));

  }

  @Override
  public void uninviteGroup(String groupId) {
    super.uninviteGroup(groupId);
    eventManager.postEvent(new WarpGroupInvitesEvent(this, InvitationStatus.UNINVITE, groupId));

  }

  @Override
  public void uninvitePlayer(Profile player) {
    super.uninvitePlayer(player);
    eventManager.postEvent(new WarpPlayerInvitesEvent(this, InvitationStatus.UNINVITE, player));

  }

  @Override
  public void setCreator(Profile creator) {
    super.setCreator(creator);
    eventManager.postEvent(new WarpUpdateEvent(this, UpdateType.CREATOR));

  }

  @Override
  public void setLocation(LocalWorld world, Vector3 position, EulerDirection rotation) {
    super.setLocation(world, position, rotation);
    eventManager.postEvent(new WarpUpdateEvent(this, UpdateType.LOCATION));

  }

  @Override
  public void setType(Type type) {
    super.setType(type);
    eventManager.postEvent(new WarpUpdateEvent(this, UpdateType.TYPE));
  }

  @Override
  public void setWelcomeMessage(String welcomeMessage) {
    super.setWelcomeMessage(welcomeMessage);
    eventManager.postEvent(new WarpUpdateEvent(this, UpdateType.WELCOME_MESSAGE));
  }

}

/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.teleport.TeleportService.TeleportStatus;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.event.WarpAdditionEvent;
import me.taylorkelly.mywarp.warp.event.WarpDeletionEvent;
import me.taylorkelly.mywarp.warp.event.WarpEvent;
import me.taylorkelly.mywarp.warp.event.WarpGroupInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpPlayerInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent;

/**
 * Fires events for all warps managed by it. Functional calls are all delegated to an underling WarpManager as required
 * by the decorator pattern and events are implemented on top. <p>Events are dispatched in the {@link EventBus} given
 * when initializing this WarpManager.  Individual warps fire {@link WarpEvent}s and the manager itself fires {@link
 * WarpAdditionEvent}s and {@link me.taylorkelly.mywarp.warp.event.WarpDeletionEvent}s when Warps are added to or
 * removed from it. Handlers that want to listen to such events need to register themselves on the EventBus.</p>
 */
public class EventfulWarpManager extends ForwardingWarpManager {

  private final WarpManager warpManager;
  private final EventBus eventBus;

  /**
   * Creates an instance working on top of the given WarpManager.
   *
   * @param warpManager the WarpManager
   * @param eventBus    the EventBus
   */
  public EventfulWarpManager(WarpManager warpManager, EventBus eventBus) {
    this.warpManager = warpManager;
    this.eventBus = eventBus;
  }

  @Override
  protected WarpManager delegate() {
    return warpManager;
  }

  @Override
  public void add(Warp warp) {
    warp = new EventfulWarp(warp);
    delegate().add(warp);
    eventBus.post(new WarpAdditionEvent(warp));
  }

  @Override
  public void populate(Iterable<Warp> warps) {
    delegate().populate(Iterables.transform(warps, new Function<Warp, Warp>() {

      @Override
      public EventfulWarp apply(Warp input) {
        return new EventfulWarp(input);
      }
    }));
  }

  @Override
  public void remove(Warp warp) {
    delegate().remove(warp);
    eventBus.post(new WarpDeletionEvent(warp));
  }

  /**
   * Forwards method calls to an existing Warp and fires {@link WarpEvent}s to the parent's EventBus.
   */
  private class EventfulWarp extends ForwardingWarp {

    private final Warp warp;

    /**
     * Creates the instance that forwards calls to the given Warp.
     *
     * @param warp the Warp
     */
    private EventfulWarp(Warp warp) {
      this.warp = warp;
    }

    @Override
    protected Warp delegate() {
      return warp;
    }

    @Override
    public TeleportStatus teleport(LocalEntity entity) {
      TeleportStatus ret = super.teleport(entity);
      if (ret.isPositionModified()) {
        eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.VISITS));
      }
      return ret;
    }

    @Override
    public TeleportStatus teleport(LocalPlayer player, FeeProvider.FeeType fee) {
      TeleportStatus ret = super.teleport(player, fee);
      if (ret.isPositionModified()) {
        eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.VISITS));
      }
      return ret;
    }

    @Override
    public void inviteGroup(String groupId) {
      super.inviteGroup(groupId);
      eventBus.post(new WarpGroupInvitesEvent(this, WarpInvitesEvent.InvitationStatus.INVITE, groupId));

    }

    @Override
    public void invitePlayer(Profile player) {
      super.invitePlayer(player);
      eventBus.post(new WarpPlayerInvitesEvent(this, WarpInvitesEvent.InvitationStatus.INVITE, player));

    }

    @Override
    public void uninviteGroup(String groupId) {
      super.uninviteGroup(groupId);
      eventBus.post(new WarpGroupInvitesEvent(this, WarpInvitesEvent.InvitationStatus.UNINVITE, groupId));

    }

    @Override
    public void uninvitePlayer(Profile player) {
      super.uninvitePlayer(player);
      eventBus.post(new WarpPlayerInvitesEvent(this, WarpInvitesEvent.InvitationStatus.UNINVITE, player));

    }

    @Override
    public void setCreator(Profile creator) {
      super.setCreator(creator);
      eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.CREATOR));

    }

    @Override
    public void setLocation(LocalWorld world, Vector3 position, EulerDirection rotation) {
      super.setLocation(world, position, rotation);
      eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.LOCATION));

    }

    @Override
    public void setType(Type type) {
      super.setType(type);
      eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.TYPE));
    }

    @Override
    public void setWelcomeMessage(String welcomeMessage) {
      super.setWelcomeMessage(welcomeMessage);
      eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.WELCOME_MESSAGE));
    }

  }
}

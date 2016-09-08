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

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;

import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.util.teleport.TeleportHandler;
import me.taylorkelly.mywarp.util.teleport.TeleportHandler.TeleportStatus;
import me.taylorkelly.mywarp.warp.event.WarpAdditionEvent;
import me.taylorkelly.mywarp.warp.event.WarpDeletionEvent;
import me.taylorkelly.mywarp.warp.event.WarpEvent;
import me.taylorkelly.mywarp.warp.event.WarpGroupInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpPlayerInvitesEvent;
import me.taylorkelly.mywarp.warp.event.WarpUpdateEvent;

import java.util.UUID;

/**
 * Fires events for all warps managed by it. Functional calls are all delegated to an underling PopulatableWarpManager
 * as required by the decorator pattern, events are implemented on top.
 *
 * <p>Events are dispatched in the {@link EventBus} given when initializing this PopulatableWarpManager. Individual
 * warps fire {@link WarpEvent}s and the manager itself fires {@link WarpAdditionEvent}s and {@link
 * me.taylorkelly.mywarp.warp.event.WarpDeletionEvent}s when Warps are added to or removed from it. Handlers that want
 * to listen to such events need to register themselves on the EventBus.</p>
 */
public class EventfulPopulatableWarpManager extends ForwardingPopulatableWarpManager {

  private final PopulatableWarpManager delegate;
  private final EventBus eventBus;

  /**
   * Creates an instance that posts events on the given {@code eventBus}. Further management is delegated to the given
   * PopulatableWarpManager.
   *
   * @param delegate the PopulatableWarpManager to delegate calls to
   * @param eventBus the EventBus on which this manager will post events
   */
  public EventfulPopulatableWarpManager(PopulatableWarpManager delegate, EventBus eventBus) {
    this.delegate = delegate;
    this.eventBus = eventBus;
  }

  @Override
  protected PopulatableWarpManager delegate() {
    return delegate;
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

    private final Warp delegate;

    private EventfulWarp(Warp delegate) {
      this.delegate = delegate;
    }

    @Override
    protected Warp delegate() {
      return delegate;
    }

    @Override
    public TeleportStatus visit(LocalEntity entity, Game game, TeleportHandler handler) {
      TeleportStatus status = delegate().visit(entity, game, handler);

      if (status.isPositionModified()) {
        eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.VISITS));
      }
      return status;
    }

    @Override
    public void inviteGroup(String groupId) {
      super.inviteGroup(groupId);
      eventBus.post(new WarpGroupInvitesEvent(this, WarpInvitesEvent.InvitationStatus.INVITE, groupId));

    }

    @Override
    public void invitePlayer(UUID uniqueId) {
      super.invitePlayer(uniqueId);
      eventBus.post(new WarpPlayerInvitesEvent(this, WarpInvitesEvent.InvitationStatus.INVITE, uniqueId));

    }

    @Override
    public void uninviteGroup(String groupId) {
      super.uninviteGroup(groupId);
      eventBus.post(new WarpGroupInvitesEvent(this, WarpInvitesEvent.InvitationStatus.UNINVITE, groupId));

    }

    @Override
    public void uninvitePlayer(UUID uniqueId) {
      super.uninvitePlayer(uniqueId);
      eventBus.post(new WarpPlayerInvitesEvent(this, WarpInvitesEvent.InvitationStatus.UNINVITE, uniqueId));

    }

    @Override
    public void setCreator(UUID uniqueId) {
      super.setCreator(uniqueId);
      eventBus.post(new WarpUpdateEvent(this, WarpUpdateEvent.UpdateType.CREATOR));

    }

    @Override
    public void setLocation(LocalWorld world, Vector3d position, Vector2f rotation) {
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

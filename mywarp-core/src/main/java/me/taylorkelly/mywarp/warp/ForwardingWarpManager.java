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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.teleport.TeleportService.TeleportStatus;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.MatchList;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Forwards all method calls to another WarpManager. Subclasses should override one or more methods to modify the
 * behavior of the backing WarpManager as desired per the <a href="http://en.wikipedia
 * .org/wiki/Decorator_pattern">decorator pattern</a>.
 */
abstract class ForwardingWarpManager extends ForwardingObject implements WarpManager {

  @Override
  public void add(Warp warp) {
    delegate().add(warp);
  }

  @Override
  public void populate(Iterable<Warp> warps) {
    delegate().populate(warps);
  }

  @Override
  public void remove(Warp warp) {
    delegate().remove(warp);
  }

  @Override
  public void clear() {
    delegate().clear();
  }

  @Override
  public int getSize() {
    return delegate().getSize();
  }

  @Override
  public boolean contains(String name) {
    return delegate().contains(name);
  }

  @Override
  public Optional<Warp> get(String name) {
    return delegate().get(name);
  }

  @Override
  public Collection<Warp> filter(Predicate<Warp> predicate) {
    return delegate().filter(predicate);
  }

  @Override
  public MatchList getMatchingWarps(String filter, Predicate<Warp> predicate) {
    return delegate().getMatchingWarps(filter, predicate);
  }

  @Override
  protected abstract WarpManager delegate();

  /**
   * Forwards all method calls to another Warp. Subclasses should override one or more methods to modify the behavior of
   * the backing Warp as desired per the <a href="http://en.wikipedia .org/wiki/Decorator_pattern">decorator
   * pattern</a>.
   */
  protected abstract class ForwardingWarp extends ForwardingObject implements Warp {

    @Override
    public void visit(LocalEntity entity, TeleportStatus status) {
      delegate().visit(entity, status);
    }

    @Override
    public boolean isCreator(LocalPlayer player) {
      return delegate().isCreator(player);
    }

    @Override
    public boolean isCreator(Profile profile) {
      return delegate().isCreator(profile);
    }

    @Override
    public boolean isType(Type type) {
      return delegate().isType(type);
    }

    @Override
    public boolean isPlayerInvited(LocalPlayer player) {
      return delegate().isPlayerInvited(player);
    }

    @Override
    public boolean isPlayerInvited(Profile profile) {
      return delegate().isPlayerInvited(profile);
    }

    @Override
    public boolean isGroupInvited(String groupId) {
      return delegate().isGroupInvited(groupId);
    }

    @Override
    public void inviteGroup(String groupId) {
      delegate().inviteGroup(groupId);
    }

    @Override
    public void invitePlayer(Profile player) {
      delegate().invitePlayer(player);
    }

    @Override
    public void uninviteGroup(String groupId) {
      delegate().uninviteGroup(groupId);
    }

    @Override
    public void uninvitePlayer(Profile player) {
      delegate().uninvitePlayer(player);
    }

    @Override
    public int compareTo(Warp that) {
      return delegate().compareTo(that);
    }

    @Override
    public Profile getCreator() {
      return delegate().getCreator();
    }

    @Override
    public void setCreator(Profile creator) {
      delegate().setCreator(creator);
    }

    @Override
    public Set<String> getInvitedGroups() {
      return delegate().getInvitedGroups();
    }

    @Override
    public Set<Profile> getInvitedPlayers() {
      return delegate().getInvitedPlayers();
    }

    @Override
    public String getName() {
      return delegate().getName();
    }

    @Override
    public LocalWorld getWorld(Game game) {
      return delegate().getWorld(game);
    }

    @Override
    public UUID getWorldIdentifier() {
      return delegate().getWorldIdentifier();
    }

    @Override
    public Vector3 getPosition() {
      return delegate().getPosition();
    }

    @Override
    public EulerDirection getRotation() {
      return delegate().getRotation();
    }

    @Override
    public Type getType() {
      return delegate().getType();
    }

    @Override
    public void setType(Type type) {
      delegate().setType(type);
    }

    @Override
    public Date getCreationDate() {
      return delegate().getCreationDate();
    }

    @Override
    public int getVisits() {
      return delegate().getVisits();
    }

    @Override
    public String getWelcomeMessage() {
      return delegate().getWelcomeMessage();
    }

    @Override
    public void setWelcomeMessage(String welcomeMessage) {
      delegate().setWelcomeMessage(welcomeMessage);
    }

    @Override
    public void setLocation(LocalWorld world, Vector3 position, EulerDirection rotation) {
      delegate().setLocation(world, position, rotation);
    }

    @Override
    protected abstract Warp delegate();

  }
}

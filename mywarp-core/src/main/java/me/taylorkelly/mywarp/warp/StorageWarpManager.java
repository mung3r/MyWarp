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

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.storage.WarpStorage;
import me.taylorkelly.mywarp.teleport.TeleportService.TeleportStatus;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;

/**
 * Stores all warps managed by itusing a {@link WarpStorage}. Calls are all delegated to an underling WarpManager as
 * required by the decorator pattern, storage is implemented on top.
 */
public class StorageWarpManager extends ForwardingWarpManager {

  private final WarpManager warpManager;
  private final WarpStorage connection;

  /**
   * Creates an instance that works on top the given WarpManager.
   *
   * @param warpManager the WarpManager
   * @param connection  the WarpStorage
   */
  public StorageWarpManager(WarpManager warpManager, WarpStorage connection) {
    this.warpManager = warpManager;
    this.connection = connection;
  }

  @Override
  protected WarpManager delegate() {
    return warpManager;
  }

  @Override
  public void add(Warp warp) {
    warp = new PersistentWarp(warp);
    delegate().add(warp);
    connection.addWarp(warp);
  }

  @Override
  public void remove(Warp warp) {
    delegate().remove(warp);
    connection.removeWarp(warp);
  }

  @Override
  public void populate(Iterable<Warp> warps) {
    delegate().populate(Iterables.transform(warps, new Function<Warp, Warp>() {

      @Override
      public PersistentWarp apply(Warp input) {
        return new PersistentWarp(input);
      }
    }));
  }

  /**
   * A Warp that persists its values using a {@link StorageWarpManager}.
   */
  private class PersistentWarp extends ForwardingWarp {

    private final Warp warp;

    /**
     * Creates an instance that works on top of the given warp.
     *
     * @param warp the warp
     */
    private PersistentWarp(Warp warp) {
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
        connection.updateVisits(warp);
      }
      return ret;
    }

    @Override
    public TeleportStatus teleport(LocalPlayer player, FeeProvider.FeeType fee) {
      TeleportStatus ret = super.teleport(player, fee);
      if (ret.isPositionModified()) {
        connection.updateVisits(warp);
      }
      return ret;
    }

    @Override
    public void inviteGroup(String groupId) {
      super.inviteGroup(groupId);
      connection.inviteGroup(warp, groupId);

    }

    @Override
    public void invitePlayer(Profile player) {
      super.invitePlayer(player);
      connection.invitePlayer(warp, player);

    }

    @Override
    public void uninviteGroup(String groupId) {
      super.uninviteGroup(groupId);
      connection.uninviteGroup(warp, groupId);

    }

    @Override
    public void uninvitePlayer(Profile player) {
      super.uninvitePlayer(player);
      connection.uninvitePlayer(warp, player);

    }

    @Override
    public void setCreator(Profile creator) {
      super.setCreator(creator);
      connection.updateCreator(warp);

    }

    @Override
    public void setLocation(LocalWorld world, Vector3 position, EulerDirection rotation) {
      super.setLocation(world, position, rotation);
      connection.updateLocation(warp);

    }

    @Override
    public void setType(Type type) {
      super.setType(type);
      connection.updateType(warp);
    }

    @Override
    public void setWelcomeMessage(String welcomeMessage) {
      super.setWelcomeMessage(welcomeMessage);
      connection.updateWelcomeMessage(warp);
    }
  }
}

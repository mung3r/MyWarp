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

package me.taylorkelly.mywarp.teleport;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Teleporty Entities to Warps.
 */
public interface TeleportService {

  /**
   * Teleports the given {@code entity} to the given {@code warp} and returns the status of the teleport.
   * <p/>
   * Implementations must return a {@link TeleportStatus} that represents the status of the individual teleport.
   *
   * @param entity the entity to teleport
   * @param warp   the warp to teleport to
   * @return The resulting {@code TeleportStatus}
   */
  TeleportStatus teleport(LocalEntity entity, Warp warp);

  /**
   * The status of a finished teleport.
   */
  enum TeleportStatus {
    /**
     * The entity has not been teleported.
     */
    NONE(false),
    /**
     * The entity has been teleported to the desired position.
     */
    ORIGINAL(true),
    /**
     * The entity has been teleported, but the position is not equal to the desired one.
     */
    MODIFIED(true);

    private final boolean positionModified;

    TeleportStatus(boolean positionModified) {
      this.positionModified = positionModified;
    }

    /**
     * Returns whether this status implies that the position has been modified.
     *
     * @return {@code true} if a position change is implied
     */
    public boolean isPositionModified() {
      return positionModified;
    }
  }
}

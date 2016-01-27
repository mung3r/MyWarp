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

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;

/**
 * Teleports an entity after the teleport position has been validated using a previously registered {@link
 * PositionValidationStrategy}.<p>For raw teleports use {@link LocalEntity#teleport(LocalWorld, Vector3,
 * EulerDirection)}.</p>
 */
public final class TeleportService {

  /**
   * The status of a finished teleport.
   */
  public enum TeleportStatus {
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

  private final PositionValidationStrategy strategy;

  /**
   * Creates an instance that uses the given {@code strategy} to validate positions.
   *
   * @param strategy the strategy to use
   */
  public TeleportService(PositionValidationStrategy strategy) {
    this.strategy = strategy;
  }

  /**
   * Teleports the given {@code entity} to the given {@code position} in the given {@code world} and sets its rotation
   * to the given one. <p>Implementations must return a {@link TeleportStatus} that represents the status of the
   * individual teleport.</p>
   *
   * @param entity   the entity to teleport
   * @param world    the world to teleport to
   * @param position the position to teleport to
   * @param rotation the rotation  to teleport to
   * @return The resulting {@code TeleportStatus}
   */
  public TeleportStatus teleport(LocalEntity entity, LocalWorld world, Vector3 position, EulerDirection rotation) {
    // In MyWarp 2.x, the warp height was equivalent with the Y coordinate of the block. If the warp was located on
    // top of a block that was smaller than a full black (e.g. a half step), the height needed to be adjusted or the
    // player would have been teleported inside of the block.
    if (world.getBlock(position).isNotFullHeight()) {
      position = position.add(0, 1, 0);
    }

    Optional<Vector3> strategyPosition = strategy.getValidPosition(position, world);
    if (!strategyPosition.isPresent()) {
      return TeleportStatus.NONE;
    }
    Vector3 finalPosition = strategyPosition.get();
    entity.teleport(world, finalPosition, rotation);

    return position.equals(finalPosition) ? TeleportStatus.ORIGINAL : TeleportStatus.MODIFIED;
  }
}

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

package me.taylorkelly.mywarp.util.teleport;

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.service.teleport.strategy.PositionValidationStrategy;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;

/**
 * Parses teleport positions against a {@link PositionValidationStrategy}. If a valid position exists, the entity is
 * teleported there. If no valid position exists, the teleport is canceled.
 */
public class StrategicTeleportHandler implements TeleportHandler {

  private final PositionValidationStrategy strategy;

  /**
   * Creates an instance that uses the given strategy to validate teleport positions.
   *
   * @param strategy the strategy to use
   */
  public StrategicTeleportHandler(PositionValidationStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity, LocalWorld world, Vector3 position, EulerDirection rotation) {
    Optional<Vector3> optional = strategy.getValidPosition(position, world);

    if (!optional.isPresent()) {
      return TeleportStatus.NONE;
    }

    Vector3 validPosition = optional.get();
    entity.teleport(world, position, rotation);

    if (!validPosition.equals(position)) {
      return TeleportStatus.MODIFIED;
    }
    return TeleportStatus.ORIGINAL;
  }
}

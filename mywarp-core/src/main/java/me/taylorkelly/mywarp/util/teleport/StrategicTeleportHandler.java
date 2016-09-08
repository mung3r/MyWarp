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

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.platform.Settings;
import me.taylorkelly.mywarp.platform.capability.PositionValidationCapability;

/**
 * Parses teleport positions against a {@link PositionValidationCapability}. If a valid position exists, the entity is
 * teleported there. If no valid position exists, the teleport is canceled.
 */
public class StrategicTeleportHandler implements TeleportHandler {

  private final Iterable<PositionValidationCapability> strategies;
  private final Settings settings;


  /**
   * Creates an instance that uses the given strategies to validate teleport positions.
   *
   * <p>The strategies are evaluated in the order of the given elements until either a strategy returns no valid
   * position or all strategies have evaluated the position. If a strategy returns an alternate position, following
   * strategies will check this position opposed to the original one. </p>
   *
   * @param settings   the settings instance to use
   * @param strategies the strategies to use
   */
  public StrategicTeleportHandler(Settings settings, PositionValidationCapability... strategies) {
    this(settings, Lists.newArrayList(strategies));
  }

  /**
   * Creates an instance that uses the given strategies to validate teleport positions.
   *
   * <p>The strategies are evaluated in the order of the elements in the given Iterable until either a strategy returns
   * no valid position or all strategies have evaluated the position. If a strategy returns an alternate position,
   * following strategies will check this position opposed to the original one. </p>
   *
   * @param settings   the settings instance to use
   * @param strategies the strategies to use
   */
  public StrategicTeleportHandler(Settings settings, Iterable<PositionValidationCapability> strategies) {
    this.strategies = strategies;
    this.settings = settings;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity, LocalWorld world, Vector3d position, Vector2f rotation) {
    Optional<Vector3d> optional = getValidPosition(world, position);

    if (!optional.isPresent()) {
      return TeleportStatus.NONE;
    }

    Vector3d validPosition = optional.get();

    if (settings.isShowTeleportEffect()) {
      world.playTeleportEffect(entity.getPosition());
    }
    entity.teleport(world, validPosition, rotation, settings.isTeleportTamedHorses());

    if (!validPosition.equals(position)) {
      return TeleportStatus.MODIFIED;
    }
    return TeleportStatus.ORIGINAL;
  }

  private Optional<Vector3d> getValidPosition(LocalWorld world, Vector3d originalPosition) {
    Optional<Vector3d> ret = Optional.of(originalPosition);
    for (PositionValidationCapability strategy : strategies) {
      if (!ret.isPresent()) {
        return ret;
      }
      ret = strategy.getValidPosition(ret.get(), world);
    }
    return ret;
  }
}

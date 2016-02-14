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

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.teleport.strategy.PositionValidationStrategy;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Teleports an entity after the teleport position has been validated using a previously registered {@link
 * PositionValidationStrategy}.
 */
public final class StrategyTeleportService implements TeleportService {

  private final PositionValidationStrategy strategy;
  private final Game game;

  /**
   * Creates an instance that uses the given {@code strategy} to validate positions.
   *
   * @param strategy the strategy to use
   * @param game     the configured Game
   */
  public StrategyTeleportService(PositionValidationStrategy strategy, Game game) {
    this.strategy = strategy;
    this.game = game;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity, Warp warp) {
    TeleportStatus status = teleport(entity, warp.getWorld(game), warp.getPosition(), warp.getRotation());

    // warp callback!
    warp.visit(entity, status);

    return status;
  }

  private TeleportStatus teleport(LocalEntity entity, LocalWorld world, Vector3 position, EulerDirection rotation) {
    Optional<Vector3> strategyPosition = strategy.getValidPosition(position, world);
    if (!strategyPosition.isPresent()) {
      return TeleportStatus.NONE;
    }
    Vector3 finalPosition = strategyPosition.get();
    entity.teleport(world, finalPosition, rotation);

    return position.equals(finalPosition) ? TeleportStatus.ORIGINAL : TeleportStatus.MODIFIED;
  }
}

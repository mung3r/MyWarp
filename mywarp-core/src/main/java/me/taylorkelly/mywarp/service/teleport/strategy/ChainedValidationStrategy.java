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

package me.taylorkelly.mywarp.service.teleport.strategy;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.util.Vector3;

import java.util.Arrays;
import java.util.List;

/**
 * Chains multiple {@link PositionValidationStrategy}s.
 *
 * <p>Strategies are evaluated in the order they were registered, using the returned value of one strategy for the
 * second etc. If one strategy returns an empty Optional, this Strategy will immediately return an empty Optional.</p>
 */
public class ChainedValidationStrategy implements PositionValidationStrategy {

  private final List<PositionValidationStrategy> strategies;

  /**
   * Creates an instance that evaluates the given strategies in the given order.
   *
   * @param strategies the strategies
   */
  public ChainedValidationStrategy(PositionValidationStrategy... strategies) {
    this(Arrays.asList(strategies));
  }

  /**
   * Creates an instance that evaluates the given strategies in the order they are stored in the given Iterable.
   *
   * @param strategies the strategies
   */
  public ChainedValidationStrategy(Iterable<PositionValidationStrategy> strategies) {
    this.strategies = Lists.newArrayList(strategies);
  }

  @Override
  public Optional<Vector3> getValidPosition(Vector3 originalPosition, LocalWorld world) {
    Optional<Vector3> ret = Optional.of(originalPosition);
    for (PositionValidationStrategy strategy : strategies) {
      if (!ret.isPresent()) {
        return ret;
      }
      ret = strategy.getValidPosition(ret.get(), world);
    }
    return ret;
  }
}

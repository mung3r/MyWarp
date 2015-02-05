/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

package me.taylorkelly.mywarp.limits;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.limits.Limit.Type;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

/**
 * A LimitManager implementation that does absolutely nothing. Limit evaluation will always result in a positive
 * response.
 */
public class DummyLimitManager implements LimitManager {

  private final Game game;
  private final WarpManager manager;

  /**
   * Construct an instance.
   *
   * @param game the Game
   * @param manager the WarpManager this LimitManager is active on
   */
  public DummyLimitManager(Game game, WarpManager manager) {
    this.game = game;
    this.manager = manager;
  }

  @Override
  public EvaluationResult evaluateLimit(LocalPlayer creator, LocalWorld world, Type type, boolean evaluateParents) {
    return EvaluationResult.LIMIT_MEAT;
  }

  @Override
  public Multimap<Limit, Warp> getWarpsPerLimit(LocalPlayer creator) {
    Limit dummyLimit = new Limit() {

      @Override
      public int getLimit(Type type) {
        return -1;
      }

      @Override
      public ImmutableSet<LocalWorld> getAffectedWorlds() {
        return game.getWorlds();
      }

      @Override
      public boolean isAffectedWorld(LocalWorld world) {
        return true;
      }

    };
    return ImmutableMultimap.<Limit, Warp>builder()
        .putAll(dummyLimit, manager.filter(WarpUtils.isCreator(creator.getProfile()))).build();
  }

}

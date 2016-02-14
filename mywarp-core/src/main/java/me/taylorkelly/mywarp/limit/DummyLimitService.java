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

package me.taylorkelly.mywarp.limit;

import com.google.common.collect.ImmutableSet;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.limit.Limit.Type;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A LimitService that does absolutely nothing: <ul> <li>Limit evaluation will always result in a positive
 * response,</li> <li>Sorting warps per player and limit will return all warps created by a player mapped under a dummy
 * limit.</li> </ul>Limit evaluation will always result in a positive response.
 */
public class DummyLimitService implements LimitService {

  private final Game game;
  private final WarpManager warpManager;

  /**
   * Creates an instance.
   *
   * @param game        the Game
   * @param warpManager the WarpManager this LimitService is active on
   */
  public DummyLimitService(Game game, WarpManager warpManager) {
    this.game = game;
    this.warpManager = warpManager;
  }

  @Override
  public EvaluationResult evaluateLimit(LocalPlayer creator, LocalWorld world, Type type, boolean evaluateParents) {
    return EvaluationResult.LIMIT_MEAT;
  }

  @Override
  public Map<Limit, List<Warp>> getWarpsPerLimit(LocalPlayer creator) {
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
      public boolean isAffectedWorld(UUID worldIdentifer) {
        return true;
      }

    };

    Map<Limit, List<Warp>> ret = new HashMap<Limit, List<Warp>>();
    ret.put(dummyLimit, new ArrayList<Warp>(warpManager.filter(WarpUtils.isCreator(creator.getProfile()))));

    return ret;
  }

}

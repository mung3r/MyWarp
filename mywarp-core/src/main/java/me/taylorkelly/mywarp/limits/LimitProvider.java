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

package me.taylorkelly.mywarp.limits;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;

import java.util.List;

/**
 * Provides the actual limits that affect a user under certain conditions. <p>Typically an implementation is provided by
 * the platform running MyWarp.</p>
 */
public interface LimitProvider {

  /**
   * Gets the Limit that affects the given LocalPlayer on the given LocalWorld.
   *
   * @param player the player
   * @param world  the world
   * @return the Limit
   */
  Limit getLimit(LocalPlayer player, LocalWorld world);

  /**
   * Gets all Limits that can possible affect the given LocalPlayer.
   *
   * @param player the player
   * @return a List of Limits that could affect the player
   */
  List<Limit> getEffectiveLimits(LocalPlayer player);
}

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

package me.taylorkelly.mywarp.service.teleport;

import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.util.teleport.TeleportHandler;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Teleports entities to Warps.
 */
public interface TeleportService {

  /**
   * Teleports the given {@code entity} to the given {@code warp} and returns the status of the teleport.
   *
   * @param entity the entity to teleport
   * @param warp   the warp to teleport to
   * @return The resulting {@code TeleportStatus}
   */
  TeleportHandler.TeleportStatus teleport(LocalEntity entity, Warp warp);

}

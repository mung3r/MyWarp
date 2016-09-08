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

package me.taylorkelly.mywarp.warp.event;

import me.taylorkelly.mywarp.warp.Warp;

/**
 * Indicates that something has happen with a Warp.
 */
public abstract class WarpEvent {

  private final Warp warp;

  /**
   * Constructs this event for the given Warp.
   *
   * @param warp the Warp
   */
  WarpEvent(Warp warp) {
    this.warp = warp;
  }

  /**
   * Gets the Warp.
   *
   * @return the Warp
   */
  public Warp getWarp() {
    return warp;
  }

}

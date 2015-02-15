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

package me.taylorkelly.mywarp;

/**
 * Represents a block.
 */
public interface BlockType {

  /**
   * Returns whether this particular block is safe to stand <b>in</b>, which means an entity could stand inside this
   * particular block without taking damage from doing so.
   *
   * @return true if this particular block is safe to stand in
   */
  boolean isSafeToStandIn();

  /**
   * Returns whether this particular block is safe to stand <b>on</b>, which mean an entity could stand on this
   * particular block without taking damage from doing so.
   *
   * @return true if this particular block is safe to stand on
   */
  boolean isSafeToStandOn();

  /**
   * Returns whether this particular Block is smaller than a normal (full) block.
   *
   * @return true if this particular block is smaller than a normal block
   * @deprecated This method exists only to suport warps created in legacy versions.
   */
  @Deprecated
  boolean isNotFullHeight();

}

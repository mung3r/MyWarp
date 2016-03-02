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

package me.taylorkelly.mywarp.platform;

/**
 * A block.
 */
public interface BlockType {

  /**
   * Returns whether a regular entity (without any status effects) can stand <i>within</i> this block without taking any
   * damage from doing so.
   *
   * @return {@code true} if an entity can safely stand within this block
   */
  boolean canEntitySafelyStandWithin();

  /**
   * Returns whether a regular entity (without any status effects) can stand <i>on</i> this block without taking any
   * damage from doing so.
   *
   * @return {@code true} if an entity can safely stand on this block
   */
  boolean canEntitySafelyStandOn();

  /**
   * Returns whether this particular Block is smaller than a normal (full) block.
   *
   * @return {@code true} if this particular block is smaller than a normal block
   * @deprecated This method exists only to support warps created in legacy versions and may be removed at any time.
   */
  @Deprecated
  boolean isNotFullHeight();

}

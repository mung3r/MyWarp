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

package me.taylorkelly.mywarp.warp;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import java.util.Collection;

/**
 * Manages Warps and provides utility methods to get information based on the Warps managed by it.
 */
public interface WarpManager {

  /**
   * Adds the given Warp to this manager.
   *
   * @param warp the Warp
   */
  void add(Warp warp);

  /**
   * Populates this manager with the given Warps. Unlike {@link #add(Warp)} this method must only be used to populate
   * the warp manager with <i>already existing</i> warps.
   *
   * @param warps the Warps
   */
  void populate(Iterable<Warp> warps);

  /**
   * Deletes the given Warp from this manager.
   *
   * @param warp the warp
   */
  void remove(Warp warp);

  /**
   * Clears this manager, removing all Warps previously managed by it. Unlike {@link #remove(Warp)} this method must
   * only be used to <b>clear</b> the warp manager, it does not represent a removal of a warp.
   */
  void clear();

  /**
   * Gets the number of all Warps managed by this manger.
   *
   * @return the total number of Warps on this manager
   */
  int getSize();

  /**
   * Checks whether a Warp with the given name is managed by this manager.
   *
   * @param name the name
   * @return true if this manager contains a warp with this name
   */
  boolean contains(String name);

  /**
   * Gets an Optional containing the Warp of the given name managed by this manager, if such a Warp exists.
   *
   * @param name the exact name
   * @return an Optional containing the Warp with the given name
   */
  Optional<Warp> get(String name);

  /**
   * Gets all Warps on this manager that fulfill the given Predicate. The returned collection is a live view, changes to
   * one affect the other. If a live view is not needed, it may be faster to create a copy of the Warps returned by this
   * method.
   *
   * @param predicate the predicate
   * @return all Warps that fulfill the Predicate
   */
  Collection<Warp> filter(Predicate<Warp> predicate);

}

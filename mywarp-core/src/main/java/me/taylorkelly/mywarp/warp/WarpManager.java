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

package me.taylorkelly.mywarp.warp;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.MatchList;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.util.Collection;

/**
 * Manages Warps and provides utility methods to get certain informations based on the Warps managed by it.
 */
public interface WarpManager {

  /**
   * Adds the given Warp to this manager.
   *
   * @param warp the Warp
   */
  void add(Warp warp);

  /**
   * Deletes the given Warp from this manager.
   *
   * @param warp the warp
   */
  void remove(Warp warp);

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

  /**
   * Gets an Optional containing a warp's creator whose name uniquely matches the given filter within the Warps
   * fulfilling the given Predicate ,if such a creator exists.
   *
   * @param filter    the filter
   * @param predicate the predicate
   * @return an Optional containing the matching creator
   */
  @Deprecated
  Optional<Profile> getMatchingCreator(String filter, Predicate<Warp> predicate);

  /**
   * Gets an Optional containing a warp's world whose name uniquely matches the given filter within the Warps fulfilling
   * the given Predicate, if such a world exists.
   *
   * @param filter    the filter
   * @param predicate the predicate
   * @return an Optional containing the matching world
   */
  @Deprecated
  Optional<LocalWorld> getMatchingWorld(String filter, Predicate<Warp> predicate);

  /**
   * Gets a MatchList containing Warps fulfilling the given predicate.
   *
   * @param filter    the filter
   * @param predicate the predicate
   * @return a MatchList containing the Warps
   */
  MatchList getMatchingWarps(String filter, Predicate<Warp> predicate);

}

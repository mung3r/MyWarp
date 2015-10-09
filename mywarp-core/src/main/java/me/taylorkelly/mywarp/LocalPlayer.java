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

import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.util.UUID;

/**
 * Represents a player.
 */
public interface LocalPlayer extends LocalEntity, Actor {

  /**
   * Gets the name of this player.
   *
   * @return this player's name
   */
  String getName();

  /**
   * Gets the unique ID of this player.
   *
   * @return this player's unique ID
   */
  UUID getUniqueId();

  /**
   * Gets the Profile of this player.
   *
   * @return the Profile
   */
  Profile getProfile();

  /**
   * Sets the compass target of this player.
   *
   * @param world    the world where the new position is in
   * @param position the position of the new target
   */
  void setCompassTarget(LocalWorld world, Vector3 position);

  /**
   * Resets the compass target of this player.
   */
  void resetCompass();

  /**
   * Returns whether this player belongs to the given group.
   *
   * @param groupId the group-identifier
   * @return true if this player belongs to the given group
   */
  boolean hasGroup(String groupId);

  /**
   * Gets the current health of this player.
   *
   * @return the current health
   */
  double getHealth();

}

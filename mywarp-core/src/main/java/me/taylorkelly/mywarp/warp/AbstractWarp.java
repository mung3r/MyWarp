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

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.util.concurrent.TimeUnit;

/**
 * Implements methods that can be resolved using other existing methods.
 */
abstract class AbstractWarp implements Warp {

  private static final double GRAVITY_CONSTANT = 0.8;

  @Override
  public boolean isCreator(LocalPlayer player) {
    return isCreator(player.getProfile());
  }

  @Override
  public boolean isCreator(Profile profile) {
    return getCreator().equals(profile);
  }

  @Override
  public boolean isType(Type type) {
    return getType() == type;
  }

  @Override
  public boolean isPlayerInvited(LocalPlayer player) {
    return isPlayerInvited(player.getProfile());
  }

  @Override
  public boolean isPlayerInvited(Profile profile) {
    return getInvitedPlayers().contains(profile);
  }

  @Override
  public boolean isGroupInvited(String groupId) {
    return getInvitedGroups().contains(groupId);
  }

  @Override
  public int compareTo(Warp that) {
    return getName().compareTo(that.getName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (getName() == null ? 0 : getName().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SimpleWarp other = (SimpleWarp) obj;
    if (getName() == null) {
      if (other.getName() != null) {
        return false;
      }
    } else if (!getName().equals(other.getName())) {
      return false;
    }
    return true;
  }

  @Override
  public double getPopularityScore() {
    // a basic implementation of the hacker news ranking algorithm detailed
    // at http://amix.dk/blog/post/19574: Older warps receive lower scores
    // due to the influence of the gravity constant.
    double daysExisting = (System.currentTimeMillis() - getCreationDate().getTime()) / (1000 * 60 * 60 * 24L);
    return getVisits() / Math.pow(daysExisting, GRAVITY_CONSTANT);
  }

  @Override
  public double getVisitsPerDay() {
    // this method might not be 100% exact (considering leap seconds), but
    // within the current Java API there are no alternatives
    long daysSinceCreation = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - getCreationDate().getTime());
    if (daysSinceCreation <= 0) {
      return getVisits();
    }
    return getVisits() / daysSinceCreation;
  }

  @Override
  public void asCompassTarget(LocalPlayer player) {
    player.setCompassTarget(getWorld(), getPosition());
  }

}

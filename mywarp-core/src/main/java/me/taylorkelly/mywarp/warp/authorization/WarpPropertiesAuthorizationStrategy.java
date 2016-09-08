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

package me.taylorkelly.mywarp.warp.authorization;

import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Resolves a user's authentication for a certain warp based on properties of the warp: <ul> <li>A warp is
 * <i>modifiable</i> if the Actor is the creator,</li> <li>A warp is <i>usable</i> if the entity is the creator, or is
 * an invited player, or the warp is public,</li> <li>A warp is viewable if the Actor is an entity who can use the warp,
 * or the warp is public.</li> </ul>
 */
public class WarpPropertiesAuthorizationStrategy implements AuthorizationStrategy {

  @Override
  public boolean isModifiable(Warp warp, Actor actor) {
    return actor instanceof LocalPlayer && warp.isCreator(((LocalPlayer) actor).getUniqueId());
  }

  @Override
  public boolean isUsable(Warp warp, LocalEntity entity) {
    if (entity instanceof Actor && isModifiable(warp, (Actor) entity)) {
      return true;
    }

    if (entity instanceof LocalPlayer) {
      LocalPlayer player = (LocalPlayer) entity;

      if (warp.isPlayerInvited(player.getUniqueId())) {
        return true;
      }
      for (String groupId : warp.getInvitedGroups()) {
        if (player.hasGroup(groupId)) {
          return true;
        }
      }
    }

    return warp.isType(Warp.Type.PUBLIC);
  }

  @Override
  public boolean isViewable(Warp warp, Actor actor) {
    //for entities a warp is visible only if if they can use it
    if (actor instanceof LocalEntity) {
      return isUsable(warp, (LocalEntity) actor);
    }
    //for everybody else a warp is visible only if it is public
    return warp.isType(Warp.Type.PUBLIC);
  }
}

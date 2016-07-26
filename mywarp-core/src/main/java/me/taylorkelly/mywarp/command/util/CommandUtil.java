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

package me.taylorkelly.mywarp.command.util;

import com.google.common.collect.ImmutableMap;

import me.taylorkelly.mywarp.platform.PlayerNameResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Utilities for writing commands.
 */
public class CommandUtil {

  private CommandUtil() {
  }

  /**
   * Returns the name of the player identified by the given unique identifier or, if the name is not available, the
   * identifier as String.
   *
   * @param uniqueId the player's unique identifer
   * @param resolver the resolver used to resolve the player's name
   * @return a readable name
   */
  public static String toName(UUID uniqueId, PlayerNameResolver resolver) {
    return resolver.getByUniqueId(uniqueId).or(uniqueId.toString());
  }

  /**
   * Returns a alphabetically sorted List with the name of each  player identified by the given unique identifier or, if
   * the name is not available, the identifier as String.
   *
   * @param uniqueIds the unique identifiers
   * @param resolver  the resolver used to resolve the player's name
   * @return a sorted list of readable names
   */
  public static List<String> toName(Iterable<UUID> uniqueIds, PlayerNameResolver resolver) {
    List<String> ret = new ArrayList<String>();

    ImmutableMap<UUID, String> map = resolver.getByUniqueId(uniqueIds);

    for (UUID uniqueId : uniqueIds) {
      @Nullable String name = map.get(uniqueId);
      if (name != null) {
        ret.add(name);
      } else {
        ret.add(uniqueId.toString());
      }
    }
    Collections.sort(ret);
    return ret;
  }
}

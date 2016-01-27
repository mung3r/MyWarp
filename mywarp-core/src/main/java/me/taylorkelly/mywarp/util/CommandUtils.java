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

package me.taylorkelly.mywarp.util;

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;

import java.util.Collection;

/**
 * Utility methods to write commands.
 */
public final class CommandUtils {

  public static final String RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Commands";
  public static final String CONVERSATIONS_RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Conversations";

  /**
   * Block initialization of this class.
   */
  private CommandUtils() {
  }

  /**
   * Joins all warps in the given collection in one string, separated by {@code ", "}.
   *
   * @param warps a collection of warps
   * @return a string with all warp-names or {@code -} if the collection was empty
   */
  public static String joinWarps(Collection<Warp> warps) {
    if (warps.isEmpty()) {
      return "-";
    }

    StrBuilder ret = new StrBuilder();
    for (Warp warp : warps) {
      ret.appendSeparator(", ");
      ret.append(warp.getName());
    }
    return ret.toString();
  }

  /**
   * Joins all worlds in the given collection in one string, separated by {@code ", "}.
   *
   * @param worlds a collection of worlds
   * @return a string with all world-names or {@code -} if the collection was empty
   */
  public static String joinWorlds(Collection<LocalWorld> worlds) {
    if (worlds.isEmpty()) {
      return "-";
    }

    StrBuilder ret = new StrBuilder();
    for (LocalWorld world : worlds) {
      ret.appendSeparator(", ");
      ret.append(world.getName());
    }
    return ret.toString();
  }
}

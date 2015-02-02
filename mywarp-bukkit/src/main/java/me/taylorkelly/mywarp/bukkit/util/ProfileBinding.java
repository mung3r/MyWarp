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

package me.taylorkelly.mywarp.bukkit.util;

import com.google.common.base.Optional;

import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.profile.Profile;

/**
 * A binding for {@link Profile}s.
 */
public class ProfileBinding extends BindingHelper {

  /**
   * Gets a Profile matching the name given by the command.
   *
   * @param context the command's context
   * @return a matching Profile
   * @throws NoSuchProfileException if no matching Profile was found
   * @throws ParameterException     on a parameter error
   */
  @BindingMatch(type = Profile.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1, provideModifiers = false)
  public Profile getString(ArgumentStack context)
      throws NoSuchProfileException, ParameterException {
    String query = context.next();
    Optional<Profile> optional = MyWarp.getInstance().getProfileService().get(query);

    if (!optional.isPresent()) {
      throw new NoSuchProfileException(query);
    }
    return optional.get();
  }

  /**
   * Thrown when no {@link Profile} can be found for a given query. Typically this is caused by a
   * malformed query or unavailable UUID servers.
   */
  public static class NoSuchProfileException extends Exception {

    private static final long serialVersionUID = 5770626202494099277L;

    private final String query;

    /**
     * Creates an instance.
     *
     * @param query the query
     */
    public NoSuchProfileException(String query) {
      this.query = query;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
      return query;
    }
  }

}

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

package me.taylorkelly.mywarp.util;

import com.google.common.base.Predicate;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Utility methods to work with warps.
 */
public final class WarpUtils {

  public static final int MAX_NAME_LENGTH = 32;

  /**
   * Block initialization of this class.
   */
  private WarpUtils() {
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is created by player identified by the
   * given profile.
   *
   * @param profile the Profile
   * @return a predicate that checks if the given warp is created by the given player
   * @see Warp#isCreator(Profile)
   */
  public static Predicate<Warp> isCreator(final Profile profile) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isCreator(profile);
      }

    };
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is modifiable by the given Actor.
   *
   * @param actor the command-sender
   * @return a predicate that checks if the given warp is modifiable by the given Actor
   * @see Warp#isModifiable(Actor)
   */
  public static Predicate<Warp> isModifiable(final Actor actor) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isModifiable(actor);
      }

    };
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is of the given type.
   *
   * @param type the type
   * @return a predicate that checks if the given warp is of the given type
   * @see Warp#isType(Warp.Type)
   */
  public static Predicate<Warp> isType(final Warp.Type type) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isType(type);
      }

    };
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is usable by the given entity.
   *
   * @param entity the entity
   * @return a predicate that checks if the given warp is usable by the given entity
   * @see Warp#isUsable(LocalEntity)
   */
  public static Predicate<Warp> isUsable(final LocalEntity entity) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isUsable(entity);
      }

    };
  }

  /**
   * Returns a predicate that evaluates to {@code true} if the warp being tested is viewable by the given Actor.
   *
   * @param actor the Actor
   * @return a predicate that checks if the given warp is usable by the given Actor
   * @see Warp#isViewable(Actor)
   */
  public static Predicate<Warp> isViewable(final Actor actor) {
    return new Predicate<Warp>() {

      @Override
      public boolean apply(Warp warp) {
        return warp.isViewable(actor);
      }

    };
  }

}

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

package me.taylorkelly.mywarp.command.annotation;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a warp is parsed by name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Name {

  /**
   * The condition the parsed warp must meat.
   */
  Condition value();

  /**
   * The condition a warp must meat.
   */
  enum Condition {
    /**
     * The Warp is viewable.
     *
     * @see AuthorizationService#isViewable(Warp, Actor)
     */
    VIEWABLE(Actor.class),
    /**
     * The Warp is usable.
     *
     * @see AuthorizationService#isUsable(Warp, LocalEntity)
     */
    USABLE(LocalEntity.class),
    /**
     * The Warp is modifiable.
     *
     * @see AuthorizationService#isModifiable(Warp, Actor)
     */
    MODIFIABLE(Actor.class);

    private final Class<?> userClass;

    /**
     * Creates an instance.
     *
     * @param userClass the class of the instance that corresponds with this Condition.
     */
    Condition(Class<?> userClass) {
      this.userClass = userClass;
    }

    public Class<?> getUserClass() {
      return userClass;
    }
  }

}

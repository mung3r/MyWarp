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

import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;

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
   * @return the condition the parsed warp must met
   */
  Condition value();

  /**
   * The condition a warp must met.
   */
  enum Condition {
    /**
     * The Warp is viewable.
     *
     * @see AuthorizationResolver#isViewable(Warp, Actor)
     */
    VIEWABLE(Actor.class),
    /**
     * The Warp is usable.
     *
     * @see AuthorizationResolver#isUsable(Warp, LocalEntity)
     */
    USABLE(LocalEntity.class),
    /**
     * The Warp is modifiable.
     *
     * @see AuthorizationResolver#isModifiable(Warp, Actor)
     */
    MODIFIABLE(Actor.class);

    private final Class<?> userClass;

    Condition(Class<?> userClass) {
      this.userClass = userClass;
    }

    /**
     * To check if a user can potentially have this condition, the user to check must be an instance of the class
     * returned by this method.
     *
     * @return the class a user must be an instance of so that this condition can potentially apply
     */
    public Class<?> getUserClass() {
      return userClass;
    }
  }

}

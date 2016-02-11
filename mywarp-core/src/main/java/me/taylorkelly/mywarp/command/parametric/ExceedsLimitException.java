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

package me.taylorkelly.mywarp.command.parametric;

import me.taylorkelly.mywarp.util.profile.Profile;

/**
 * Indicates that an action exceeds a limit of somebody else than the initiator.
 *
 * @see ExceedsInitiatorLimitException for an Exception thrown when the limit of the initiator is exceeded
 */
public class ExceedsLimitException extends Exception {

  private final Profile subject;

  /**
   * Constructs an instance.
   *
   * @param subject the subject whose limits are or would be exceeded
   */
  public ExceedsLimitException(Profile subject) {
    this.subject = subject;
  }

  /**
   * Gets the subject whose limits are or would be exceeded.
   *
   * @return the subject
   */
  public Profile getSubject() {
    return subject;
  }
}

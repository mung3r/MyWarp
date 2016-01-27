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

package me.taylorkelly.mywarp.bukkit.util.parametric;

import me.taylorkelly.mywarp.limits.Limit;
import me.taylorkelly.mywarp.limits.Limit.Type;

/**
 * Indicates that an action exceeds or would exceed the limit of the initiator himself.
 *
 * @see ExceedsLimitException for an Exception thrown when the limit is unknown by the initiator
 */
public class ExceedsInitiatorLimitException extends Exception {

  private static final long serialVersionUID = 1167613312996698929L;

  private final Limit.Type exceededLimit;
  private final int limitMaximum;

  /**
   * Constructs an instance.
   *
   * @param exceededLimit the exceeded Limit.Type
   * @param limitMaximum  the maximum number of warps a user can create under the exceeded limit
   */
  public ExceedsInitiatorLimitException(Type exceededLimit, int limitMaximum) {
    this.exceededLimit = exceededLimit;
    this.limitMaximum = limitMaximum;
  }

  /**
   * Gets the exceeded Limit.Type.
   *
   * @return the exceeded Limit.Type
   */
  public Limit.Type getExceededLimit() {
    return exceededLimit;
  }

  /**
   * Gets the maximum number of warps a user can create under the exceeded limit.
   *
   * @return the maximum number of warps of the exceeded limit
   */
  public int getLimitMaximum() {
    return limitMaximum;
  }

}

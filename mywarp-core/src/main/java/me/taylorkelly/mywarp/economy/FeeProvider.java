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

package me.taylorkelly.mywarp.economy;

import me.taylorkelly.mywarp.LocalPlayer;

import java.math.BigDecimal;

/**
 * Provides the amount of a fee that affects a user under certain conditions. <p>Typically an implementation is provided
 * by the platform running MyWarp.</p>
 */
public interface FeeProvider {

  /**
   * The different types of fees.
   */
  enum FeeType {
    ASSETS, CREATE, CREATE_PRIVATE, DELETE, GIVE, HELP, INFO, INVITE, LIST, POINT, PRIVATE, PUBLIC, UNINVITE, UPDATE,
    WARP_PLAYER, WARP_SIGN_CREATE, WARP_SIGN_USE, WARP_TO, WELCOME
  }

  /**
   * Gets the amount of the given {@code fee} for the given {@code player}.
   *
   * @param player the player
   * @param fee    the identifier of the actual fee
   * @return the corresponding amount
   */
  BigDecimal getAmount(LocalPlayer player, FeeType fee);

}

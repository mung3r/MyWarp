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

package me.taylorkelly.mywarp.economy;

import me.taylorkelly.mywarp.LocalPlayer;

import java.math.BigDecimal;

/**
 * Provides raw access to an economy system. <p>Typically an implementation is provided by the platform running
 * MyWarp.</p>
 */
public interface EconomyProvider {

  /**
   * Returns whether the given {@code player} has at least the given {@code amount}.
   *
   * @param player the player to check
   * @param amount the amount to check
   * @return {@code true} if the player has at least the given amount
   * @throws IllegalArgumentException if the given {@code amount} is not greater than 0
   */
  boolean hasAtLeast(LocalPlayer player, BigDecimal amount);

  /**
   * Withdraws the given {@code player} with the given {@code amount}.
   *
   * @param player the player to check
   * @param amount the amount to withdraw
   * @throws IllegalArgumentException if the given {@code amount} is not greater than 0
   */
  void withdraw(LocalPlayer player, BigDecimal amount);
}

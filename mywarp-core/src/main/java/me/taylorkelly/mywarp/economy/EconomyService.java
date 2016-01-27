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
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;

import java.math.BigDecimal;

/**
 * Provides managed access to an economy system. Implementations may call additional validation before, or call
 * additional callback after a transactions is executed. <p>For a raw access use an {@link EconomyProvider}.</p>
 */
public interface EconomyService {

  /**
   * Returns whether the given {@code player} has at least the amount identified by the given {@code fee}.
   *
   * @param player the player
   * @param fee    the identifier of the actual fee
   * @return {@code true} if the player has at least the given amount
   */
  boolean hasAtLeast(LocalPlayer player, FeeType fee);

  /**
   * Returns whether the given {@code player} has at least the given amount.
   *
   * @param player the player
   * @param amount the amount
   * @return {@code true} if the player has at least the given amount
   * @throws IllegalArgumentException if the given {@code amount} is not greater than 0
   */
  boolean hasAtLeast(LocalPlayer player, BigDecimal amount);

  /**
   * Withdraws the given {@code player} with the amount identified by the given {@code fee}.
   *
   * @param player the player
   * @param fee    the identifier of the actual fee
   */
  void withdraw(LocalPlayer player, FeeType fee);

  /**
   * Withdraws the given {@code player} with the given amount.
   *
   * @param player the player
   * @param amount the amount
   * @throws IllegalArgumentException if the given {@code amount} is not greater than 0
   */
  void withdraw(LocalPlayer player, BigDecimal amount);

}

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
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;

import java.math.BigDecimal;

/**
 * An EconomyManager implementation that does absolutely nothing. Methods that require a return value return a
 * positive one.
 */
public class DummyEconomyManager implements EconomyManager {

  @Override
  public boolean hasAtLeast(LocalPlayer player, FeeType fee) {
    return true;
  }

  @Override
  public boolean hasAtLeast(LocalPlayer player, BigDecimal amount) {
    return true;
  }

  @Override
  public void withdraw(LocalPlayer player, FeeType fee) {
  }

  @Override
  public void withdraw(LocalPlayer player, BigDecimal amount) {
  }

}

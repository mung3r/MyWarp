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

import static com.google.common.base.Preconditions.checkArgument;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;

import java.math.BigDecimal;

/**
 * Informs users after a transaction about the result. <p>This implementation operates on a {@link FeeProvider} that
 * provides the amount of the fees and a {@link EconomyProvider} that provides a connection with the actual
 * economy.</p>
 */
public class InformativeEconomyService implements EconomyService {

  private static final DynamicMessages MESSAGES = new DynamicMessages("me.taylorkelly.mywarp.lang.Economy");

  private final Settings settings;
  private final FeeProvider provider;
  private final EconomyProvider service;

  /**
   * Creates an instance.
   *
   * @param settings the {@code Settings} to use
   * @param provider the {@code FeeProvider} to use
   * @param service  the {@code EconomyProvider} to use
   */
  public InformativeEconomyService(Settings settings, FeeProvider provider, EconomyProvider service) {
    this.settings = settings;
    this.provider = provider;
    this.service = service;
  }

  /**
   * Returns whether the given {@code player} can disobey fees.
   *
   * @param player the player
   * @return {@code true} if the player can disobey fees
   */
  private boolean canDisobeyFees(LocalPlayer player) {
    return player.hasPermission("mywarp.economy.disobey");
  }

  @Override
  public boolean hasAtLeast(LocalPlayer player, FeeType fee) {
    BigDecimal amount = provider.getAmount(player, fee);
    if (amount.signum() != 1) {
      return true;
    }
    return hasAtLeast(player, amount);
  }

  @Override
  public boolean hasAtLeast(LocalPlayer player, BigDecimal amount) {
    checkArgument(amount.signum() == 1, "The amount must be greater than 0.");
    if (canDisobeyFees(player)) {
      return true;
    }
    if (service.hasAtLeast(player, amount)) {
      return true;
    }
    player.sendError(MESSAGES.getString("cannot-afford", amount));
    return false;
  }

  @Override
  public void withdraw(LocalPlayer player, FeeType fee) {
    BigDecimal amount = provider.getAmount(player, fee);
    if (amount.signum() != 1) {
      return;
    }
    withdraw(player, amount);
  }

  @Override
  public void withdraw(LocalPlayer player, BigDecimal amount) {
    checkArgument(amount.signum() == 1, "The amount must be greater than 0.");
    if (canDisobeyFees(player)) {
      return;
    }
    service.withdraw(player, amount);
    if (settings.isEconomyInformAfterTransaction()) {
      // TODO color in aqua
      player.sendMessage(MESSAGES.getString("transaction-complete", amount));
    }
  }

}

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

package me.taylorkelly.mywarp.bukkit;

import static com.google.common.base.Preconditions.checkArgument;

import me.taylorkelly.mywarp.bukkit.settings.BukkitSettings;
import me.taylorkelly.mywarp.bukkit.settings.FeeBundle;
import me.taylorkelly.mywarp.bukkit.util.permission.BundleProvider;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.capability.EconomyCapability;
import me.taylorkelly.mywarp.service.economy.FeeType;
import me.taylorkelly.mywarp.util.MyWarpLogger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.slf4j.Logger;

import java.math.BigDecimal;

/**
 * Economic compatibility for the Bukkit platform. Hooks into <a href="https://github.com/MilkBowl/VaultAPI">Vault</a>
 * to handle the economical tasks.
 */
public class BukkitEconomyCapability implements EconomyCapability {

  private static final Logger log = MyWarpLogger.getLogger(BukkitEconomyCapability.class);

  private final Economy vaultApi;
  private final BundleProvider<FeeBundle> feeProvider;
  private final BukkitSettings settings;

  BukkitEconomyCapability(Economy vaultApi, BundleProvider<FeeBundle> feeProvider, BukkitSettings settings) {
    this.vaultApi = vaultApi;
    this.feeProvider = feeProvider;
    this.settings = settings;
  }

  @Override
  public BigDecimal getFee(LocalPlayer player, FeeType feeType) {
    return feeProvider.getBundle(player).get(feeType);
  }

  @Override
  public boolean hasAtLeast(LocalPlayer player, BigDecimal amount) {
    checkArgument(amount.signum() == 1, "amount must be positive");

    return vaultApi.has(BukkitAdapter.adapt(player), amount.doubleValue());
  }

  @Override
  public BigDecimal withdraw(LocalPlayer player, BigDecimal amount) {
    checkArgument(amount.signum() == 1, "amount must be positive");

    EconomyResponse response = vaultApi.withdrawPlayer(BukkitAdapter.adapt(player), amount.doubleValue());

    if (!response.transactionSuccess()) {
      log.error("Could not withdraw {}: {}", player.getName(), response.errorMessage);
    }
    return BigDecimal.valueOf(response.amount);
  }

  @Override
  public boolean informAfterTransaction() {
    return settings.isEconomyInformAfterTransaction();
  }
}

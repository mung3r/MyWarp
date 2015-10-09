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

package me.taylorkelly.mywarp.bukkit.economy;

import static com.google.common.base.Preconditions.checkArgument;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.BukkitAdapter;
import me.taylorkelly.mywarp.economy.EconomyProvider;
import me.taylorkelly.mywarp.util.MyWarpLogger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.slf4j.Logger;

import java.math.BigDecimal;

/**
 * An EconomyProvider that uses Vault to resolve requests.
 */
public class VaultProvider implements EconomyProvider {

  private static final Logger log = MyWarpLogger.getLogger(VaultProvider.class);

  private final Economy economy;
  private final BukkitAdapter adapter;

  /**
   * Creates an instance that uses the given {@code economyProvider} to connect with Vault.
   *
   * @param economyProvider the economy service provider from Vault
   * @param adapter         the adapter
   */
  public VaultProvider(RegisteredServiceProvider<Economy> economyProvider, BukkitAdapter adapter) {
    this.economy = economyProvider.getProvider();
    this.adapter = adapter;
  }

  @Override
  public boolean hasAtLeast(LocalPlayer player, BigDecimal amount) {
    checkArgument(amount.signum() == 1, "The amount must be greater than 0.");
    Player bukkitPlayer = adapter.adapt(player);
    return economy.has(bukkitPlayer, bukkitPlayer.getWorld().getName(), amount.doubleValue());
  }

  @Override
  public void withdraw(LocalPlayer player, BigDecimal amount) {
    checkArgument(amount.signum() == 1, "The amount must be greater than 0.");
    Player bukkitPlayer = adapter.adapt(player);
    EconomyResponse
        response =
        economy.withdrawPlayer(bukkitPlayer, bukkitPlayer.getWorld().getName(), amount.doubleValue());

    if (!response.transactionSuccess()) {
      log.error("Could not withdraw {}: {}", bukkitPlayer.getName(), response.errorMessage);
    }
  }
}

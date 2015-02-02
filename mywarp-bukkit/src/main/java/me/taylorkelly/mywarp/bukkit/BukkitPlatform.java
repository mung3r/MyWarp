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

package me.taylorkelly.mywarp.bukkit;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.Platform;
import me.taylorkelly.mywarp.bukkit.economy.BukkitFeeProvider;
import me.taylorkelly.mywarp.bukkit.economy.VaultService;
import me.taylorkelly.mywarp.bukkit.limits.BukkitLimitProvider;
import me.taylorkelly.mywarp.bukkit.timer.BukkitDurationProvider;
import me.taylorkelly.mywarp.bukkit.timer.BukkitTimerService;
import me.taylorkelly.mywarp.economy.EconomyService;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.limits.LimitProvider;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.util.profile.ProfileService;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.ResourceBundle.Control;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * The Platform implementation when running on Bukkit.
 */
public class BukkitPlatform implements Platform {

  private final MyWarpPlugin plugin;
  @Nullable
  private DurationProvider durationProvider;
  @Nullable
  private TimerService timerService;

  /**
   * Creates an instance.
   *
   * @param plugin the plugin
   */
  public BukkitPlatform(MyWarpPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void reload() {
    plugin.reload();
  }

  @Override
  public File getDataFolder() {
    return plugin.getDataFolder();
  }

  @Override
  public Control getResourceBundleControl() {
    return plugin.getResourceBundleControl();
  }

  @Override
  public BukkitSettings getSettings() {
    return plugin.getSettings();
  }

  @Override
  public ProfileService getProfileService() {
    return plugin.getProfileService();
  }

  @Override
  public EconomyService getEconomyService() {
    try {
      RegisteredServiceProvider<Economy>
          economyProvider =
          Bukkit.getServicesManager().getRegistration(
              net.milkbowl.vault.economy.Economy.class);
      if (economyProvider == null) {
        plugin.getLogger()
            .severe(
                "Failed to hook into Vault (EconomyProvider is null). EconomySupport will not be avilable."); // NON-NLS
      } else {
        return new VaultService(economyProvider, plugin.getAdapter());
      }
    } catch (NoClassDefFoundError e) {
      plugin.getLogger()
          .severe(
              "Failed to hook into Vault (EconomyProviderClass not available). EconomySupport will not be avilable."); // NON-NLS
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public FeeProvider getFeeProvider() {
    return new BukkitFeeProvider(getSettings().getEconomyConfiguredFeeBundles(), getSettings()
        .getEconomyDefaultFeeBundle());
  }

  @Override
  public LimitProvider getLimitProvider() {
    return new BukkitLimitProvider(getSettings().getLimitsConfiguredLimitBundles(), getSettings()
        .getLimitsDefaultLimitBundle());
  }

  @Override
  public DurationProvider getDurationProvider() {
    if (durationProvider == null) {
      durationProvider =
          new BukkitDurationProvider(getSettings().getTimersConfiguredDurationBundles(),
                                     getSettings().getTimersDefaultDurationBundle());
    }
    return durationProvider;
  }

  @Override
  public TimerService getTimerService() {
    if (timerService == null) {
      timerService = new BukkitTimerService(plugin);
    }
    return timerService;
  }

  @Override
  public Optional<LocalWorld> getLoadedWorld(String worldName) {
    World world = Bukkit.getWorld(worldName);
    if (world != null) {
      return Optional.of(plugin.getAdapter().adapt(world));
    }
    return Optional.absent();
  }

  @Override
  public Optional<LocalWorld> getLoadedWorld(UUID uniqueId) {
    World world = Bukkit.getWorld(uniqueId);
    if (world != null) {
      return Optional.of(plugin.getAdapter().adapt(world));
    }
    return Optional.absent();
  }

  @Override
  public Optional<LocalPlayer> getOnlinePlayer(String name) {
    @SuppressWarnings("deprecation")
    Player player = Bukkit.getPlayer(name);
    if (player != null) {
      return Optional.of(plugin.getAdapter().adapt(player));
    }
    return Optional.absent();
  }

  @Override
  public Optional<LocalPlayer> getOnlinePlayer(UUID identifier) {
    Player player = Bukkit.getPlayer(identifier);
    if (player != null) {
      return Optional.of(plugin.getAdapter().adapt(player));
    }
    return Optional.absent();
  }

  @Override
  public ImmutableSet<LocalWorld> getLoadedWorlds() {
    ImmutableSet.Builder<LocalWorld> builder = ImmutableSet.builder();

    for (World world : Bukkit.getWorlds()) {
      builder.add(plugin.getAdapter().adapt(world));
    }
    return builder.build();
  }

  @Override
  public Executor getServerExecutor() {
    return new BukkitExecutor(plugin);
  }

}

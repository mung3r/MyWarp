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

package me.taylorkelly.mywarp;

import me.taylorkelly.mywarp.economy.EconomyProvider;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.limit.LimitProvider;
import me.taylorkelly.mywarp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.storage.RelationalDataService;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.util.profile.ProfileService;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Represents a platform MyWarp has been adapted to run on. <p>Typically an implementation is provided by the platform
 * running MyWarp.</p>
 */
public interface Platform {

  /**
   * Reloads this Platform. Calling this method will unload and reload all platform specific behaviors, as far as
   * supported by the Platform.
   */
  void reload();

  /**
   * Gets the data-folder when running in this Platform. The folder is expected to exist and be read- and writable for
   * MyWarp.
   *
   * @return the data-folder
   */
  File getDataFolder();

  /**
   * Gets the {@link Settings} as implemented by this Platform.
   *
   * @return the {@code Settings}
   */
  Settings getSettings();

  /**
   * Gets the {@link java.util.ResourceBundle.Control} as implemented by this Platform.
   *
   * @return the {@code ResourceBundle.Control}
   */
  ResourceBundle.Control getResourceBundleControl();

  /**
   * Gets the {@link Game} as implemented by this Platform.
   *
   * @return the {@code Game}
   */
  Game getGame();

  /**
   * Creates a {@link RelationalDataService} as described by the given {@code config}.
   *
   * @param configuration the configuration
   * @return the {@code RelationalDataService}
   */
  RelationalDataService createDataService(ConnectionConfiguration configuration);

  /**
   * Gets the {@link ProfileService} as implemented by this Platform.
   *
   * @return the {@code ProfileService}
   */
  ProfileService getProfileService();

  /**
   * Gets the {@link EconomyProvider} as implemented by this Platform.
   *
   * @return the {@code EconomyProvider}
   * @throws java.lang.UnsupportedOperationException if the Platform has no support for an economy
   */
  EconomyProvider getEconomyService();

  /**
   * Gets the {@link TimerService} as implemented by this Platform.
   *
   * @return the {@code TimerService}
   */
  TimerService getTimerService();

  /**
   * Gets the {@link FeeProvider} as implemented by this Platform.
   *
   * @return the {@code FeeProvider}
   */
  FeeProvider getFeeProvider();

  /**
   * Gets the {@link LimitProvider} as implemented by this Platform.
   *
   * @return the {@code LimitProvider}
   */
  LimitProvider getLimitProvider();

  /**
   * Gets the {@link DurationProvider} as implemented by this Platform.
   *
   * @return the {@code DurationProvider}
   */
  DurationProvider getDurationProvider();

}

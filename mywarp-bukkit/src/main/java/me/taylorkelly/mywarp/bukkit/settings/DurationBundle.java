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

package me.taylorkelly.mywarp.bukkit.settings;

import com.google.common.collect.ImmutableMap;

import me.taylorkelly.mywarp.bukkit.util.permission.ValueBundle;
import me.taylorkelly.mywarp.service.teleport.timer.Duration;
import me.taylorkelly.mywarp.service.teleport.timer.TimerAction;
import me.taylorkelly.mywarp.service.teleport.timer.WarpCooldown;
import me.taylorkelly.mywarp.service.teleport.timer.WarpWarmup;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A ValueBundle that bundles Durations.
 */
public class DurationBundle extends ValueBundle {

  private final Map<Class<? extends TimerAction<?>>, Duration> durations;

  DurationBundle(String identifier, ConfigurationSection values) {
    this(identifier, new Duration(values.getLong("warpCooldown", 0), TimeUnit.SECONDS),
         new Duration(values.getLong("warpWarmup", 0), TimeUnit.SECONDS));
  }

  /**
   * Initializes this bundle.
   *
   * @param identifier   the unique identifier
   * @param warpCooldown the cooldown for the {@code warp} command
   * @param warpWarmup   the warmup for the {@code warp} command
   */
  DurationBundle(String identifier, Duration warpCooldown, Duration warpWarmup) {
    super(identifier, "mywarp.timer");

    durations =
        ImmutableMap.<Class<? extends TimerAction<?>>, Duration>builder().put(WarpCooldown.class, warpCooldown)
            .put(WarpWarmup.class, warpWarmup).build();
  }

  /**
   * Gets the Duration referenced by the given type.
   *
   * @param clazz the TimerAction class the Duration is requested for
   * @return the referenced duration
   */
  public Duration get(Class<? extends TimerAction<?>> clazz) {
    return durations.get(clazz);
  }

}

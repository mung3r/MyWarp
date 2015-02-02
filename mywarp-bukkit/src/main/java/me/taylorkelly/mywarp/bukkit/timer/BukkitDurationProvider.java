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

package me.taylorkelly.mywarp.bukkit.timer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.permissions.bundles.ValueBundle;
import me.taylorkelly.mywarp.timer.Duration;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerAction;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Map;
import java.util.SortedSet;

/**
 * Provides Durations when running on Bukkit. The actual Durations are stored in {@link
 * DurationBundle}s managed by this provider. <p> Players either need to have a specific permission
 * of a certain bundle or they fall under a default bundle. If a player has the permission for more
 * than one bundle, the alphabetically first bundle will be used. </p>
 */
public class BukkitDurationProvider implements DurationProvider {

  private SortedSet<DurationBundle> configuredDurations;
  private DurationBundle defaultDurations;

  /**
   * Initializes this provider.
   *
   * @param configuredDurations the configured FeeBundles that are assigned to a player via a
   *                            specific permission
   * @param defaultDurations    the default FeeBundle that acts as a fallback if a player has none
   *                            of the specific permissions
   */
  public BukkitDurationProvider(Iterable<DurationBundle> configuredDurations,
                                DurationBundle defaultDurations) {
    this.configuredDurations = ImmutableSortedSet.copyOf(configuredDurations);
    this.defaultDurations = defaultDurations;

    for (ValueBundle bundle : configuredDurations) {
      BukkitPermissionsRegistration.INSTANCE.register(new Permission(bundle.getPermission(),
                                                                     PermissionDefault.FALSE));
    }
  }

  @Override
  public Duration getDuration(LocalPlayer player, Class<? extends TimerAction<?>> clazz) {
    return getDurationBundle(player).get(clazz);
  }

  /**
   * Gets the appropriate DurationBundle for the given player.
   *
   * @param player the player
   * @return the appropriate DurationBundle
   */
  private DurationBundle getDurationBundle(LocalPlayer player) {
    for (DurationBundle bundle : configuredDurations) {
      if (!player.hasPermission(bundle.getPermission())) {
        continue;
      }
      return bundle;
    }
    return defaultDurations;
  }

  /**
   * A ValueBundle that bundles Durations.
   */
  public static class DurationBundle extends ValueBundle {

    private final Map<Class<? extends TimerAction<?>>, Duration> durations;

    /**
     * Initializes this bundle.
     *
     * @param identifier   the unique identifier
     * @param warpCooldown the cooldown for the {@code warp} command
     * @param warpWarmup   the warmup for the {@code warp} command
     */
    public DurationBundle(String identifier, Duration warpCooldown, Duration warpWarmup) {
      super(identifier, "mywarp.timer"); // NON-NLS

      durations = ImmutableMap.<Class<? extends TimerAction<?>>, Duration>builder()
          .put(WarpCooldown.class, warpCooldown).put(WarpWarmup.class, warpWarmup).build();
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

}

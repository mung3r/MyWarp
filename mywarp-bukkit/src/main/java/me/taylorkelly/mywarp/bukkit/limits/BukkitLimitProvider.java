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

package me.taylorkelly.mywarp.bukkit.limits;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.bukkit.permissions.BukkitPermissionsRegistration;
import me.taylorkelly.mywarp.bukkit.permissions.bundles.ValueBundle;
import me.taylorkelly.mywarp.limits.Limit;
import me.taylorkelly.mywarp.limits.LimitProvider;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Provides Limits when running on Bukkit. The actual Limits are stored in {@link LimitBundle}s managed by this
 * provider. <p> Players either need to have a specific permission of a certain bundle that covers the world in question
 * or they fall under a global default bundle. If a player has the permission for more than one bundle, the
 * alphabetically first bundle will be used. </p>
 */
public class BukkitLimitProvider implements LimitProvider {

  private SortedSet<LimitBundle> configuredLimits;
  private LimitBundle defaultLimit;

  /**
   * Initializes this provider.
   *
   * @param configuredLimits the configured FeeBundles that are assigned to a player via a specific permission
   * @param defaultLimit     the default FeeBundle that acts as a fallback if a player has none of the specific
   *                         permissions
   */
  public BukkitLimitProvider(Iterable<LimitBundle> configuredLimits, LimitBundle defaultLimit) {
    this.configuredLimits = ImmutableSortedSet.copyOf(configuredLimits);
    this.defaultLimit = defaultLimit;

    for (ValueBundle bundle : configuredLimits) {
      BukkitPermissionsRegistration.INSTANCE.register(new Permission(bundle.getPermission(), PermissionDefault.FALSE));
    }
    // register per-world overrides
    for (World world : Bukkit.getWorlds()) {
      String perm = "mywarp.limit.disobey." + world.getName();
      // mywarp.limit.disobey.[WORLDNAME].total
      Permission totalPerm = new Permission(perm + ".total");
      totalPerm.addParent("mywarp.limit.disobey.total", true);
      totalPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
      BukkitPermissionsRegistration.INSTANCE.register(totalPerm);
      // mywarp.limit.disobey.[WORLDNAME].private
      Permission privatePerm = new Permission(perm + ".private");
      privatePerm.addParent("mywarp.limit.disobey.private", true);
      privatePerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
      BukkitPermissionsRegistration.INSTANCE.register(privatePerm);
      // mywarp.limit.disobey.[WORLDNAME].public
      Permission publicPerm = new Permission(perm + ".public");
      publicPerm.addParent("mywarp.limit.disobey.public", true);
      publicPerm.addParent("mywarp.limit.disobey." + world.getName() + ".*", true);
      BukkitPermissionsRegistration.INSTANCE.register(publicPerm);
    }
  }

  @Override
  public Limit getLimit(LocalPlayer player, LocalWorld world) {
    for (LimitBundle bundle : configuredLimits) {
      if (!player.hasPermission(bundle.getPermission())) {
        continue;
      }
      if (!bundle.isAffectedWorld(world)) {
        continue;
      }
      return bundle;
    }
    return defaultLimit;
  }

  @Override
  public List<Limit> getEffectiveLimits(LocalPlayer player) {
    Builder<Limit> ret = ImmutableList.builder();
    Set<LocalWorld> worlds = new HashSet<LocalWorld>();
    for (LimitBundle bundle : configuredLimits) {
      if (!player.hasPermission(bundle.getPermission())) {
        continue;
      }
      if (worlds.containsAll(bundle.getAffectedWorlds())) {
        // the affective bundles already cover all worlds that this
        // bundle covers, so it is effectively overwritten.
        continue;
      }
      ret.add(bundle);
      worlds.addAll(bundle.getAffectedWorlds());
    }
    // if there is a world that is not covered by all bundles, the default
    // bundle (always global) is needed
    if (!worlds.containsAll(defaultLimit.getAffectedWorlds())) {
      ret.add(defaultLimit);
    }
    return ret.build();
  }

}

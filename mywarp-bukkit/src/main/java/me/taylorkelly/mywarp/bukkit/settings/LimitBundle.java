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

import com.google.common.collect.ImmutableSet;

import me.taylorkelly.mywarp.bukkit.BukkitAdapter;
import me.taylorkelly.mywarp.bukkit.util.permission.ValueBundle;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.service.limit.Limit;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * A ValueBundle that bundles limit.
 */
public class LimitBundle extends ValueBundle implements Limit {

  private final Map<Type, Integer> limitMap = new EnumMap<Limit.Type, Integer>(Type.class);
  @Nullable
  private final ImmutableSet<LocalWorld> affectedWorlds;

  LimitBundle(String identifier, ConfigurationSection values) {
    this(identifier, values.getInt("totalLimit"), values.getInt("publicLimit"), values.getInt("privateLimit"),
         createWorldList(values));
  }

  @Nullable
  private static List<LocalWorld> createWorldList(ConfigurationSection values) {
    List<LocalWorld> worlds = null;

    if (values.contains("affectedWorlds")) {
      worlds = new ArrayList<LocalWorld>();
      for (String name : values.getStringList("affectedWorlds")) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
          //log.warn("The world name '{}' configured for the limit '{}' does not match any existing world and will be
          // " + "ignored.", name, identifier);
          continue;
        }
        worlds.add(BukkitAdapter.adapt(world));
      }
    }
    return worlds;
  }

  /**
   * Initializes this bundle as global.
   *
   * @param identifier   the unique identifier
   * @param totalLimit   the total limit
   * @param publicLimit  the public limit
   * @param privateLimit the private limit
   */
  LimitBundle(String identifier, int totalLimit, int publicLimit, int privateLimit) {
    this(identifier, totalLimit, publicLimit, privateLimit, null);
  }

  /**
   * Initializes this bundle.
   *
   * @param identifier     the unique identifier
   * @param totalLimit     the total limit
   * @param publicLimit    the public limit
   * @param privateLimit   the private limit
   * @param affectedWorlds an Iterable of worlds affected by this limit. Can be {@code null} if this limit should affect
   *                       all worlds.
   */
  LimitBundle(String identifier, int totalLimit, int publicLimit, int privateLimit,
              @Nullable Iterable<LocalWorld> affectedWorlds) {
    super(identifier, "mywarp.limit");

    if (affectedWorlds != null) {
      this.affectedWorlds = ImmutableSet.copyOf(affectedWorlds);
    } else {
      this.affectedWorlds = null;
    }
    limitMap.put(Type.TOTAL, totalLimit);
    limitMap.put(Type.PUBLIC, publicLimit);
    limitMap.put(Type.PRIVATE, privateLimit);
  }

  @Override
  public int getLimit(Type type) {
    return limitMap.get(type);
  }

  @Override
  public ImmutableSet<LocalWorld> getAffectedWorlds() {
    ImmutableSet<LocalWorld> ret = affectedWorlds;
    if (ret == null) {
      // bundle is global
      ImmutableSet.Builder<LocalWorld> builder = ImmutableSet.builder();
      for (World bukkitWorld : Bukkit.getWorlds()) {
        builder.add(BukkitAdapter.adapt(bukkitWorld));
      }
      ret = builder.build();
    }
    return ret;
  }

  @Override
  public boolean isAffectedWorld(UUID worldIdentifier) {
    if (affectedWorlds == null) {
      return true;
    }
    for (LocalWorld world : affectedWorlds) {
      if (world.getUniqueId().equals(worldIdentifier)) {
        return true;
      }
    }
    return false;
  }

}

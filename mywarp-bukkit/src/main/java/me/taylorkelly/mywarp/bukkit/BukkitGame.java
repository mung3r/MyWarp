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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * The game as implemented by Bukkit.
 */
public class BukkitGame implements Game {

  private final BukkitExecutor executor;
  private final BukkitAdapter adapter;

  /**
   * Creates the instance.
   *
   * @param executor the executor for Bukkit
   * @param adapter  the adapter for Bukkit
   */
  public BukkitGame(BukkitExecutor executor, BukkitAdapter adapter) {
    this.executor = executor;
    this.adapter = adapter;
  }

  @Override
  public Executor getExecutor() {
    return executor;
  }

  @Override
  public Optional<LocalWorld> getWorld(String worldName) {
    World world = Bukkit.getWorld(worldName);
    if (world != null) {
      return Optional.of(adapter.adapt(world));
    }
    return Optional.absent();
  }

  @Override
  public Optional<LocalWorld> getWorld(UUID uniqueId) {
    World world = Bukkit.getWorld(uniqueId);
    if (world != null) {
      return Optional.of(adapter.adapt(world));
    }
    return Optional.absent();
  }

  @Override
  public Optional<LocalPlayer> getPlayer(String name) {
    @SuppressWarnings("deprecation") Player player = Bukkit.getPlayer(name);
    if (player != null) {
      return Optional.of(adapter.adapt(player));
    }
    return Optional.absent();
  }

  @Override
  public Optional<LocalPlayer> getPlayer(UUID identifier) {
    Player player = Bukkit.getPlayer(identifier);
    if (player != null) {
      return Optional.of(adapter.adapt(player));
    }
    return Optional.absent();
  }

  @Override
  public ImmutableSet<LocalWorld> getWorlds() {
    ImmutableSet.Builder<LocalWorld> builder = ImmutableSet.builder();

    for (World world : Bukkit.getWorlds()) {
      builder.add(adapter.adapt(world));
    }
    return builder.build();
  }
}

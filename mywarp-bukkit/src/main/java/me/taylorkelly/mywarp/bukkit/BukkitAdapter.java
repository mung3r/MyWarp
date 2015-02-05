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

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.bukkit.permissions.GroupResolver;
import me.taylorkelly.mywarp.util.profile.ProfileService;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Adapts between equivalent local and Bukkit objects.
 */
public final class BukkitAdapter {

  private final GroupResolver groupResolver;
  private final ProfileService profileService;
  private final Settings settings;


  /**
   * Creates an instance.
   *
   * @param groupResolver  the GroupResolver
   * @param profileService the ProfileService
   * @param settings the Settings
   */
  public BukkitAdapter(GroupResolver groupResolver, ProfileService profileService, Settings settings) {
    this.profileService = profileService;
    this.groupResolver = groupResolver;
    this.settings = settings;
  }

  /**
   * Adapts between a LocalWorld and a World.
   *
   * @param world the LocalWorld
   * @return the World representing the given LocalWorld
   */
  public World adapt(LocalWorld world) {
    if (world instanceof BukkitWorld) {
      return ((BukkitWorld) world).getLoadedWorld();
    }
    World loadedWorld = Bukkit.getWorld(world.getName());
    if (loadedWorld == null) {
      throw new IllegalArgumentException("Cannot find a loaded world for " + world + "in Bukkit.");
    }
    return loadedWorld;
  }

  /**
   * Adapts between a World and a LocalWorld.
   *
   * @param world the World
   * @return the LocalWorld representing the given World
   */
  public LocalWorld adapt(World world) {
    return new BukkitWorld(world);
  }

  /**
   * Adapts between a LocalPlayer and a Player.
   *
   * @param player the LocalPlayer
   * @return the Player representing the given LocalPlayer
   */
  public Player adapt(LocalPlayer player) {
    if (player instanceof BukkitPlayer) {
      return ((BukkitPlayer) player).getLoadedPlayer();
    }
    Player loadedPlayer = Bukkit.getPlayer(player.getProfile().getUniqueId());
    if (loadedPlayer == null) {
      throw new IllegalArgumentException("Cannot find a loaded player for " + player + "in Bukkit.");
    }
    return loadedPlayer;
  }

  /**
   * Adapts between a Player and a LocalPlayer.
   *
   * @param player the Player
   * @return the LocalPlayer representing the given Player
   */
  public LocalPlayer adapt(Player player) {
    return new BukkitPlayer(player, this, groupResolver, profileService, settings);
  }

}

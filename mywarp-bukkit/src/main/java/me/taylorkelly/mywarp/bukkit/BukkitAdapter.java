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

import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.util.BlockFace;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Adapts between equivalent local and Bukkit objects.
 *
 * <p>Operations of this class are always stateless. Conversions that depend on a state are available in {@link
 * MyWarpPlugin}.</p>
 */
public final class BukkitAdapter {

  private BukkitAdapter() {
  }

  /**
   * Adapts between a LocalWorld and a World.
   *
   * @param world the LocalWorld
   * @return the World representing the given LocalWorld
   */
  public static World adapt(LocalWorld world) {
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
  public static LocalWorld adapt(World world) {
    return new BukkitWorld(world);
  }

  /**
   * Adapts between a LocalPlayer and a Player.
   *
   * @param player the LocalPlayer
   * @return the Player representing the given LocalPlayer
   */
  public static Player adapt(LocalPlayer player) {
    if (player instanceof BukkitPlayer) {
      return ((BukkitPlayer) player).getWrapped();
    }
    Player loadedPlayer = Bukkit.getPlayer(player.getUniqueId());
    if (loadedPlayer == null) {
      throw new IllegalArgumentException("Cannot find a loaded player for " + player + "in Bukkit.");
    }
    return loadedPlayer;
  }

  static BlockFace adapt(org.bukkit.block.BlockFace blockFace) {
    switch (blockFace) {
      case NORTH:
        return BlockFace.NORTH;
      case EAST:
        return BlockFace.EAST;
      case SOUTH:
        return BlockFace.SOUTH;
      case WEST:
        return BlockFace.WEST;
      case UP:
        return BlockFace.UP;
      case DOWN:
        return BlockFace.DOWN;
      default:
        return BlockFace.NONE;
    }
  }
}

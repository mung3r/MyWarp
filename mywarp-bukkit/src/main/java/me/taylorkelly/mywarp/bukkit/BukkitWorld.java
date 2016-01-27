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

import me.taylorkelly.mywarp.BlockType;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.NoSuchWorldException;
import me.taylorkelly.mywarp.util.Vector3;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

/**
 * A reference to a World in Bukkit.
 */
public class BukkitWorld implements LocalWorld {

  private final UUID worldIdentifier;

  /**
   * Creates an instance that references the given World.
   *
   * @param world the World.
   */
  public BukkitWorld(World world) {
    this.worldIdentifier = world.getUID();
  }

  @Override
  public String getName() {
    return getLoadedWorld().getName();
  }

  @Override
  public UUID getUniqueId() {
    return worldIdentifier;
  }

  /**
   * Gets the loaded World that is referenced by this BukkitWorld.
   *
   * @return the loaded World
   * @throws NoSuchWorldException if the World is no longer loaded
   */
  public World getLoadedWorld() {
    World ret = Bukkit.getWorld(worldIdentifier);
    if (ret == null) {
      throw new NoSuchWorldException(worldIdentifier.toString());
    }
    return ret;
  }

  @Override
  public BlockType getBlock(Vector3 position) {
    return new BukkitBlockType(this, position);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BukkitWorld that = (BukkitWorld) o;

    if (!worldIdentifier.equals(that.worldIdentifier)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return worldIdentifier.hashCode();
  }
}

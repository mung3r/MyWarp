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

import me.taylorkelly.mywarp.BlockType;
import me.taylorkelly.mywarp.util.Vector3;

import org.bukkit.Material;

/**
 * A reference to a block in Bukkit.
 */
public class BukkitBlockType implements BlockType {

  private final BukkitWorld world;
  private final Vector3 position;

  /**
   * Constructs an instance.
   *
   * @param world    the world of the block
   * @param position the position of the block
   */
  public BukkitBlockType(BukkitWorld world, Vector3 position) {
    this.world = world;
    this.position = position;
  }

  @Override
  public boolean isSafeToStandIn() {
    Material material = getBlockMaterial();
    switch (material) {
      case LAVA:
      case STATIONARY_LAVA:
      case FIRE:
        return false;
      case WATER:
        return true;
      default:
        return !material.isSolid();
    }
  }

  @Override
  public boolean isSafeToStandOn() {
    Material material = getBlockMaterial();
    switch (material) {
      case CACTUS:
        return false;
      case WATER:
        return true;
      default:
        return material.isSolid();
    }
  }

  @Override
  public boolean isNotFullHeight() {
    Material material = getBlockMaterial();
    switch (material) {
      case BED_BLOCK:
      case STEP:
      case WOOD_STAIRS:
      case CHEST:
      case COBBLESTONE_STAIRS:
      case CAKE_BLOCK:
      case TRAP_DOOR:
      case BRICK_STAIRS:
      case SMOOTH_STAIRS:
      case NETHER_BRICK_STAIRS:
      case ENCHANTMENT_TABLE:
      case BREWING_STAND:
      case CAULDRON:
      case WOOD_STEP:
      case SANDSTONE_STAIRS:
      case ENDER_CHEST:
      case SPRUCE_WOOD_STAIRS:
      case BIRCH_WOOD_STAIRS:
      case JUNGLE_WOOD_STAIRS:
      case SKULL:
      case TRAPPED_CHEST:
      case DAYLIGHT_DETECTOR:
      case QUARTZ_STAIRS:
      case ACACIA_STAIRS:
      case DARK_OAK_STAIRS:
        return true;
      default:
        return false;
    }
  }

  /**
   * Gets the block's Material.
   *
   * @return the Material of this block
   */
  private Material getBlockMaterial() {
    return world
        .getLoadedWorld()
        .getBlockAt((int) position.getFloorX(), (int) position.getFloorY(),
                    (int) position.getFloorZ()).getType();
  }

}

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

import org.bukkit.Material;

/**
 * Provides information about Materials.
 */
class MaterialInfo {

  private MaterialInfo() {
  }

  /**
   * Returns whether a regular entity (without any status effects) can stand <i>within</i> a block of the given material
   * without taking any damage from doing so.
   *
   * @param material the material to check
   * @return {@code true} if an entity can safely stand within a block of the given material
   */
  static boolean canEntitySafelyStandWithin(Material material) {
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

  /**
   * Returns whether a regular entity (without any status effects) can stand <i>on</i> a block of the given material
   * without taking any damage from doing so.
   *
   * @param material the material to check
   * @return {@code true} if an entity can safely stand within a block of the given material
   */
  static boolean canEntitySafelyStandOn(Material material) {
    switch (material) {
      case CACTUS:
        return false;
      case WATER:
        return true;
      default:
        return material.isSolid();
    }
  }

  /**
   * Returns whether a block of the given material is smaller than a normal full block.
   *
   * @param material the material to check
   * @return {@code true} if this particular block is smaller than a normal block
   */
  static boolean isNotFullHeight(Material material) {
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
}

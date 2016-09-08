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

import me.taylorkelly.mywarp.util.BlockFace;

/**
 * A reference to an existing sign in Bukkit.
 */
class BukkitSign implements me.taylorkelly.mywarp.platform.Sign {

  private final org.bukkit.block.Sign bukkitSign;

  /**
   * Creates an instance that references the given sign.
   *
   * @param bukkitSign the sign
   */
  BukkitSign(org.bukkit.block.Sign bukkitSign) {
    this.bukkitSign = bukkitSign;
  }

  @Override
  public String getLine(int line) {
    return bukkitSign.getLine(line);
  }

  @Override
  public void setLine(int line, String text) {
    bukkitSign.setLine(line, text);
  }

  /**
   * Returns whether this sign is attached to the given block face.
   *
   * @param blockFace the block face.
   * @return {@code true} if this sign is attached to the given block face
   */
  boolean isAttached(BlockFace blockFace) {
    org.bukkit.material.Sign signMat = asMaterial();
    return signMat.isWallSign() && blockFace.equals(BukkitAdapter.adapt(signMat.getAttachedFace()));
  }

  private org.bukkit.material.Sign asMaterial() {
    return (org.bukkit.material.Sign) bukkitSign.getData();
  }

}

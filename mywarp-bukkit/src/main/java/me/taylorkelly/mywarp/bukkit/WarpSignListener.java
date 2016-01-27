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

import com.google.common.collect.ImmutableSet;

import me.taylorkelly.mywarp.bukkit.util.AbstractListener;
import me.taylorkelly.mywarp.warp.WarpSignManager;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Attachable;

/**
 * Listens for events involving signs and feats them to a {@link me.taylorkelly.mywarp.warp.WarpSignManager};
 */
public class WarpSignListener extends AbstractListener {

  private static final ImmutableSet<Material>
      SUPPORTED_ATTACHABLES =
      ImmutableSet.of(Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.LEVER);
  private static final ImmutableSet<Material>
      SUPPORTED_PLATES =
      ImmutableSet.of(Material.WOOD_PLATE, Material.STONE_PLATE);

  private final BukkitAdapter adapter;
  private final WarpSignManager warpSignManager;

  /**
   * Initializes this listener.
   *
   * @param adapter         the adapter
   * @param warpSignManager the warpSignManager that will be feat by this listener
   */
  public WarpSignListener(BukkitAdapter adapter, WarpSignManager warpSignManager) {
    this.adapter = adapter;
    this.warpSignManager = warpSignManager;
  }

  /**
   * Called whenever a sign is changed.
   *
   * @param event the event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    String[] lines = event.getLines();
    if (warpSignManager.isWarpSign(lines)) {
      if (warpSignManager.validateWarpSign(lines, adapter.adapt(event.getPlayer()))) {
        for (int i = 0; i < lines.length; i++) {
          event.setLine(i, lines[i]);
        }
      } else {
        event.getBlock().breakNaturally();
        event.setCancelled(true);
      }
    }
  }

  /**
   * Called whenever a player interacts with a block.
   *
   * @param event the event
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {

    // a player clicked on a block
    if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      Block block = event.getClickedBlock();

      if (block.getState() instanceof Sign) {
        Sign sign = (Sign) block.getState();

        if (warpSignManager.isWarpSign(sign.getLines())) {
          warpSignManager.warpFromSign(sign.getLine(WarpSignManager.WARPNAME_LINE), adapter.adapt(event.getPlayer()));
          event.setCancelled(true);
        }

      } else if (SUPPORTED_ATTACHABLES.contains(block.getType())) {

        Attachable attachable = (Attachable) block.getState().getData();
        Block behind = block.getRelative(attachable.getAttachedFace(), 2);

        if (!(behind.getState() instanceof Sign)) {
          return;
        }

        org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) behind.getState().getData();
        Sign signBut = (Sign) behind.getState();

        if (!(signMat.getFacing() == attachable.getAttachedFace() && warpSignManager.isWarpSign(signBut.getLines()))) {
          return;
        }

        warpSignManager.warpFromSign(signBut.getLine(WarpSignManager.WARPNAME_LINE), adapter.adapt(event.getPlayer()));
      }
      // a player stepped on something
    } else if (event.getAction().equals(Action.PHYSICAL)) {
      if (SUPPORTED_PLATES.contains(event.getClickedBlock().getType())) {
        Block twoBelow = event.getClickedBlock().getRelative(BlockFace.DOWN, 2);

        if (!(twoBelow.getState() instanceof Sign)) {
          return;
        }
        Sign signBelow = (Sign) twoBelow.getState();

        if (!warpSignManager.isWarpSign(signBelow.getLines())) {
          return;
        }
        warpSignManager
            .warpFromSign(signBelow.getLine(WarpSignManager.WARPNAME_LINE), adapter.adapt(event.getPlayer()));
      }
    }
  }

}

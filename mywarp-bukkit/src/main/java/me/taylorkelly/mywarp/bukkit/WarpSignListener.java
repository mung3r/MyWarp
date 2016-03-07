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

import me.taylorkelly.mywarp.bukkit.util.AbstractListener;
import me.taylorkelly.mywarp.sign.WarpSignHandler;

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
 * Listens for events involving signs and feats them to a {@link WarpSignHandler}.
 */
public class WarpSignListener extends AbstractListener {

  private static final ImmutableSet<Material>
      SUPPORTED_ATTACHABLES =
      ImmutableSet.of(Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.LEVER);
  private static final ImmutableSet<Material>
      SUPPORTED_PLATES =
      ImmutableSet.of(Material.WOOD_PLATE, Material.STONE_PLATE, Material.GOLD_PLATE, Material.IRON_PLATE);

  private final MyWarpPlugin plugin;
  private final WarpSignHandler warpSignHandler;

  /**
   * Initializes this listener.
   *
   * @param warpSignHandler the warpSignHandler that will be feat by this listener
   */
  WarpSignListener(MyWarpPlugin plugin, WarpSignHandler warpSignHandler) {
    this.plugin = plugin;
    this.warpSignHandler = warpSignHandler;
  }

  /**
   * Called whenever a sign is changed.
   *
   * @param event the event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    String[] lines = event.getLines();

    Optional<Boolean>
        isValidWarpSign =
        warpSignHandler.handleSignCreation(plugin.wrap(event.getPlayer()), new EventSign(event));

    if (!isValidWarpSign.isPresent()) {
      return;
    }

    if (!isValidWarpSign.get()) {
      event.getBlock().breakNaturally();
      event.setCancelled(true);
      return;
    }

    for (int i = 0; i < lines.length; i++) {
      event.setLine(i, lines[i]);
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

        boolean cancel = warpSignHandler.handleSignInteraction(plugin.wrap(event.getPlayer()), new BukkitSign(sign));
        event.setCancelled(cancel);

      } else if (SUPPORTED_ATTACHABLES.contains(block.getType())) {

        Attachable attachable = (Attachable) block.getState().getData();
        Block behind = block.getRelative(attachable.getAttachedFace(), 2);

        if (!(behind.getState() instanceof Sign)) {
          return;
        }

        org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) behind.getState().getData();
        Sign sign = (Sign) behind.getState();

        if (!(signMat.getFacing() == attachable.getAttachedFace())) {
          return;
        }

        warpSignHandler.handleSignInteraction(plugin.wrap(event.getPlayer()), new BukkitSign(sign));
      }
      // a player stepped on something
    } else if (event.getAction().equals(Action.PHYSICAL)) {
      if (SUPPORTED_PLATES.contains(event.getClickedBlock().getType())) {
        Block twoBelow = event.getClickedBlock().getRelative(BlockFace.DOWN, 2);

        if (!(twoBelow.getState() instanceof Sign)) {
          return;
        }
        Sign sign = (Sign) twoBelow.getState();

        warpSignHandler.handleSignInteraction(plugin.wrap(event.getPlayer()), new BukkitSign(sign));
      }
    }
  }

  private class BukkitSign implements me.taylorkelly.mywarp.platform.Sign {

    private final Sign bukkitSign;

    private BukkitSign(Sign bukkitSign) {
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
  }

  private class EventSign implements me.taylorkelly.mywarp.platform.Sign {

    private final SignChangeEvent event;

    private EventSign(SignChangeEvent event) {
      this.event = event;
    }

    @Override
    public String getLine(int line) {
      return event.getLine(line);
    }

    @Override
    public void setLine(int line, String text) {
      event.setLine(line, text);
    }
  }

}

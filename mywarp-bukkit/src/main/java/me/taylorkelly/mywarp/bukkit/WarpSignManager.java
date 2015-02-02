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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import org.bukkit.ChatColor;
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

import java.util.TreeSet;

/**
 * Manages warp signs.
 */
public class WarpSignManager extends AbstractListener {

  private static final int IDENTIFIER_LINE = 1;
  private static final int WARPNAME_LINE = 2;
  private static final ImmutableSet<Material>
      SUPPORTED_ATTACHABLES =
      ImmutableSet.of(Material.STONE_BUTTON, Material.WOOD_BUTTON, Material.LEVER);
  private static final ImmutableSet<Material>
      SUPPORTED_PLATES =
      ImmutableSet.of(Material.WOOD_PLATE, Material.STONE_PLATE);

  private static final DynamicMessages MESSAGES = new DynamicMessages("me.taylorkelly.mywarp.lang.WarpSignManager");
  // NON-NLS-1

  private final TreeSet<String> identifiers;
  private final WarpManager manager;
  private final BukkitAdapter adapter;

  /**
   * Initializes this manager using the given identifiers, operation on the given WarpManager.
   *
   * @param identifiers the identifiers
   * @param manager     the WarpManager
   * @param adapter     the adapter
   */
  public WarpSignManager(Iterable<String> identifiers, WarpManager manager, BukkitAdapter adapter) {
    this.identifiers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    Iterables.addAll(this.identifiers, identifiers);
    this.manager = manager;
    this.adapter = adapter;
  }

  /**
   * Called whenever a sign is changed.
   *
   * @param event the event
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    if (isSignWarp(event.getLines())) {
      if (!validateWarpSign(event, adapter.adapt(event.getPlayer()))) {
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

        if (isWarpSign(sign)) {
          warpFromSign(sign.getLine(WARPNAME_LINE), adapter.adapt(event.getPlayer()));
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

        if (!(signMat.getFacing() == attachable.getAttachedFace() && isWarpSign(signBut))) {
          return;
        }

        warpFromSign(signBut.getLine(WARPNAME_LINE), adapter.adapt(event.getPlayer()));
      }
      // a player stepped on something
    } else if (event.getAction().equals(Action.PHYSICAL)) {
      if (SUPPORTED_PLATES.contains(event.getClickedBlock().getType())) {
        Block twoBelow = event.getClickedBlock().getRelative(BlockFace.DOWN, 2);

        if (!(twoBelow.getState() instanceof Sign)) {
          return;
        }
        Sign signBelow = (Sign) twoBelow.getState();

        if (!isWarpSign(signBelow)) {
          return;
        }
        warpFromSign(signBelow.getLine(WARPNAME_LINE), adapter.adapt(event.getPlayer()));
      }
    }
  }

  /**
   * Warps the given player to the warp with the given name.
   *
   * @param warpName the name
   * @param player   the player who should be teleported
   */
  public void warpFromSign(String warpName, final LocalPlayer player) {
    LocaleManager.setLocale(player.getLocale());
    if (!player.hasPermission("mywarp.warp.sign.use")) { // NON-NLS
      player.sendError(MESSAGES.getString("use-permission", player)); // NON-NLS-1
      return;
    }

    Optional<Warp> optional = manager.get(warpName);
    if (!optional.isPresent()) {
      player.sendError(MESSAGES.getString("warp-non-existent", player, warpName)); // NON-NLS-1
      return;
    }
    final Warp warp = optional.get();

    if (!warp.isUsable(player)) {
      player.sendError(MESSAGES.getString("use-warp-permission", player, warpName)); // NON-NLS-1
      return;
    }

    if (MyWarp.getInstance().getSettings().isEconomyEnabled()) {
      if (MyWarp.getInstance().getEconomyManager().informativeHasAtLeast(player, FeeType.WARP_SIGN_USE)) {
        return;
      }
    }

    // REVIEW include fix for BUKKIT-4365?
    warp.teleport(player, FeeType.WARP_SIGN_USE);

  }

  /**
   * Validates a warp sign, taken from the given sign change event. This method expects that the given event belongs to
   * a valid warp sign!
   *
   * @param sign   the sign change event
   * @param player the player who created the warp sign
   * @return true if the sign could be created
   */
  public boolean validateWarpSign(SignChangeEvent sign, LocalPlayer player) {
    LocaleManager.setLocale(player.getLocale());
    if (!player.hasPermission("mywarp.warp.sign.create")) { // NON-NLS
      player.sendError(MESSAGES.getString("create-permission", player)); // NON-NLS-1
      return false;
    }
    String name = sign.getLine(WARPNAME_LINE);
    Optional<Warp> optional = manager.get(name);

    if (!optional.isPresent()) {
      player.sendError(MESSAGES.getString("warp-non-existent", player, name)); // NON-NLS-1
      return false;
    }
    Warp warp = optional.get();

    if (!warp.isModifiable(player) && !player.hasPermission("mywarp.warp.sign.create.all")) { // NON-NLS
      player.sendError(MESSAGES.getString("create-warp-permission", player, name)); // NON-NLS-1
      return false;
    }

    if (MyWarp.getInstance().getEconomyManager().informativeHasAtLeast(player, FeeType.WARP_SIGN_CREATE)) {
      return false;
    }
    MyWarp.getInstance().getEconomyManager().informativeWithdraw(player, FeeType.WARP_SIGN_CREATE);

    // get the right spelling (case) out of the config
    String line = sign.getLine(IDENTIFIER_LINE);
    line = line.substring(1, line.length() - 1);
    sign.setLine(IDENTIFIER_LINE, "[" + identifiers.ceiling(line) + "]");

    player.sendMessage(ChatColor.AQUA + MESSAGES.getString("created-successful", player)); // NON-NLS-1
    // NON-NLS-1
    return true;
  }

  /**
   * Returns whether the given sign is a valid warp sign.
   *
   * @param sign the sign to check
   * @return true if the sign is a warp sign
   */
  public boolean isWarpSign(Sign sign) {
    return isSignWarp(sign.getLines());
  }

  /**
   * Returns whether the given array of lines belongs to a valid warp sign.
   *
   * @param lines an array with the lines of the sign
   * @return true if the sign is a warp sign
   */
  public boolean isSignWarp(String[] lines) {
    String identifier = lines[IDENTIFIER_LINE];
    return identifier.startsWith("[") && identifier.endsWith("]") && identifiers
        .contains(identifier.substring(1, identifier.length() - 1));
  }
}

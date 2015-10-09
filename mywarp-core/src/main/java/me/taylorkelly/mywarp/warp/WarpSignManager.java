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

package me.taylorkelly.mywarp.warp;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.economy.EconomyManager;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationService;

import java.util.TreeSet;

/**
 * Manages warp signs.
 */
public class WarpSignManager {

  private static final int IDENTIFIER_LINE = 1;
  public static final int WARPNAME_LINE = 2;
  private static final DynamicMessages MESSAGES = new DynamicMessages("me.taylorkelly.mywarp.lang.WarpSignManager");

  private final TreeSet<String> identifiers;
  private final WarpManager warpManager;
  private final AuthorizationService authorizationService;
  private final EconomyManager economyManager;

  /**
   * Creates an instance.
   *
   * @param identifiers          the identifiers to identify a valid warp sign
   * @param warpManager          the WarpManager this manager will act on
   * @param authorizationService the AuthorizationService used to resolve authorizations
   * @param economyManager       the EconomyManager this manager will act on
   */
  public WarpSignManager(Iterable<String> identifiers, WarpManager warpManager,
                         AuthorizationService authorizationService, EconomyManager economyManager) {
    this.identifiers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    Iterables.addAll(this.identifiers, identifiers);

    this.warpManager = warpManager;
    this.authorizationService = authorizationService;
    this.economyManager = economyManager;
  }

  /**
   * Teleports the given player to the warp with the given name.
   *
   * @param warpName the name
   * @param player   the player who should be teleported
   */
  public void warpFromSign(String warpName, final LocalPlayer player) {
    LocaleManager.setLocale(player.getLocale());
    if (!player.hasPermission("mywarp.sign.use")) {
      player.sendError(MESSAGES.getString("use-permission"));
      return;
    }

    Optional<Warp> optional = warpManager.get(warpName);
    if (!optional.isPresent()) {
      player.sendError(MESSAGES.getString("warp-non-existent", warpName));
      return;
    }
    final Warp warp = optional.get();

    if (!authorizationService.isUsable(warp, player)) {
      player.sendError(MESSAGES.getString("use-warp-permission", warpName));
      return;
    }

    if (!economyManager.hasAtLeast(player, FeeType.WARP_SIGN_USE)) {
      return;
    }

    warp.teleport(player, FeeType.WARP_SIGN_USE);

  }

  /**
   * Validates a warp sign, taken from the given sign change event. This method expects that the given event belongs to
   * a valid warp sign!
   *
   * @param lines  an array with the lines of the sign, including empty ones
   * @param player the player who created the warp sign
   * @return true if the sign could be created
   */
  public boolean validateWarpSign(String[] lines, LocalPlayer player) {
    LocaleManager.setLocale(player.getLocale());
    if (!player.hasPermission("mywarp.sign.create.self")) {
      player.sendError(MESSAGES.getString("create-permission"));
      return false;
    }
    String name = lines[WARPNAME_LINE];
    Optional<Warp> optional = warpManager.get(name);

    if (!optional.isPresent()) {
      player.sendError(MESSAGES.getString("warp-non-existent", name));
      return false;
    }
    Warp warp = optional.get();

    if (!authorizationService.isModifiable(warp, player) && !player.hasPermission("mywarp.sign.create")) {
      player.sendError(MESSAGES.getString("create-warp-permission", name));
      return false;
    }

    if (!economyManager.hasAtLeast(player, FeeType.WARP_SIGN_CREATE)) {
      return false;
    }
    economyManager.withdraw(player, FeeType.WARP_SIGN_CREATE);

    // get the right spelling (case) out of the config
    String line = lines[IDENTIFIER_LINE];
    line = line.substring(1, line.length() - 1);
    lines[IDENTIFIER_LINE] = "[" + identifiers.ceiling(line) + "]";

    player.sendMessage(MESSAGES.getString("created-successful"));
    return true;
  }

  /**
   * Returns whether the given array of lines belongs to a valid warp sign.
   *
   * @param lines an array with the lines of the sign, including empty ones
   * @return true if the sign is a warp sign
   */
  public boolean isWarpSign(String[] lines) {
    String identifier = lines[IDENTIFIER_LINE];
    return identifier.startsWith("[") && identifier.endsWith("]") && identifiers
        .contains(identifier.substring(1, identifier.length() - 1));
  }
}

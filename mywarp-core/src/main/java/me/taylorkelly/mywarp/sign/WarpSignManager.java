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

package me.taylorkelly.mywarp.sign;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.capability.EconomyCapability;
import me.taylorkelly.mywarp.service.economy.EconomyService;
import me.taylorkelly.mywarp.service.economy.FeeType;
import me.taylorkelly.mywarp.service.teleport.EconomyTeleportService;
import me.taylorkelly.mywarp.service.teleport.HandlerTeleportService;
import me.taylorkelly.mywarp.service.teleport.TeleportService;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.util.teleport.TeleportHandler;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;

import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * Manages warp signs.
 *
 * <p>As of itself this class does nothing. It must be feat by a event system that tracks creation and clinking on
 * signs.</p>
 */
public class WarpSignManager {

  public static final int WARPNAME_LINE = 2;

  private static final int IDENTIFIER_LINE = 1;

  private static final DynamicMessages msg = new DynamicMessages("me.taylorkelly.mywarp.lang.WarpSignManager");

  private final TreeSet<String> identifiers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
  private final AuthorizationResolver authorizationResolver;
  private final WarpManager warpManager;
  private final TeleportService teleportService;

  @Nullable
  private final EconomyService economyService;

  /**
   * Creates an instance.
   *
   * @param identifiers           the identifiers to identify a valid warp sign
   * @param warpManager           the WarpManager this manager will act on
   * @param authorizationResolver the AuthorizationResolver used to resolve authorizations
   * @param game                  the game within which this instance acts
   * @param handler               the TeleportHandler that handles teleports
   * @param economyCapability     an Optional with the platform's capability to provide economical functionality
   */
  public WarpSignManager(Iterable<String> identifiers, WarpManager warpManager,
                         AuthorizationResolver authorizationResolver, Game game, TeleportHandler handler,
                         Optional<EconomyCapability> economyCapability) {
    Iterables.addAll(this.identifiers, identifiers);

    this.warpManager = warpManager;
    this.authorizationResolver = authorizationResolver;

    if (economyCapability.isPresent()) {
      this.economyService = new EconomyService(economyCapability.get());
    } else {
      this.economyService = null;
    }
    this.teleportService = createTeleportService(game, handler, economyCapability);
  }

  private TeleportService createTeleportService(Game game, TeleportHandler handler,
                                                Optional<EconomyCapability> economyCapability) {
    TeleportService ret = new HandlerTeleportService(handler, game);

    if (economyCapability.isPresent()) {
      ret = new EconomyTeleportService(ret, new EconomyService(economyCapability.get()), FeeType.WARP_SIGN_USE);
    }
    return ret;
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
      player.sendError(msg.getString("use-permission"));
      return;
    }

    Optional<Warp> optional = warpManager.get(warpName);
    if (!optional.isPresent()) {
      player.sendError(msg.getString("warp-non-existent", warpName));
      return;
    }
    final Warp warp = optional.get();

    if (!authorizationResolver.isUsable(warp, player)) {
      player.sendError(msg.getString("use-warp-permission", warpName));
      return;
    }

    teleportService.teleport(player, warp);
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
      player.sendError(msg.getString("create-permission"));
      return false;
    }
    String name = lines[WARPNAME_LINE];
    Optional<Warp> optional = warpManager.get(name);

    //validate warp existence
    if (!optional.isPresent()) {
      player.sendError(msg.getString("warp-non-existent", name));
      return false;
    }
    Warp warp = optional.get();

    //validate authorization
    if (!authorizationResolver.isModifiable(warp, player) && !player.hasPermission("mywarp.sign.create")) {
      player.sendError(msg.getString("create-warp-permission", name));
      return false;
    }

    //validate economy
    if (economyService != null) {
      if (!economyService.hasAtLeast(player, FeeType.WARP_SIGN_CREATE)) {
        return false;
      }
      economyService.withdraw(player, FeeType.WARP_SIGN_CREATE);
    }

    // get the right spelling (case) out of the config
    String line = lines[IDENTIFIER_LINE];
    line = line.substring(1, line.length() - 1);
    lines[IDENTIFIER_LINE] = "[" + identifiers.ceiling(line) + "]";

    player.sendMessage(msg.getString("created-successful"));
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

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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.platform.Sign;
import me.taylorkelly.mywarp.platform.capability.EconomyCapability;
import me.taylorkelly.mywarp.service.economy.EconomyService;
import me.taylorkelly.mywarp.service.economy.FeeType;
import me.taylorkelly.mywarp.service.teleport.EconomyTeleportService;
import me.taylorkelly.mywarp.service.teleport.HandlerTeleportService;
import me.taylorkelly.mywarp.service.teleport.TeleportService;
import me.taylorkelly.mywarp.util.BlockFace;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.util.teleport.TeleportHandler;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;

import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * Handles interaction with warp signs.
 *
 * <p>A warp sign is a sign that has an identifier (e.g. 'MyWarp') enclosed by brackets in the second and the name of an
 * existing warp in the third line. If a player interacts with a sign by clicking it, he is teleported to the warp
 * specified on the sign, if he meets certain conditions.</p>
 *
 * <p>As of itself this class does nothing. It must be feat by a event system that tracks creation and clinking on
 * signs.</p>
 */
public class WarpSignHandler {

  private static final int WARPNAME_LINE = 2;
  private static final int IDENTIFIER_LINE = 1;

  private static final DynamicMessages msg = new DynamicMessages("me.taylorkelly.mywarp.lang.WarpSigns");

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
   */
  public WarpSignHandler(Iterable<String> identifiers, WarpManager warpManager,
                         AuthorizationResolver authorizationResolver, Game game, TeleportHandler handler) {
    this(identifiers, warpManager, authorizationResolver, game, handler, null);
  }

  /**
   * Creates an instance.
   *
   * @param identifiers           the identifiers to identify a valid warp sign
   * @param warpManager           the WarpManager this manager will act on
   * @param authorizationResolver the AuthorizationResolver used to resolve authorizations
   * @param game                  the game within which this instance acts
   * @param handler               the TeleportHandler that handles teleports
   * @param economyCapability     the platform's capability to provide economical functionality (may be {@code null} if
   *                              the plattform does not provide an economy capability)
   */
  public WarpSignHandler(Iterable<String> identifiers, WarpManager warpManager,
                         AuthorizationResolver authorizationResolver, Game game, TeleportHandler handler,
                         @Nullable EconomyCapability economyCapability) {
    Iterables.addAll(this.identifiers, identifiers);

    this.warpManager = warpManager;
    this.authorizationResolver = authorizationResolver;

    if (economyCapability != null) {
      this.economyService = new EconomyService(economyCapability);
    } else {
      this.economyService = null;
    }
    this.teleportService = createTeleportService(game, handler, economyCapability);
  }

  private TeleportService createTeleportService(Game game, TeleportHandler handler,
                                                @Nullable EconomyCapability economyCapability) {
    TeleportService ret = new HandlerTeleportService(handler, game);

    if (economyCapability != null) {
      ret = new EconomyTeleportService(ret, new EconomyService(economyCapability), FeeType.WARP_SIGN_USE);
    }
    return ret;
  }

  /**
   * Handles the creation of the given {@code sign} by the given {@code player}. Returns an Optional with the result of
   * this creation: {@code true} if the sign is a warp sign and could be created, {@code false} if the sign is a warp
   * sign but could not be created, {@code Optional.absent()} if the sign is not a warp sign.
   *
   * <p>If the sign is a warp sign, the player has the permission to create warp signs, the warp given on the warp sign
   * exists and the player may create warp signs to it, the sign may be created. If any of this conditions is not met,
   * the sign may not be created and the player is informed (if appropriate).</p>
   *
   * @param player the player who created the sign
   * @param sign   the created sign
   * @return the result
   */
  public Optional<Boolean> handleSignCreation(LocalPlayer player, Sign sign) {
    if (!isWarpSign(sign)) {
      return Optional.absent();
    }

    //validate permission
    LocaleManager.setLocale(player.getLocale());
    if (!player.hasPermission("mywarp.sign.create.self")) {
      player.sendError(msg.getString("permission.create"));
      return Optional.of(false);
    }
    String name = sign.getLine(WARPNAME_LINE);
    Optional<Warp> optional = warpManager.getByName(name);

    //validate warp existence
    if (!optional.isPresent()) {
      player.sendError(msg.getString("warp-non-existent", name));
      return Optional.of(false);
    }
    Warp warp = optional.get();

    //validate authorization
    if (!authorizationResolver.isModifiable(warp, player) && !player.hasPermission("mywarp.sign.create")) {
      player.sendError(msg.getString("permission.create.to-warp", name));
      return Optional.of(false);
    }

    //validate economy
    if (economyService != null) {
      if (!economyService.hasAtLeast(player, FeeType.WARP_SIGN_CREATE)) {
        return Optional.of(false);
      }
      economyService.withdraw(player, FeeType.WARP_SIGN_CREATE);
    }

    // get the right spelling (case) out of the config
    String line = sign.getLine(IDENTIFIER_LINE);
    line = line.substring(1, line.length() - 1);
    sign.setLine(IDENTIFIER_LINE, "[" + identifiers.ceiling(line) + "]");

    player.sendMessage(msg.getString("created-successful"));
    return Optional.of(true);
  }

  /**
   * Handles the interaction of the given {@code player} with the given {@code blockFace} of the block at the given
   * {@code position}.
   *
   * <p>If position and block face can be traced back to a warp sign, the player has the permission to use warp signs,
   * the warp given on the warp sign exists and is usable by the player, he is teleported there. If any of this
   * conditions is not met, the handling is aborted and the player is informed (if appropriate).</p>
   *
   * <p>Typically an interaction is a right click.</p>
   *
   * @param player    the player who interacted
   * @param position  the position of the interaction
   * @param blockFace the blockFace of the interaction
   * @return {@code true} if the sign is a warp sign
   */
  public boolean handleInteraction(LocalPlayer player, Vector3i position, BlockFace blockFace) {
    LocalWorld world = player.getWorld();
    Optional<Sign> sign;

    switch (blockFace) {
      case NORTH:
      case EAST:
      case SOUTH:
      case WEST:
        sign = world.getAttachedSign(position.add(blockFace.getVector().mul(2)), blockFace.getOpposite());
        break;
      case UP:
      case DOWN:
        sign = world.getSign(position.sub(blockFace.getVector().mul(2)));
        break;
      default:
        sign = world.getSign(position);
    }

    return !sign.isPresent() || handleInteraction(player, sign.get());
  }

  /**
   * Handles the interaction of the given {@code player} with the given {@code sign}. Returns {@code true} if and only
   * if the sign is a valid warp sign.
   *
   * <p>If the sign is a warp sign, the player has the permission to use warp signs, the warp given on the warp sign
   * exists and is usable by the player, he is teleported there. If any of this conditions is not met, the handling is
   * aborted and the player is informed (if appropriate).</p>
   *
   * <p>Typically an interaction is a right click.</p>
   *
   * @param player the player who interacted with the the sign
   * @param sign   the sign interacted with
   * @return {@code true} if the sign is a warp sign
   */
  public boolean handleInteraction(LocalPlayer player, Sign sign) {
    if (!isWarpSign(sign)) {
      return false;
    }
    LocaleManager.setLocale(player.getLocale());
    if (!player.hasPermission("mywarp.sign.use")) {
      player.sendError(msg.getString("permission.use"));
      return true;
    }

    String warpName = sign.getLine(WARPNAME_LINE);

    Optional<Warp> optional = warpManager.getByName(warpName);
    if (!optional.isPresent()) {
      player.sendError(msg.getString("warp-non-existent", warpName));
      return true;
    }
    final Warp warp = optional.get();

    if (!authorizationResolver.isUsable(warp, player)) {
      player.sendError(msg.getString("permission.use.to-warp", warpName));
      return true;
    }

    teleportService.teleport(player, warp);
    return true;
  }

  private boolean isWarpSign(Sign sign) {
    String identifier = sign.getLine(IDENTIFIER_LINE);

    if (!(identifier.startsWith("[") && identifier.endsWith("]"))) {
      return false;
    }
    identifier = identifier.substring(1, identifier.length() - 1);
    return identifiers.contains(identifier);
  }
}

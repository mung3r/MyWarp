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

package me.taylorkelly.mywarp.bukkit.timer;

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.timer.AbortableTimerAction;
import me.taylorkelly.mywarp.timer.Duration;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * A warmup that teleports a player to a warp when done.
 */
public class WarpWarmup extends AbortableTimerAction<Profile> {

  private static final int ALLOWED_DISTANCE = 2;

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;
  private final Warp warp;
  private final Vector3 initialPosition;
  private final double initialHealth;

  /**
   * Initializes the warp-warmup.
   *
   * @param myWarp the MyWarp instance
   * @param player the player who is cooling down
   * @param warp   the warp that the player wants to use
   */
  public WarpWarmup(MyWarp myWarp, LocalPlayer player, Warp warp) {
    super(player.getProfile());
    this.myWarp = myWarp;
    this.warp = warp;
    this.initialPosition = player.getPosition();
    this.initialHealth = player.getHealth();
  }

  @Override
  public void run() {
    Optional<LocalPlayer> optionalPlayer = myWarp.getGame().getPlayer(getTimedSuject().getUniqueId());
    if (!optionalPlayer.isPresent()) {
      return;
    }
    LocalPlayer player = optionalPlayer.get();
    LocaleManager.setLocale(player.getLocale());

    if (myWarp.getSettings().isEconomyEnabled()) {
      if (myWarp.getEconomyManager().informativeHasAtLeast(player, FeeProvider.FeeType.WARP_TO)) {
        return;
      }
    }

    warp.teleport(player, FeeProvider.FeeType.WARP_TO);
    Duration duration = myWarp.getPlatform().getDurationProvider().getDuration(player, WarpCooldown.class);
    myWarp.getPlatform().getTimerService().start(player.getProfile(), duration, new WarpCooldown(myWarp, player));
  }

  @Override
  public boolean abort() {
    Optional<LocalPlayer> player = myWarp.getGame().getPlayer(getTimedSuject().getUniqueId());
    // player is not online, but might re-login so the timer continues
    return player.isPresent() && (abortOnMove(player.get()) || abortOnDamage(player.get()));
  }

  /**
   * Returns whether the warmup should be aborted because the player has moved.
   *
   * @param player the LocalPlayer
   * @return true if the warmup should be aborted
   */
  private boolean abortOnMove(LocalPlayer player) {
    if (myWarp.getSettings().isTimersWarmupAbortOnMove() || player
        .hasPermission("mywarp.warmup.disobey.moveabort")) { // NON-NLS
      return false;
    }
    if (!(player.getPosition().distanceSquared(initialPosition) > (ALLOWED_DISTANCE * ALLOWED_DISTANCE))) {
      return false;
    }
    LocaleManager.setLocale(player.getLocale());
    player.sendError(MESSAGES.getString("warp-to.warmup.cancelled-move"));
    return true;
  }

  /**
   * Returns whether the warmup should be aborted because the player has taken damage.
   *
   * @param player the LocalPlayer
   * @return true if the warmup should be aborted
   */
  private boolean abortOnDamage(LocalPlayer player) {
    if (myWarp.getSettings().isTimersWarmupAbortOnDamage() || player
        .hasPermission("mywarp.warmup.disobey.dmgabort")) { // NON-NLS
      return false;
    }
    if (!(player.getHealth() < initialHealth)) {
      return false;
    }
    LocaleManager.setLocale(player.getLocale());
    player.sendError(MESSAGES.getString("warp-to.warmup.cancelled-damage"));
    return true;
  }

}

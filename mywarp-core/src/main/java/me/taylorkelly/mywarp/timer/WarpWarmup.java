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

package me.taylorkelly.mywarp.timer;

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.teleport.TeleportService;
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

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final Warp warp;
  private final Vector3 initialPosition;
  private final double initialHealth;
  private final Settings settings;
  private final Game game;
  private final TeleportService teleportService;
  private DurationProvider durationProvider;
  private TimerService timerService;

  /**
   * Creates an instance for the given {@code player} and {@code warp}.
   *
   * @param player           the player who is cooling down
   * @param warp             the warp that the player wants to use
   * @param timerService     the timer service used to schedule the cooldown (if any)
   * @param durationProvider the duration provider that provides duration for the cooldown (if any)
   */
  public WarpWarmup(LocalPlayer player, Warp warp, Settings settings, Game game, TeleportService teleportService,
                    TimerService timerService, DurationProvider durationProvider) {
    super(player.getProfile());
    this.warp = warp;
    this.settings = settings;
    this.game = game;
    this.teleportService = teleportService;
    this.timerService = timerService;
    this.durationProvider = durationProvider;
    this.initialPosition = player.getPosition();
    this.initialHealth = player.getHealth();
  }

  @Override
  public void run() {
    Optional<LocalPlayer> optionalPlayer = game.getPlayer(getTimedSuject().getUniqueId());
    if (!optionalPlayer.isPresent()) {
      return;
    }
    LocalPlayer player = optionalPlayer.get();
    LocaleManager.setLocale(player.getLocale());

    if (teleportService.teleport(player, warp).isPositionModified()) {
      Duration duration = durationProvider.getDuration(player, WarpCooldown.class);
      timerService.start(player.getProfile(), duration, new WarpCooldown(player, game, settings));
    }
  }

  @Override
  public boolean abort() {
    Optional<LocalPlayer> player = game.getPlayer(getTimedSuject().getUniqueId());
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
    if (!settings.isTimersWarmupAbortOnMove() || player.hasPermission("mywarp.timer.disobey.warmup-abort.move")) {
      return false;
    }
    if (player.getPosition().distanceSquared(initialPosition) <= Math.pow(ALLOWED_DISTANCE, 2)) {
      return false;
    }
    LocaleManager.setLocale(player.getLocale());
    player.sendError(msg.getString("warp-to.warmup.cancelled-move"));
    return true;
  }

  /**
   * Returns whether the warmup should be aborted because the player has taken damage.
   *
   * @param player the LocalPlayer
   * @return true if the warmup should be aborted
   */
  private boolean abortOnDamage(LocalPlayer player) {
    if (!settings.isTimersWarmupAbortOnDamage() || player.hasPermission("mywarp.timer.disobey.warmup-abort.damage")) {
      return false;
    }
    if (player.getHealth() >= initialHealth) {
      return false;
    }
    LocaleManager.setLocale(player.getLocale());
    player.sendError(msg.getString("warp-to.warmup.cancelled-damage"));
    return true;
  }

}

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

package me.taylorkelly.mywarp.teleport;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.timer.Duration;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.timer.WarpCooldown;
import me.taylorkelly.mywarp.timer.WarpWarmup;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.concurrent.TimeUnit;

/**
 * Sets timers for users who want to teleport.
 * <p/>
 * Only {@link LocalPlayer}s without the disobey permission need to warmup when teleporting or cooldown after
 * teleporting (or both). The teleport itself is delegated to another teleportService.
 * <p/>
 * If timers are disabled in the configuration, teleports are immediately delegated.
 */
public class TimerTeleportService extends ForwardingTeleportService {

  //TODO move resources into dedicated bundle
  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final TeleportService delegate;
  private final Settings settings;
  private final Game game;
  private final TimerService timerService;
  private final DurationProvider durationProvider;

  /**
   * Creates an instance that uses the given TimerService and the given DurationProvider to create and resolve timers.
   * Teleports are delegated to the given TeleportService.
   *
   * @param delegate         the TeleportService to delegate teleports to
   * @param settings         the configures Settings instance
   * @param game             the current Game instance
   * @param timerService     the TimerService
   * @param durationProvider the DurationProvider
   */
  public TimerTeleportService(TeleportService delegate, Settings settings, Game game, TimerService timerService,
                              DurationProvider durationProvider) {
    this.delegate = delegate;
    this.settings = settings;
    this.game = game;
    this.timerService = timerService;
    this.durationProvider = durationProvider;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity, Warp warp) {

    if (!settings.isTimersEnabled() || canDisobeyTimers(entity)) {
      return delegate().teleport(entity, warp);
    }

    LocalPlayer player = (LocalPlayer) entity;

    // check for already running timers
    TimerService.EvaluationResult cooldownResult = timerService.has(player.getProfile(), WarpCooldown.class);
    if (cooldownResult.isTimerRunning()) {
      player
          .sendError(msg.getString("exception.timer-running", cooldownResult.getDurationLeft().get(TimeUnit.SECONDS)));
      return TeleportStatus.NONE;
    }
    TimerService.EvaluationResult warmupResult = timerService.has(player.getProfile(), WarpWarmup.class);
    if (warmupResult.isTimerRunning()) {
      player.sendError(msg.getString("exception.timer-running", warmupResult.getDurationLeft().get(TimeUnit.SECONDS)));
      return TeleportStatus.NONE;
    }

    // start warmup
    Duration duration = durationProvider.getDuration(player, WarpWarmup.class);
    timerService.start(player.getProfile(), duration,
                       new WarpWarmup(player, warp, settings, game, delegate(), timerService, durationProvider));
    player.sendMessage(msg.getString("warp-to.warmup.started", warp.getName(), duration.get(TimeUnit.SECONDS)));

    // teleport will be scheduled by WarpWarmup once the warmup ended
    return TeleportStatus.NONE;
  }

  @Override
  protected TeleportService delegate() {
    return delegate;
  }

  /**
   * Returns whether the given Entity can disobey timers. <p/> <p>If this method returns {@code false} it is guaranteed
   * that the given entity is a {@link LocalPlayer}.</p>
   *
   * @param entity the entity
   * @return {@code true} if the entity can disobey timers
   */
  private boolean canDisobeyTimers(LocalEntity entity) {
    if (!(entity instanceof LocalPlayer)) {
      return true;
    }
    return ((LocalPlayer) entity).hasPermission("mywarp.timer.disobey");
  }
}

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

package me.taylorkelly.mywarp.bukkit.commands;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.util.parametric.TimerRunningException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.PlayerBinding.Sender;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.Name;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.Name.Condition;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.timer.Duration;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.timer.TimerService.EvaluationResult;
import me.taylorkelly.mywarp.timer.WarpCooldown;
import me.taylorkelly.mywarp.timer.WarpWarmup;
import me.taylorkelly.mywarp.util.CommandUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

/**
 * Bundles usage commands.
 */
public class UsageCommands {

  private static final DynamicMessages MESSAGES = new DynamicMessages(CommandUtils.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;

  /**
   * Creates an instance.
   *
   * @param myWarp the MyWarp instance
   */
  public UsageCommands(MyWarp myWarp) {
    this.myWarp = myWarp;
  }

  /**
   * Teleports a player to a Warp.
   *
   * @param player the LocalPlayer
   * @param warp   the Warp
   * @throws TimerRunningException if timers are enabled and a timer is already running for the player using this
   *                               command
   */
  @Command(aliases = {"to"}, desc = "warp-to.description")
  @Require("mywarp.cmd.to")
  public void to(@Sender LocalPlayer player, @Name(Condition.USABLE) Warp warp) throws TimerRunningException {
    FeeType feeType = FeeType.WARP_TO;

    if (!myWarp.getEconomyManager().hasAtLeast(player, feeType)) {
      return;
    }

    // XXX This implementation is ugly and inflexible
    if (myWarp.getSettings().isTimersEnabled() && !player.hasPermission("mywarp.timer.disobey")) {
      TimerService timerService = myWarp.getPlatform().getTimerService();
      DurationProvider durationProvider = myWarp.getPlatform().getDurationProvider();

      EvaluationResult cooldownResult = timerService.has(player.getProfile(), WarpCooldown.class);
      if (cooldownResult.isTimerRunning()) {
        throw new TimerRunningException(cooldownResult.getDurationLeft().get());
      }
      EvaluationResult warmupResult = timerService.has(player.getProfile(), WarpWarmup.class);
      if (warmupResult.isTimerRunning()) {
        throw new TimerRunningException(warmupResult.getDurationLeft().get());
      }
      Duration duration = durationProvider.getDuration(player, WarpWarmup.class);
      timerService.start(player.getProfile(), duration, new WarpWarmup(myWarp, player, warp));

      player.sendMessage(ChatColor.AQUA + MESSAGES
          .getString("warp-to.warmup.started", warp.getName(), duration.get(TimeUnit.SECONDS)));
      return;
    }
    warp.teleport(player, feeType);
  }

}

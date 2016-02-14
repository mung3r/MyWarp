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

package me.taylorkelly.mywarp.command.definition;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.command.parametric.TimerRunningException;
import me.taylorkelly.mywarp.command.parametric.binding.PlayerBinding.Sender;
import me.taylorkelly.mywarp.command.parametric.binding.WarpBinding.Name;
import me.taylorkelly.mywarp.command.parametric.binding.WarpBinding.Name.Condition;
import me.taylorkelly.mywarp.economy.EconomyService;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.teleport.EconomyTeleportService;
import me.taylorkelly.mywarp.teleport.TeleportService;
import me.taylorkelly.mywarp.teleport.TimerTeleportService;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Bundles usage commands.
 */
public class UsageCommands {

  private final TeleportService teleportService;

  /**
   * Creates an instance.
   *
   * @param teleportService  the TeleportService used by commands, implementing additional validation on top
   * @param settings         the Settings used by commands
   * @param economyService   the EconomyService used by commands
   * @param timerService     the TimerService used by commands
   * @param durationProvider the DurationProvider used by commands
   * @param game             the Game instance used by commands
   */
  public UsageCommands(TeleportService teleportService, Settings settings, EconomyService economyService,
                       TimerService timerService, DurationProvider durationProvider, Game game) {
    this.teleportService =
        new TimerTeleportService(
            new EconomyTeleportService(teleportService, economyService, FeeProvider.FeeType.WARP_TO), settings, game,
            timerService, durationProvider);
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
    teleportService.teleport(player, warp);
  }

}

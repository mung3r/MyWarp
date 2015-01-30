/*
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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

import java.util.concurrent.TimeUnit;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.timer.WarpCooldown;
import me.taylorkelly.mywarp.bukkit.timer.WarpWarmup;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding.Sender;
import me.taylorkelly.mywarp.bukkit.util.TimerRunningException;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition.Type;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.timer.Duration;
import me.taylorkelly.mywarp.timer.TimerService.EvaluationResult;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;

/**
 * Bundles usage commands.
 */
public class UsageCommands {

    // REVIEW move these somewhere else - where? MyWarp?
    public static final String RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Commands"; // NON-NLS
    public static final String CONVERSATIONS_RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Conversations"; // NON-NLS

    private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

    /**
     * Teleports a player to a Warp.
     * 
     * @param player
     *            the LocalPlayer
     * @param warp
     *            the Warp
     * @throws TimerRunningException
     *             if timers are enabled and a timer is already running for the
     *             player using this command
     */
    @Command(aliases = { "to" }, desc = "warp-to.description")
    @Require("mywarp.warp.basic.warp-to")
    public void to(@Sender LocalPlayer player, @Condition(Type.USABLE) Warp warp)
            throws TimerRunningException {
        FeeType feeType = FeeType.WARP_TO;

        if (!MyWarp.getInstance().getEconomyManager().informativeHasAtLeast(player, feeType)) {
            return;
        }

        // XXX This implementation is ugly and inflexible
        if (MyWarp.getInstance().getSettings().isTimersEnabled()
                && !player.hasPermission("mywarp.timer.disobey")) { // NON-NLS
            EvaluationResult cooldownResult = MyWarp.getInstance().getTimerService()
                    .has(player.getProfile(), WarpCooldown.class);
            if (cooldownResult.isTimerRunning()) {
                throw new TimerRunningException(cooldownResult.getDurationLeft().get());
            }
            EvaluationResult warmupResult = MyWarp.getInstance().getTimerService()
                    .has(player.getProfile(), WarpWarmup.class);
            if (warmupResult.isTimerRunning()) {
                throw new TimerRunningException(warmupResult.getDurationLeft().get());
            }
            Duration duration = MyWarp.getInstance().getDurationProvider()
                    .getDuration(player, WarpWarmup.class);
            MyWarp.getInstance().getTimerService()
                    .start(player.getProfile(), duration, new WarpWarmup(player, warp));

            player.sendMessage(ChatColor.AQUA
                    + MESSAGES.getString("warp-to.warmup.started", warp.getName(),
                            duration.get(TimeUnit.SECONDS)));
            return;
        }
        warp.teleport(player, feeType);
    }

}

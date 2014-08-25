/**
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
package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.FeeBundle;
import me.taylorkelly.mywarp.timer.WarpCooldown;
import me.taylorkelly.mywarp.timer.WarpWarmup;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.NestedCommand;

import org.bukkit.entity.Player;

/**
 * This class contains all root-level commands annotated by the
 * {@link NestedCommand} annotation.
 */
public class RootCommands {

    /**
     * Warps the player to a warp.
     * 
     * @param args
     *            the command-arguments
     * @param player
     *            the player who initiated this command
     * @throws CommandException
     *             if the command is cancelled
     */
    @NestedCommand({ AdminCommands.class, BasicCommands.class, SocialCommands.class })
    @Command(aliases = { "warp", "mv", "mywarp" }, usage = "<name>", desc = "commands.warp-to.description", min = 1, permissions = { "mywarp.warp.basic.warp" })
    public void warpTo(CommandContext args, Player player) throws CommandException {
        // first check the economy
        if (MyWarp.inst().isEconomySetup()) {
            FeeBundle fees = MyWarp.inst().getPermissionsManager().getFeeBundleManager().getBundle(player);

            if (!fees.hasAtLeast(player, FeeBundle.Fee.WARP_TO)) {
                return;
            }
        }

        Warp warp = CommandUtils.getUsableWarp(player, args.getJoinedStrings(0));
        if (MyWarp.inst().getSettings().isTimersEnabled()
                && !MyWarp.inst().getTimerManager().canDisobey(player)) {
            if (MyWarp.inst().getTimerManager().hasRunningTimer(player.getUniqueId(), WarpCooldown.class)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString(
                                "commands.warp-to.cooldown.active",
                                player,
                                MyWarp.inst().getTimerManager()
                                        .getRemainingSeconds(player.getUniqueId(), WarpCooldown.class)));
            }

            if (MyWarp.inst().getTimerManager().hasRunningTimer(player.getUniqueId(), WarpWarmup.class)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString(
                                "commands.warp-to.warmup.active",
                                player,
                                MyWarp.inst().getTimerManager()
                                        .getRemainingSeconds(player.getUniqueId(), WarpWarmup.class)));
            }

            MyWarp.inst().getTimerManager().registerNewTimer(new WarpWarmup(player, warp));

        } else {
            warp.teleport(player, FeeBundle.Fee.WARP_TO);
        }
    }
}

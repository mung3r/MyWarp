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
package me.taylorkelly.mywarp.timer;

import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.FeeBundle;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A warmup that teleports a player to a warp when done.
 */
public class WarpWarmup extends TimerAction<UUID> {

    private static final int CHECK_FREQUENCY = 2;

    private final Warp warp;

    /**
     * Initializes the warp-warmup.
     * 
     * @param player
     *            the player who is cooling down
     * @param warp
     *            the warp that the player wants to use
     */
    public WarpWarmup(Player player, Warp warp) {
        super(player.getUniqueId(), MyWarp.inst().getPermissionsManager().getTimeBundleManager()
                .getBundle(player).getTicks(TimeBundle.Time.WARP_WARMUP));
        this.warp = warp;
    }

    @Override
    protected void run(TimerManager timerManager, Plugin plugin) {
        Player player = MyWarp.server().getPlayer(type);

        if (MyWarp.inst().getSettings().isTimersWarmupNotifyOnStart()) {
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.warp-to.warmup.started", player, warp.getName(), duration / 20));
        }

        if (MyWarp.inst().getSettings().isTimersWarmupAbortOnDamage()) {
            new HealthCheck(player.getHealth()).runTaskTimer(MyWarp.inst(), 20 * CHECK_FREQUENCY,
                    20 * CHECK_FREQUENCY);
        }
        if (MyWarp.inst().getSettings().isTimersWarmupAbortOnMove()) {
            new MovementCheck(player.getLocation()).runTaskTimer(MyWarp.inst(), 20 * CHECK_FREQUENCY,
                    20 * CHECK_FREQUENCY);
        }
        super.run(timerManager, plugin);
    }

    @Override
    public void action() {
        Player player = MyWarp.server().getPlayer(type);
        if (player == null) {
            return;
        }

        if (MyWarp.inst().isEconomySetup()) {
            FeeBundle fees = MyWarp.inst().getPermissionsManager().getFeeBundleManager().getBundle(player);
            if (!fees.hasAtLeast(player, FeeBundle.Fee.WARP_TO)) {
                return;
            }
        }

        warp.teleport(player, FeeBundle.Fee.WARP_TO);
        MyWarp.inst().getTimerManager().registerNewTimer(new WarpCooldown(player));
    }

    /**
     * Runnable that checks if the player moves.
     */
    private class MovementCheck extends PlayerCheck {

        private final Location originalLoc;

        /**
         * Initializes the check with the given location. A player may not move
         * away from it.
         * 
         * @param originalLoc
         *            the original location
         */
        public MovementCheck(Location originalLoc) {
            this.originalLoc = originalLoc;
        }

        @Override
        public boolean cancelAction(Player player) {
            Location loc = player.getLocation();
            if (loc.distanceSquared(originalLoc) >= 2 * 2) {
                // REVIEW permission logic
                if (!MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.timer.disobey.moveabort")) {
                    player.sendMessage(MyWarp.inst().getLocalizationManager()
                            .getString("commands.warp-to.warmup.cancelled-move", player));
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Runnable that checks if the player loses any health.
     */
    private class HealthCheck extends PlayerCheck {

        private final double originalHealth;

        /**
         * Initialzes this check with the given health amount. The checked
         * player may not have less health than this.
         * 
         * @param originalHealth
         *            the original health
         */
        public HealthCheck(double originalHealth) {
            this.originalHealth = originalHealth;
        }

        @Override
        public boolean cancelAction(Player player) {
            if (player.getHealth() < originalHealth) {
                // REVIEW permission logic
                if (!MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.timer.disobey.dmgabort")) {
                    player.sendMessage(MyWarp.inst().getLocalizationManager()
                            .getString("commands.warp-to.warmup.cancelled-damage", player));
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * An abstract runnable that runs checks on a player.
     */
    private abstract class PlayerCheck extends BukkitRunnable {

        /**
         * Returns whether the underling action should be cancelled.
         * 
         * @param player
         *            the player who is checked
         * @return true if the action should be cancelled
         */
        public abstract boolean cancelAction(Player player);

        @Override
        public void run() {
            if (!MyWarp.inst().getTimerManager().hasRunningTimer(type, WarpWarmup.class)) {
                cancel();
                return;
            }
            Player player = MyWarp.server().getPlayer(type);
            if (player == null) {
                cancel();
                return;
            }
            if (cancelAction(player)) {
                WarpWarmup.this.cancel();
                cancel();
            }
        }
    }
}

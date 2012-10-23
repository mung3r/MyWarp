package me.taylorkelly.mywarp.scheduler;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.timer.Cooldown;
import me.taylorkelly.mywarp.timer.GeneralTimer;
import me.taylorkelly.mywarp.timer.PlayerCooldown;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.timer.Warmup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

// TODO: Auto-generated Javadoc
/*	Copyright (c) 2012, Nick Porillo milkywayz@mail.com
 *
 *	Permission to use, copy, modify, and/or distribute this software for any purpose 
 *  with or without fee is hereby granted, provided that the above copyright notice 
 *  and this permission notice appear in all copies.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE 
 *	INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE 
 *  FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *	OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, 
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

public class Scheduler {

    /**
     * Create an instance of Schedule Reference main class for plugin Define a
     * playername for cooldown to affect Define a Time for the timer *Since it
     * deals with bukkit methods. Strickly synchronous.
     * 
     * @param plugin
     *            the plugin
     * @param player
     *            the player
     * @param cooldown
     *            the cooldown
     * @return an instanceof Schedule
     */
    public static Schedule playerCooldown(MyWarp plugin, Player player, Cooldown cooldown) {
        return new Schedule(player.getName(), cooldown, System.currentTimeMillis(),
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                        new PlayerCooldown(player),
                        cooldown.getMinecraftLong()), false);
    }

    /**
     * Create an instance of Schedule Reference main class for plugin Define a
     * playername for warmup to affect Define a Time for the timer Define a
     * runnable to be executed when scheduler ends *Since it deals with bukkit
     * methods. Strickly synchronous.
     * 
     * @param plugin
     *            the plugin
     * @param player
     *            the player
     * @param warmup
     *            the warmup
     * @param cooldown
     *            the cooldown (needed for the runnable that is executed when
     *            warmup ends)
     * @param name
     *            the warp (needed for the runnable that is executed when warmup
     *            ends)
     * @param server
     *            the server (needed for the runnable that is executed when
     *            warmup ends)
     * @param runOnFinish
     *            runnable to be executed when scheduler ends
     * @return an instanceof Schedule
     */
    public static Schedule playerWarmup(MyWarp plugin, Player player, Warmup warmup,
            Cooldown cooldown, String name) {
        return new Schedule(player.getName(), warmup, System.currentTimeMillis(), Bukkit
                .getScheduler().scheduleSyncDelayedTask(plugin,
                        new PlayerWarmup(plugin, player, cooldown, name),
                        warmup.getMinecraftLong()), false);
    }

    /**
     * Create an instance of Schedule <br>
     * Reference main class for plugin <br>
     * Define a time for the cooldown <br>
     * *Note: This cooldown is global for what your using it for, it doesnt
     * matter which player <br>
     * *Strickly an async schedule, if you want async use:
     * <p>
     * scheduleGeneralCooldown(new Schedule(null, new Group(time), time,
     * System.currentTimeMillis(), Bukkit.getScheduler()
     * .scheduleAsyncDelayedTask(plugin, new PlayerCooldown(),
     * time.getMinecraftLong()), true);
     * 
     * @param plugin
     *            the plugin
     * @param time
     *            the time
     * @return an instance of Schedule
     */
    public static Schedule generalCooldown(MyWarp plugin, Time time) {
        return new Schedule(null, time, System.currentTimeMillis(), Bukkit
                .getScheduler().scheduleSyncDelayedTask(plugin, null,
                        time.getMinecraftLong()), false);
    }

    /**
     * Schedules a new player timer Uses the Schedule passed along for
     * information schedulePlayerCooldown(schedule(this, player, cooldown);
     * 
     * @param schedule
     *            the schedule
     */
    public static void schedulePlayerCooldown(Schedule schedule) {
        PlayerCooldown.pctask.add(schedule);
    }
    
    /**
     * Schedules a new player timer Uses the Schedule passed along for
     * information schedulePlayerCooldown(schedule(this, player, cooldown);
     * 
     * @param schedule
     *            the schedule
     */
    public static void schedulePlayerWarmup(Schedule schedule) {
        PlayerCooldown.pwtask.add(schedule);
    }

    /**
     * Schedules a new general timer Uses the schedule passed along for
     * information.
     * 
     * @param schedule
     *            the schedule
     */
    public static void scheduleGeneralTimer(Schedule schedule) {
        if (schedule.getPlayerName() == null) {
            GeneralTimer.gtask.add(schedule);
        }
    }
}

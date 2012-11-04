package me.taylorkelly.mywarp.timer;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.scheduler.ScheduledTask;

// TODO: Auto-generated Javadoc
/*  Copyright (c) 2012, Nick Porillo milkywayz@mail.com
 *
 *  Permission to use, copy, modify, and/or distribute this software for any purpose 
 *  with or without fee is hereby granted, provided that the above copyright notice 
 *  and this permission notice appear in all copies.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE 
 *  INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE 
 *  FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 *  OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, 
 *  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

public class PlayerCooldown implements Runnable, Timer {

    private Player player;

    public PlayerCooldown(Player player) {
        this.player = player;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        if (WarpSettings.coolDownNotify) {
            player.sendMessage(LanguageManager.getString("timer.cooldown.ended"));
        }

        //Removes corresponding ScheduledTask from pctask
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getPlayerName() == player.getName()) {
                itr.remove();
                return;
            }
        }
    }

    /**
     * Fix.
     */
    public static void fix() {
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            long curTime = System.currentTimeMillis();
            if (s.getEndTime() <= curTime) {
                itr.remove();
            }
        }
    }

    /**
     * Checks if cooldown / delay is over.
     * 
     * @param player
     *            the player
     * @param time
     *            the time
     * @return the boolean
     */
    public static Boolean isActive(String player) {
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getPlayerName() == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the remaining time on the cooldown / delay Always returns an
     * integer - About 95% accurate. Negatives no longer possible as they will
     * return 0 and possible cause of the negative will be fixed
     * 
     * @param name
     *            the name
     * @param timer
     *            the timer
     * @return Seconds
     */
    public static Integer getRemainingTime(String name) {
        int time;
        int secs = 0;
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getPlayerName() == name) {
                time = (int) (s.getDuration().getMinecraftLong() - (s.getTime() - System
                        .currentTimeMillis()));
                secs = s.getDuration().getInt() - (time / 1000);
            }
        }
        if (secs < 0) {
            fix();
            return 0;
        }
        return secs;
    }

    /**
     * Gets the long left.
     * 
     * @param name
     *            the name
     * @param timer
     *            the timer
     * @return the long left
     */
    public static Long getLongLeft(String name) {
        long time;
        long secs = 0L;
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getPlayerName() == name) {
                time = s.getDuration().getMinecraftLong()
                        - (s.getTime() - System.currentTimeMillis());
                secs = s.getDuration().getInt() - (time / 1000);
            }
        }
        return secs;
    }

    /**
     * Adds ticks to cooldown / delay, 20 ticks = 1 second - Not a tested
     * method.
     * 
     * @param id
     *            the id
     * @param ticks
     *            the ticks
     */
    public static void addToTimer(Integer id, Long ticks) {
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getTaskId() == id) {
                s.setEndTime(s.getEndTime() + ticks);
            }
        }
    }

    /**
     * Subtract ticks from cooldown / delay, 20 ticks = 1 second - Not a tested
     * method.
     * 
     * @param id
     *            the id
     * @param ticks
     *            the ticks
     */
    public static void substractFromTimer(Integer id, Long ticks) {
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getTaskId() == id) {
                s.setEndTime(s.getEndTime() - ticks);
            }
        }
    }

    /**
     * Ends the cooldown / delay.
     * 
     * @param name
     *            the name
     * @param timer
     *            the timer
     */
    public static void endTimer(String name) {
        Iterator<ScheduledTask> itr = pctask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getPlayerName() == name) {
                Bukkit.getScheduler().cancelTask(s.getTaskId());
                itr.remove();
            }
        }
    }
}

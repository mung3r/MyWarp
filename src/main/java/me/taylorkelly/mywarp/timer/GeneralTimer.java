package me.taylorkelly.mywarp.timer;

import java.util.Iterator;

import me.taylorkelly.mywarp.scheduler.ScheduledTask;

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

public class GeneralTimer implements Runnable, Timer {

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        fix();
    }

    /**
     * Fix.
     */
    public static void fix() {
        Iterator<ScheduledTask> itr = gtask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getEndTime() <= System.currentTimeMillis()) {
                itr.remove();
            }
        }
    }

    /**
     * Checks if is cooling down.
     * 
     * @param time
     *            the time
     * @return the boolean
     */
    public static Boolean isCoolingDown(Time time) {
        Iterator<ScheduledTask> itr = gtask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getDuration() == time) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the remaining time.
     * 
     * @param timer
     *            the timer
     * @return the remaining time
     */
    public static Integer getRemainingTime(Time timer) {
        int time;
        int secs = -1;
        Iterator<ScheduledTask> itr = gtask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getDuration() == timer) {
                time = (int) (timer.getMinecraftLong() - (s.getTime() - System
                        .currentTimeMillis()));
                secs = timer.getInt() - (time / 1000);
            }
        }
        try {
            assert secs >= 0;
        } catch (AssertionError e) {
            fix();
            return 0;
        }
        return secs;
    }

    /**
     * Gets the long left.
     * 
     * @param timer
     *            the timer
     * @return the long left
     */
    public static Long getLongLeft(Time timer) {
        long time;
        long secs = 0L;
        Iterator<ScheduledTask> itr = gtask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getDuration() == timer) {
                time = timer.getMinecraftLong()
                        - (s.getTime() - System.currentTimeMillis());
                secs = timer.getInt() - (time / 1000);
            }
        }
        return secs;
    }

    /**
     * Adds the to delay.
     * 
     * @param id
     *            the id
     * @param ticks
     *            the ticks
     */
    public static void addToDelay(int id, Long ticks) {
        Iterator<ScheduledTask> itr = gtask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getTaskId() == id) {
                s.setEndTime(s.getEndTime() + ticks);
            }
        }
    }

    /**
     * Substact from delay.
     * 
     * @param id
     *            the id
     * @param ticks
     *            the ticks
     */
    public static void substactFromDelay(int id, Long ticks) {
        Iterator<ScheduledTask> itr = gtask.iterator();
        while (itr.hasNext()) {
            ScheduledTask s = itr.next();
            if (s.getTaskId() == id) {
                s.setEndTime(s.getEndTime() - ticks);
            }
        }
    }

    /**
     * Gets the task id.
     * 
     * @param timer
     *            the timer
     * @return the task id
     */
    public static int getTaskId(Time timer) {
        while (gtask.iterator().hasNext()) {
            if (gtask.iterator().next().getDuration() == timer) {
                return gtask.iterator().next().getTaskId();
            }
        }
        return 0;
    }

}

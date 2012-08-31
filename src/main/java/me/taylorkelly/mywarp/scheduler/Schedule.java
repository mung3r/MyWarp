package me.taylorkelly.mywarp.scheduler;

import me.taylorkelly.mywarp.timer.Time;

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

public class Schedule implements ScheduledTask {

    String playername;
    long time;
    int taskid;
    boolean async;
    long endtime;
    Time dur;

    /**
     * Instantiates a new schedule.
     * 
     * @param playername
     *            the playername
     * @param dur
     *            the dur
     * @param time
     *            the time
     * @param taskid
     *            the taskid
     * @param async
     *            the async
     */
    public Schedule(String playername, Time dur, Long time, int taskid, boolean async) {
        this.playername = playername;
        this.time = time;
        this.async = async;
        this.dur = dur;
        this.taskid = taskid;
        this.setEndTime(time + dur.getRealLong());
    }

    /**
     * Gets the player name.
     * 
     * @return the player name
     */
    @Override
    public String getPlayerName() {
        return this.playername;
    }

    /**
     * Gets the time.
     * 
     * @return the time
     */
    @Override
    public long getTime() {
        return this.time;
    }

    /**
     * Gets the task id.
     * 
     * @return the task id
     */
    @Override
    public int getTaskId() {
        return this.taskid;
    }

    /**
     * Gets the time when the schedule ends.
     * 
     * @return the end time
     */
    @Override
    public long getEndTime() {
        return this.endtime;
    }

    /**
     * Sets the time when the schedule ends.
     * 
     * @param endtime
     *            the new end time
     */
    @Override
    public void setEndTime(Long endtime) {
        this.endtime = endtime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.milkycraft.Scheduler.ScheduledTask#isAsync()
     */
    @Override
    public boolean isAsync() {
        return this.async;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.milkycraft.Scheduler.ScheduledTask#setAsync()
     */
    @Override
    public void setAsync() {
        this.async = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.milkycraft.Scheduler.ScheduledTask#getDuration()
     */
    @Override
    public Time getDuration() {
        return this.dur;
    }
}

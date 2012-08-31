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

public interface ScheduledTask {

    /**
     * Gets the player name.
     * 
     * @return the player name
     */
    String getPlayerName();

    /**
     * Gets the time.
     * 
     * @return the time
     */
    long getTime();

    /**
     * Gets the task id.
     * 
     * @return the task id
     */
    int getTaskId();

    /**
     * Gets the end time.
     * 
     * @return the end time
     */
    long getEndTime();

    /**
     * Sets the end time.
     * 
     * @param endtime
     *            the new end time
     */
    void setEndTime(Long endtime);

    /**
     * Checks if is async.
     * 
     * @return true, if is async
     */
    boolean isAsync();

    /**
     * Sets the async.
     */
    void setAsync();

    /**
     * Gets the duration.
     * 
     * @return the duration
     */
    Time getDuration();
}

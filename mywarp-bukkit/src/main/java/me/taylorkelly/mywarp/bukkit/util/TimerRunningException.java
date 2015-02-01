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

package me.taylorkelly.mywarp.bukkit.util;

import me.taylorkelly.mywarp.timer.Duration;

/**
 * Thrown when a Timer is already running.
 */
public class TimerRunningException extends Exception {

    private static final long serialVersionUID = -1170656232204925816L;

    private final Duration durationLeft;

    /**
     * Constructs an instance.
     * 
     * @param durationLeft
     *            the duration until the running timer is done
     */
    public TimerRunningException(Duration durationLeft) {
        this.durationLeft = durationLeft;
    }

    /**
     * Gets the Duration that is left until running Timer is done.
     *
     * @return the Duration left
     */
    public Duration getDurationLeft() {
        return durationLeft;
    }

}

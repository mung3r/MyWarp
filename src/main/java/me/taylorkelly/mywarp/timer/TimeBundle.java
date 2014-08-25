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

import java.util.EnumMap;
import java.util.Map;

import me.taylorkelly.mywarp.permissions.valuebundles.AbstractValueBundle;

/**
 * A bundle that stores times for timers.
 */
public class TimeBundle extends AbstractValueBundle {
    
    private static final int TICKS_PER_SECOND = 20;

    /**
     * The different types of times.
     */
    public enum Time {
        WARP_WARMUP, WARP_COOLDOWN
    }

    private Map<Time, Double> times = new EnumMap<Time, Double>(Time.class);

    /**
     * Initializes this limit-bundle.
     * 
     * @param identifier
     *            the unique identifier
     * @param warpCooldown
     *            the cooldown when using a warp
     * @param warpWarmup
     *            the warmup when using a warp
     **/
    public TimeBundle(String identifier, double warpCooldown, double warpWarmup) {
        super(identifier);
        times.put(Time.WARP_COOLDOWN, warpCooldown);
        times.put(Time.WARP_WARMUP, warpWarmup);
    }

    /**
     * Gets the value of the given time in seconds.
     * 
     * @param time
     *            the time
     * @return the value in seconds
     */
    public double getSeconds(Time time) {
        return times.get(time);
    }

    /**
     * Gets the value of the given time in ticks.
     * 
     * @param time
     *            the time
     * @return the value in ticks
     */
    public long getTicks(Time time) {
        return (long) (getSeconds(time) * TICKS_PER_SECOND);
    }

    @Override
    protected String getBasePermission() {
        return "mywarp.timer";
    }

    @Override
    public String toString() {
        return "TimeBundle [getIdentifier()=" + getIdentifier() + ", times=" + times + "]";
    }

}

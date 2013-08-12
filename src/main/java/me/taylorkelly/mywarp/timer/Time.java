package me.taylorkelly.mywarp.timer;

import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * This storage-object represents one time-configuration for timers
 */
public class Time extends ValuePermissionContainer {

    final private double seconds;

    /**
     * Instantiates a new time-instance
     * 
     * @param name
     *            the name used on permission lookup
     * @param seconds
     *            the time in seconds
     */
    public Time(String name, Double seconds) {
        super(name);
        this.seconds = seconds;
    }

    /**
     * Gets the time in minecraft-ticks
     * 
     * @return the minecraft long
     */

    public Long getTicks() {
        return (long) (seconds * 20);
    }

    /**
     * Gets the time in seconds.
     * 
     * @return the time in seconds
     */
    public double getSeconds() {
        return seconds;
    }
}

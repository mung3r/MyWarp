package me.taylorkelly.mywarp.timer;

import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * This storage-object represents one time-configuration for warmups and
 * cooldowns
 */
public class Time extends ValuePermissionContainer {

    final private double time;

    /**
     * Instantiates a new time.
     * 
     * @param name
     *            the name used on permission lookup
     * @param time
     *            the time
     */
    public Time(String name, Double time) {
        super(name);
        this.time = time;
    }

    /**
     * Gets the time in minecraft-ticks
     * 
     * @return the minecraft long
     */

    public Long getTicks() {
        return (long) (time * 20);
    }

    /**
     * Gets the real long.
     * 
     * @return the real long
     */
    public Long getRealLong() {
        return (long) (time * 1000);
    }

    /**
     * Gets the nano.
     * 
     * @return the nano
     */
    public Long getNano() {
        return (long) (time * 1000000000);
    }

    /**
     * Gets the int.
     * 
     * @return the int
     */
    public Integer getInt() {
        return (int) time;
    }
}

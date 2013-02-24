package me.taylorkelly.mywarp.timer;

public class Time implements Comparable<Time> {

    final private double time;
    final public String name;

    /**
     * Instantiates a new time.
     * 
     * @param time
     *            the time
     */
    public Time(String name, Double time) {
        this.name = name;
        this.time = time;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Time t) {
        return name.compareTo(t.name);
    }

    /**
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

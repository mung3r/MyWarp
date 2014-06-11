package me.taylorkelly.mywarp.timer;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * The TimerFactory manages running timers
 */
public class TimerFactory {

    private final Table<Object, Class<? extends TimerAction<?>>, TimerAction<?>> timers = HashBasedTable
            .create();

    /**
     * Registers a new timer. This method will start the timer right away.
     * 
     * @param timer
     *            the timer
     */
    @SuppressWarnings("unchecked")
    // uncheck situation cannot happen, TimerAction is abstract!
    public void registerNewTimer(TimerAction<?> timer) {
        timers.put(timer.type, (Class<? extends TimerAction<?>>) timer.getClass(), timer);
        timer.runTaskLater(MyWarp.inst(), timer.duration);
    }

    /**
     * Cancels the timer running for the given object identified by the given
     * class-type, if any.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return true if the timer could be ended, false if not
     */
    public boolean cancelTimer(Object identifier, Class<? extends TimerAction<?>> clazz) {
        TimerAction<?> timer = timers.remove(identifier, clazz);
        if (timers == null) {
            return false;
        }
        timer.cancel();
        return true;
    }

    /**
     * Checks if the given object has a timer identified by the given class-type
     * running
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return true if a timer is currently running, false if not
     */
    public boolean hasRunningTimer(Object identifier, Class<? extends TimerAction<?>> clazz) {
        return timers.get(identifier, clazz) != null;
    }

    /**
     * Gets the ticks remaining until the timer for the given object, identified
     * by the given class-type is executed. Will return 0 if not timer is
     * running.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return ticks remaining until the timer is executed
     */
    public long getRemainingTicks(Object identifier, Class<? extends TimerAction<?>> clazz) {
        TimerAction<?> timer = timers.get(identifier, clazz);
        if (timer != null) {
            return timer.getRemainingTicks();
        }
        return 0;
    }

    /**
     * Gets the seconds remaining seconds until the timer for the given object,
     * identified by the given class-type is executed. Will return 0 if not
     * timer is running.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return seconds remaining until the timer is executed
     */
    public int getRemainingSeconds(Object identifier, Class<? extends TimerAction<?>> clazz) {
        long remainingTicks = getRemainingTicks(identifier, clazz);
        // by adding 0.5 it will always round properly
        return remainingTicks != 0 ? (int) ((remainingTicks / 20) + 0.5) : 0;
    }

    /**
     * Represents an action that is scheduled to be executed after some time.
     * 
     * @param <T>
     *            the type that this action is active for
     */
    public abstract class TimerAction<T> extends BukkitRunnable {

        protected final T type;
        private final long duration;
        private final long startTime = System.currentTimeMillis();

        /**
         * Initializes the timer-action
         * 
         * @param type
         *            the type-instance
         * @param duration
         *            the duration of the timer in ticks
         */
        public TimerAction(T type, Long duration) {
            this.type = type;
            this.duration = duration;
        }

        @Override
        public void cancel() {
            timers.remove(type, getClass());
            super.cancel();
        }

        @Override
        public void run() {
            timers.remove(type, getClass());
            action();
        }

        /**
         * The action executed once the timer finishes
         */
        public abstract void action();

        /**
         * Gets the ticks remaining until this action is executed
         * 
         * @return the remaining ticks
         */
        public long getRemainingTicks() {
            // duration (20 ticks per sec) - runTime in milli-sec (1000 per sec)
            return duration - ((System.currentTimeMillis() - startTime) / 50);
        }
    }
}

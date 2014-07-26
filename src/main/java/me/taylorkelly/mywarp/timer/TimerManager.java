package me.taylorkelly.mywarp.timer;

import org.bukkit.command.CommandSender;
import me.taylorkelly.mywarp.MyWarp;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * The TimerFactory manages running timers
 */
public class TimerManager {

    private final Table<Object, Class<? extends TimerAction<?>>, TimerAction<?>> timers = HashBasedTable
            .create();

    /**
     * Returns whether the given command-sender can disobey all types of timers.
     * 
     * @param sender
     *            the command-sender
     * @return true if the given sender can disobey timers
     */
    public boolean canDisobey(CommandSender sender) {
        return !MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.timer.disobey");
    }

    /**
     * Registers a new timer. This method will start the timer right away.
     * 
     * @param timer
     *            the timer
     */
    // uncheck situation cannot happen, TimerAction is abstract!
    @SuppressWarnings("unchecked")
    public <T> void registerNewTimer(TimerAction<T> timer) {
        timers.put(timer.type, (Class<? extends TimerAction<T>>) timer.getClass(), timer);
        if (timer.duration == 0) {
            timer.action();
        } else {
            timer.run(this, MyWarp.inst());
        }
    }

    /**
     * Cancels the timer running for the given identifier of the given
     * class-type, if any.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return true if the timer could be ended
     */
    public <T> boolean cancelTimer(T identifier, Class<? extends TimerAction<T>> clazz) {
        TimerAction<?> timer = timers.remove(identifier, clazz);
        if (timers == null) {
            return false;
        }
        timer.cancel();
        return true;
    }

    /**
     * Checks if the given object has a identifier of the given class-type
     * running.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return true if a timer is currently running
     */
    public <T> boolean hasRunningTimer(T identifier, Class<? extends TimerAction<T>> clazz) {
        return timers.get(identifier, clazz) != null;
    }

    /**
     * Gets the ticks remaining until the timer for the given identifier of the
     * given class-type is executed. Returns 0 if not timer is running.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return ticks remaining until the timer is executed
     */
    public <T> long getRemainingTicks(T identifier, Class<? extends TimerAction<T>> clazz) {
        TimerAction<?> timer = timers.get(identifier, clazz);
        if (timer != null) {
            return timer.getRemainingTicks();
        }
        return 0;
    }

    /**
     * Gets the remaining seconds until the timer for the given identifier of
     * the given class-type is executed. Will return 0 if not timer is running.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     * @return seconds remaining until the timer is executed
     */
    public <T> int getRemainingSeconds(T identifier, Class<? extends TimerAction<T>> clazz) {
        long remainingTicks = getRemainingTicks(identifier, clazz);
        // by adding 0.5 it will always round properly
        return remainingTicks != 0 ? (int) ((remainingTicks / 20) + 0.5) : 0;
    }

    /**
     * Removes the timer for the given object, identified by the given
     * class-type from the underlying Table. This method is called only by a
     * TimerAction when it ends itself and <b>must not be used</b> otherwise.
     * 
     * @param identifier
     *            the object the timer runs for
     * @param clazz
     *            the class of the timer
     */
    // this method is called in the TimerAction using getClass() which strips
    // any generic informations from the returned class, so rawtypes must be
    // accepted.
    protected <T> void remove(T identifier, @SuppressWarnings("rawtypes") Class<? extends TimerAction> clazz) {
        timers.remove(identifier, clazz);
    }
}

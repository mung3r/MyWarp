package me.taylorkelly.mywarp.timer;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents an action that is scheduled to be executed after some time.
 * 
 * @param <T>
 *            the type that this action is active for
 */
public abstract class TimerAction<T> extends BukkitRunnable {

    private TimerManager timerManager;

    protected final T type;

    protected final long duration;

    protected final long startTime = System.currentTimeMillis();

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

    /**
     * Gets the ticks remaining until this action is executed
     * 
     * @return the remaining ticks
     */
    public long getRemainingTicks() {
        // duration (20 ticks per sec) - runTime in milli-sec (1000 per sec)
        return duration - ((System.currentTimeMillis() - startTime) / 50);
    }

    /**
     * The action executed once the timer finishes
     */
    public abstract void action();

    @Override
    public void cancel() {
        timerManager.remove(type, getClass());
        super.cancel();
    }

    @Override
    public void run() {
        timerManager.remove(type, getClass());
        action();
    }

    /**
     * Runs this action. This method is automatically called when the action is
     * submitted via {@link TimerManager#registerNewTimer(TimerAction)}.
     * 
     * It <b>must never</b> be called manually!
     * 
     * @param timerManager
     *            the timerFactory that manages this TimerAction
     * @param plugin
     *            the plugin that runs this TimerAction
     */
    protected void run(TimerManager timerManager, Plugin plugin) {
        this.timerManager = timerManager;
        this.runTaskLater(plugin, duration);
    }

}
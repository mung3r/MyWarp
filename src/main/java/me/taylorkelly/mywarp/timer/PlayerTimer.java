package me.taylorkelly.mywarp.timer;

import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class provides the abstract form for any form of times for players, such
 * as warmups or cooldowns
 */
public abstract class PlayerTimer extends BukkitRunnable {

    protected final Player player;
    private final Time duration;
    private final Long startTime;

    /**
     * Initializes this player-timer. This will store all values and start the
     * timer.
     * 
     * @param player
     *            the player this timer applies for
     * @param durration
     *            the durration of this timer
     */
    public PlayerTimer(Player player, Time durration) {
        this.player = player;
        this.duration = durration;
        this.startTime = System.currentTimeMillis();

        // run the task right on creation
        runTaskLater(MyWarp.inst(), durration.getTicks());
        getTimerMap().put(player.getName(), this);
    }

    /**
     * Gets the Map that stores all currently running timers stored under their
     * respective player's name
     * 
     * @return a map with all running timers
     */
    public abstract Map<String, PlayerTimer> getTimerMap();

    @Override
    public void run() {
        getTimerMap().remove(player.getName());
    }

    @Override
    public void cancel() {
        super.cancel();
        getTimerMap().remove(player.getName());
    }

    /**
     * Gets the time this timer started (measured in UTC)
     * 
     * @return the time this timer was started
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the duration as {@link Time}
     * 
     * @return the total duration of this timer
     */
    public Time getDuration() {
        return duration;
    }

    /**
     * Gets the remaining time on this timer.
     * 
     * @return the remaining time in seconds
     */
    public Integer getRemainingTime() {
        int time = (int) (duration.getTicks() - (startTime - System
                .currentTimeMillis()));
        return duration.getInt() - (time / 1000);
    }
}

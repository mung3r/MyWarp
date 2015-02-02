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

package me.taylorkelly.mywarp.bukkit.timer;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.timer.AbortableTimerAction;
import me.taylorkelly.mywarp.timer.Duration;
import me.taylorkelly.mywarp.timer.TimerAction;
import me.taylorkelly.mywarp.timer.TimerService;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * Manages timers when running on Bukkit.
 */
@SuppressWarnings("rawtypes")
public class BukkitTimerService implements TimerService {

  private final Table<Object, Class<? extends TimerAction>, SelfRunningRunnable>
      runningTimers =
      HashBasedTable
          .create();
  private final MyWarpPlugin plugin;

  /**
   * Creates an instance.
   *
   * @param plugin the running plugin instance
   */
  public BukkitTimerService(MyWarpPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public <T> void start(T timedSubject, Duration duration, TimerAction<T> action) {
    Preconditions.checkArgument(!has(timedSubject, action.getClass()).isTimerRunning(),
                                "A timer of the type %s is already running for %s.", timedSubject,
                                action.getClass()); // NON-NLS

    runningTimers
        .put(timedSubject, action.getClass(), new SelfRunningRunnable<T>(action, duration));
  }

  @Override
  public EvaluationResult has(Object timedSubject, Class<? extends TimerAction> clazz) {
    SelfRunningRunnable action = runningTimers.get(timedSubject, clazz);
    if (action != null) {
      return new EvaluationResult(true, action.getRemainingTime());
    }
    return EvaluationResult.NO_RUNNING_TIMER;
  }

  @Override
  public void cancel(Object timedSubject, Class<? extends TimerAction> clazz) {
    BukkitRunnable runnable = runningTimers.remove(timedSubject, clazz);
    if (runnable != null) {
      runnable.cancel();
    }
  }

  /**
   * A BukkitRunnable that starts itself after creation.
   *
   * @param <T> the type of the instance the timer runs on
   */
  public class SelfRunningRunnable<T> extends BukkitRunnable {

    private final TimerAction<T> runnable;
    private final long startMillis;
    private final long durationMillis;

    @Nullable
    private final BukkitRunnable abortingCheck;

    /**
     * Creates an instance. The given Runnable will be submitted for execution after the given
     * Duration.
     *
     * @param runnable the Runnable
     * @param duration the Duration
     */
    public SelfRunningRunnable(TimerAction<T> runnable, Duration duration) {
      this.runnable = runnable;
      this.durationMillis = duration.get(TimeUnit.MILLISECONDS);

      this.startMillis = System.currentTimeMillis();
      runTaskLater(plugin, duration.getTicks());

      if (runnable instanceof AbortableTimerAction) {
        abortingCheck = new BukkitRunnable() {

          @Override
          public void run() {
            if (((AbortableTimerAction<T>) SelfRunningRunnable.this.runnable).abort()) {
              SelfRunningRunnable.this.cancel();
            }
          }

        };
        abortingCheck.runTaskTimer(plugin, AbortableTimerAction.CHECK_INTERVAL_TICKS,
                                   AbortableTimerAction.CHECK_INTERVAL_TICKS);
      } else {
        abortingCheck = null;
      }
    }

    /**
     * Gets the time remaining until this Runnable is executed. <p> The returned Duration might not
     * be entirely exact, since Minecraft's internal clock may run faster or slower than the
     * real-world time. </p>
     *
     * @return the time remaining
     */
    public Duration getRemainingTime() {
      return new Duration(durationMillis - (System.currentTimeMillis() - startMillis),
                          TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
      BukkitTimerService.this.cancel(runnable.getTimedSuject(), runnable.getClass());
      cancelAbortingCheck();
      runnable.run();
    }

    @Override
    public void cancel() {
      BukkitTimerService.this.cancel(runnable.getTimedSuject(), runnable.getClass());
      cancelAbortingCheck();
      super.cancel();
    }

    /**
     * Cancels the abortion check, if any.
     */
    private void cancelAbortingCheck() {
      if (abortingCheck != null) {
        abortingCheck.cancel();
      }
    }

  }

}

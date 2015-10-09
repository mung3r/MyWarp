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

package me.taylorkelly.mywarp.timer;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;

/**
 * Manages timers on objects. <p>Typically an implementation is provided by the platform running MyWarp.</p>
 */
public interface TimerService {

  /**
   * Starts a timer with the given TimerAction and the given Duration on the given subject.
   *
   * @param <T>          the type of the subject the timer runs on
   * @param timedSubject the instance the timer runs on
   * @param duration     the Duration
   * @param action       the TimerAction
   */
  <T> void start(T timedSubject, Duration duration, TimerAction<T> action);

  /**
   * Returns whether the given subject has a running timer of the given Class.
   *
   * @param timedSubject the instance the timer runs on
   * @param clazz        the Class
   * @return true if the instance has a running timer
   */
  EvaluationResult has(Object timedSubject, @SuppressWarnings("rawtypes") Class<? extends TimerAction> clazz);

  /**
   * Cancels the timer of the given Class that runs on the given instance directly, if any.
   *
   * @param timedSubject the instance the timer runs on
   * @param clazz        the Class
   */
  void cancel(Object timedSubject, @SuppressWarnings("rawtypes") Class<? extends TimerAction> clazz);

  /**
   * The result of an evaluation that checked whether a certain subject has a running timer.
   *
   * @see TimerService#has(Object, Class)
   */
  class EvaluationResult {

    /**
     * Indicates that no timer is running.
     */
    public static final EvaluationResult NO_RUNNING_TIMER = new EvaluationResult();
    private final boolean timerRunning;
    @Nullable
    private final Duration durationLeft;

    /**
     * Creates an instance that indicates that no timer is running.
     */
    private EvaluationResult() {
      this.timerRunning = false;
      this.durationLeft = null;
    }

    /**
     * Creates an instance indicating that a timer is running. Use {@link #NO_RUNNING_TIMER} to get an instance that
     * indicates that no timer is running.
     *
     * @param durationLeft the Duration left on the running Timer
     */
    public EvaluationResult(Duration durationLeft) {
      this.timerRunning = true;
      this.durationLeft = durationLeft;
    }

    /**
     * Returns whether a timer is running.
     *
     * @return {@code true} if a timer is running
     */
    public boolean isTimerRunning() {
      return timerRunning;
    }

    /**
     * Gets the duration left on the running timer.
     *
     * @return the duration left
     * @throws IllegalStateException if no timer is running and thus {@link #isTimerRunning()} returns {@code true}
     */
    public Duration getDurationLeft() {
      checkState(timerRunning);
      return durationLeft;
    }
  }
}

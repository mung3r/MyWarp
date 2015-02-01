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

import com.google.common.base.Optional;

/**
 * Represents a service that manages timers on objects.
 */
public interface TimerService {

    /**
     * Starts a timer with the given TimerAction and the given Duration on the
     * given subject.
     * 
     * @param <T>
     *            the type of the subject the timer runs on
     * 
     * @param timedSubject
     *            the instance the timer runs on
     * @param duration
     *            the Duration
     * @param action
     *            the TimerAction
     */
    <T> void start(T timedSubject, Duration duration, TimerAction<T> action);

    /**
     * Returns whether the given subject has a running timer of the given Class.
     * 
     * @param timedSubject
     *            the instance the timer runs on
     * @param clazz
     *            the Class
     * @return true if the instance has a running timer
     */
    EvaluationResult has(Object timedSubject, @SuppressWarnings("rawtypes") Class<? extends TimerAction> clazz);

    /**
     * Cancels the timer of the given Class that runs on the given instance
     * directly, if any.
     * 
     * @param timedSubject
     *            the instance the timer runs on
     * @param clazz
     *            the Class
     */
    void cancel(Object timedSubject, @SuppressWarnings("rawtypes") Class<? extends TimerAction> clazz);

    /**
     * The result of an evaluation that checked whether a certain subject has a
     * running timer.
     * 
     * @see TimerService#has(Object, Class)
     */
    public static class EvaluationResult {
        private final boolean timerRunning;
        private final Optional<Duration> durationLeft;

        /**
         * Indicates that no timer is running.
         */
        public static final EvaluationResult NO_RUNNING_TIMER = new EvaluationResult();

        /**
         * Creates an instance that indicates that no timer is running.
         */
        private EvaluationResult() {
            this.timerRunning = false;
            this.durationLeft = Optional.absent();
        }

        /**
         * Creates an instance. Use {@link #NO_RUNNING_TIMER} to get an instance
         * that indicates that no timer is running.
         * 
         * @param timerRunning
         *            whether a timer is running
         * @param durationLeft
         *            the Duration left on the running Timer
         */
        public EvaluationResult(boolean timerRunning, Duration durationLeft) {
            this.timerRunning = timerRunning;
            this.durationLeft = Optional.of(durationLeft);
        }

        /**
         * Returns whether a timer is running.
         *
         * @return true if a timer is running
         */
        public boolean isTimerRunning() {
            return timerRunning;
        }

        /**
         * Gets an Optional containing the duration that is left on the running
         * timer. {@link Optional#absent()} will be returned if, and only if no
         * timer is running and {@link #isTimerRunning()} returns {@code true}.
         *
         * @return the Duration left
         */
        public Optional<Duration> getDurationLeft() {
            return durationLeft;
        }
    }
}

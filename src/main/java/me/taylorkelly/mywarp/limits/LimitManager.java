/*
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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
package me.taylorkelly.mywarp.limits;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.limits.Limit.Type;
import me.taylorkelly.mywarp.warp.Warp;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

/**
 * 
 */
public interface LimitManager {

    /**
     * Evaluates whether the given creator exceeds limits on the given
     * LocalWorld, testing against the given Limit.Type.
     * 
     * @param creator
     *            the creator
     * @param world
     *            the LocalWorld
     * @param type
     *            the Limit.Type
     * @param evaluateParents
     *            Whether parents of the given {@code type} will be evaluated
     *            too (recursively). This is usefull if not just a specific
     *            Limit.Type might be exceeded, but also a more general limit
     *            that includes the given one
     * @return an EvaluationResult representing the result of the evaluation
     */
    LimitManager.EvaluationResult evaluateLimit(LocalPlayer creator, LocalWorld world, Limit.Type type,
            boolean evaluateParents);

    /**
     * Gets all warps created by the given LocalPlayer mapped under the
     * applicable {@link Limit}. A warp may be mapped multiple times.
     * 
     * @param creator
     *            the creator
     * @return a Multimap with all matching warps
     */
    Multimap<Limit, Warp> getWarpsPerLimit(LocalPlayer creator);

    /**
     * The result of a limit evaluation.
     * 
     * @see LimitManager#evaluateLimit(LocalPlayer, LocalWorld, Type, boolean)
     */
    public static class EvaluationResult {
        private final boolean exceedsLimit;
        private final Optional<Limit.Type> exceededLimit;
        private final Optional<Integer> limitMaximum;

        /**
         * Indicates that no limit was exceeded.
         */
        public static final EvaluationResult LIMIT_MEAT = new EvaluationResult();

        /**
         * Creates an instance that indicates that all limits were meat.
         */
        private EvaluationResult() {
            this.exceedsLimit = false;
            this.exceededLimit = Optional.absent();
            this.limitMaximum = Optional.absent();
        }

        /**
         * Creates an instance. Use {@link #LIMIT_MEAT} to get an instance that
         * indicates that all limits are meat.
         * 
         * @param exceedsLimit
         *            whether a limit was exceeded
         * @param exceededLimit
         *            the exceeded limit - can be {@code  null} if no limit was
         *            exceeded
         * @param limitMaximum
         *            the maximum number of warps a user can create under the
         *            exceeded limit - can be {@code  null} if no limit was
         *            exceeded
         */
        public EvaluationResult(boolean exceedsLimit, Type exceededLimit, int limitMaximum) {
            this.exceedsLimit = exceedsLimit;
            this.exceededLimit = Optional.of(exceededLimit);
            this.limitMaximum = Optional.of(limitMaximum);
        }

        /**
         * Returns whether a limit is exceeded.
         *
         * @return true if a limit is exceeded
         */
        public boolean exceedsLimit() {
            return exceedsLimit;
        }

        /**
         * Gets an Optional containing the exceeded limit. Returns
         * {@link Optional#absent()} if, and only if no limit is exceeded and
         * thus {@link #exceedsLimit()} returns {@code true}.
         * 
         * @return the exceeded limit
         */
        @Nullable
        public Optional<Limit.Type> getExceededLimit() {
            return exceededLimit;
        }

        /**
         * Gets the maximum number of warps a user can create under the exceeded
         * limit. Returns @link {@link Optional#absent()} if, and only if no
         * limit is exceeded and thus {@link #exceedsLimit()} returns
         * {@code true}.
         * 
         * @return the maximum number of warps of the exceeded limit
         */
        @Nullable
        public Optional<Integer> getLimitMaximum() {
            return limitMaximum;
        }

    }

}

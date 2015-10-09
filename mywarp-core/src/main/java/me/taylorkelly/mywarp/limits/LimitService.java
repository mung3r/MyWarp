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

package me.taylorkelly.mywarp.limits;

import static com.google.common.base.Preconditions.checkState;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.limits.Limit.Type;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Manages and evaluates warp creation limits.
 */
public interface LimitService {

  /**
   * Evaluates whether the given creator exceeds limits on the given LocalWorld, testing against the given Limit.Type.
   *
   * @param creator         the creator
   * @param world           the LocalWorld
   * @param type            the Limit.Type
   * @param evaluateParents Whether parents of the given {@code type} will be evaluated too (recursively). This is
   *                        useful if not just a specific Limit.Type might be exceeded, but also a more general limit
   *                        that includes the given one
   * @return an EvaluationResult representing the result of the evaluation
   */
  LimitService.EvaluationResult evaluateLimit(LocalPlayer creator, LocalWorld world, Limit.Type type,
                                              boolean evaluateParents);

  /**
   * Gets all warps created by the given LocalPlayer mapped under the applicable {@link Limit}. The map is guaranteed to
   * include all limits that effect the given player. If no warp exists for a certain limit, {@code get(Limit)} will
   * return an empty list.
   *
   * @param creator the creator
   * @return a Map with all matching warps
   */
  Map<Limit, List<Warp>> getWarpsPerLimit(LocalPlayer creator);

  /**
   * The result of a limit evaluation.
   *
   * @see LimitService#evaluateLimit(LocalPlayer, LocalWorld, Limit.Type, boolean)
   */
  class EvaluationResult {

    /**
     * Indicates that no limit was exceeded.
     */
    public static final EvaluationResult LIMIT_MEAT = new EvaluationResult();
    private final boolean exceedsLimit;
    @Nullable
    private final Limit.Type exceededLimit;
    @Nullable
    private final Integer limitMaximum;

    /**
     * Creates an instance that indicates that all limits were meat.
     */
    private EvaluationResult() {
      this.exceedsLimit = false;
      this.exceededLimit = null;
      this.limitMaximum = null;
    }

    /**
     * Creates an instance indicating that a limit was exceeded. Use {@link #LIMIT_MEAT} to get an instance that
     * indicates that all limits are meat.
     *
     * @param exceededLimit the exceeded limit
     * @param limitMaximum  the maximum number of warps a user can create under the exceeded limit
     */
    public EvaluationResult(Type exceededLimit, int limitMaximum) {
      this.exceedsLimit = true;
      this.exceededLimit = exceededLimit;
      this.limitMaximum = limitMaximum;
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
     * Gets an Optional containing the exceeded limit.
     *
     * @return the exceeded limit
     * @throws IllegalStateException if no limit is exceeded and thus {@link #exceedsLimit()} returns {@code true}.
     */
    public Limit.Type getExceededLimit() {
      checkState(exceededLimit != null);
      return exceededLimit;
    }

    /**
     * Gets the maximum number of warps a user can create under the exceeded limit.
     *
     * @return the maximum number of warps of the exceeded limit
     * @throws IllegalStateException if no limit is exceeded and thus {@link #exceedsLimit()} returns {@code true}.
     */
    public Integer getLimitMaximum() {
      checkState(limitMaximum != null);
      return limitMaximum;
    }

  }

}

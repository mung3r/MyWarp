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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.IterableUtils;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Manages {@link Limit}s.
 * <p>
 * The SimpleLimitManager operates on a {@link LimitProvider} that provides the
 * actual limits and a {@link WarpManager} that holds the warps the limits apply
 * on.
 * </p>
 */
public class SimpleLimitManager implements LimitManager {

    private final LimitProvider provider;
    private final WarpManager manager;

    /**
     * Initializes this SimpleLimitManager acting on the given LimitProvider and
     * the given WarpManager.
     * 
     * @param provider
     *            the LimitProvider
     * @param manager
     *            the WarpManager
     */
    public SimpleLimitManager(LimitProvider provider, WarpManager manager) {
        this.provider = provider;
        this.manager = manager;
    }

    @Override
    public LimitManager.EvaluationResult evaluateLimit(LocalPlayer creator, LocalWorld world,
            Limit.Type type, boolean evaluateParents) {
        if (!type.canDisobey(creator, world)) {

            Iterable<Warp> filteredWarps = manager.filter(WarpUtils.isCreator(creator.getProfile()));
            Limit limit = provider.getLimit(creator, world);

            List<Limit.Type> limitsToCheck = Arrays.asList(type);
            if (evaluateParents) {
                limitsToCheck.addAll(type.getParentsRecusive());
            }

            for (Limit.Type parent : limitsToCheck) {
                LimitManager.EvaluationResult result = evaluateLimit(limit, parent, filteredWarps);
                if (result.exceedsLimit()) {
                    return result;
                }
            }
        }
        return LimitManager.EvaluationResult.LIMIT_MEAT;
    }

    /**
     * Evaluates whether the given Limit.Type of the given Limit is exceeded in
     * the given Iterable of warps. The Iterable will be overwritten and only
     * include the warps matching the given types condition.
     * 
     * @param limit
     *            the limit
     * @param type
     *            the type
     * @param filteredWarps
     *            the warps
     * @return an EvaluationResult representing the result of the evaluation
     */
    private LimitManager.EvaluationResult evaluateLimit(Limit limit, Limit.Type type,
            Iterable<Warp> filteredWarps) {
        filteredWarps = Iterables.filter(filteredWarps, type.getCondition());
        int limitMaximum = limit.getLimit(type);
        if (IterableUtils.atLeast(filteredWarps, limitMaximum)) {
            return new LimitManager.EvaluationResult(true, type, limitMaximum);
        }
        return LimitManager.EvaluationResult.LIMIT_MEAT;
    }

    @Override
    public Multimap<Limit, Warp> getWarpsPerLimit(LocalPlayer creator) {
        Collection<Warp> warps = manager.filter(WarpUtils.isCreator(creator.getProfile()));
        ImmutableMultimap.Builder<Limit, Warp> builder = ImmutableMultimap.builder();

        // sort warps to limits
        for (Warp warp : warps) {
            for (Limit limit : provider.getEffectiveLimits(creator)) {
                if (limit.isAffectedWorld(warp.getWorld())) {
                    builder.put(limit, warp);
                }
            }
        }

        return builder.build();
    }

}

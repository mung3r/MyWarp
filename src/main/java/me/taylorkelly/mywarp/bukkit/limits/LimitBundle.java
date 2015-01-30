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
package me.taylorkelly.mywarp.bukkit.limits;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.bukkit.BukkitAdapter;
import me.taylorkelly.mywarp.bukkit.permissions.bundles.ValueBundle;
import me.taylorkelly.mywarp.limits.Limit;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.google.common.collect.ImmutableSet;

/**
 * A ValueBundle that bundles limits.
 */
public class LimitBundle extends ValueBundle implements Limit {

    private final Map<Type, Integer> limitMap = new EnumMap<Limit.Type, Integer>(Type.class);
    @Nullable
    private final ImmutableSet<LocalWorld> affectedWorlds;
    private final BukkitAdapter adapter;

    /**
     * Initializes this bundle as global.
     * 
     * @param identifier
     *            the unique identifier
     * @param totalLimit
     *            the total limit
     * @param publicLimit
     *            the public limit
     * @param privateLimit
     *            the private limit
     * @param adapter
     *            the adapter
     */
    public LimitBundle(String identifier, int totalLimit, int publicLimit, int privateLimit,
            BukkitAdapter adapter) {
        this(identifier, totalLimit, publicLimit, privateLimit, null, adapter);
    }

    /**
     * Initializes this bundle.
     * 
     * @param identifier
     *            the unique identifier
     * @param totalLimit
     *            the total limit
     * @param publicLimit
     *            the public limit
     * @param privateLimit
     *            the private limit
     * @param affectedWorlds
     *            an Iterable of worlds affected by this limit. Can be
     *            {@code null} if this limit should affect all worlds.
     * @param adapter
     *            the adapter
     */
    public LimitBundle(String identifier, int totalLimit, int publicLimit, int privateLimit,
            @Nullable Iterable<LocalWorld> affectedWorlds, BukkitAdapter adapter) {
        super(identifier, "mywarp.limit"); // NON-NLS
        this.adapter = adapter;

        if (affectedWorlds != null) {
            this.affectedWorlds = ImmutableSet.copyOf(affectedWorlds);
        } else {
            this.affectedWorlds = null;
        }
        limitMap.put(Type.TOTAL, totalLimit);
        limitMap.put(Type.PUBLIC, publicLimit);
        limitMap.put(Type.PRIVATE, privateLimit);
    }

    @Override
    public int getLimit(Type type) {
        return limitMap.get(type);
    }

    @Override
    public ImmutableSet<LocalWorld> getAffectedWorlds() {
        ImmutableSet<LocalWorld> ret = affectedWorlds;
        if (ret == null) {
            // bundle is global
            ImmutableSet.Builder<LocalWorld> builder = ImmutableSet.builder();
            for (World bukkitWorld : Bukkit.getWorlds()) {
                builder.add(adapter.adapt(bukkitWorld));
            }
            ret = builder.build();
        }
        return ret;
    }

    @Override
    public boolean isAffectedWorld(LocalWorld world) {
        return affectedWorlds == null || affectedWorlds.contains(world);
    }

}

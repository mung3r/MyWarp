/**
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
package me.taylorkelly.mywarp.permissions.valuebundles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.World;

import com.google.common.collect.ImmutableSet;

/**
 * A value-bundle implementation that allows storing values for a set of
 * different worlds instead of globally.
 */
public abstract class MultiworldValueBundle extends AbstractValueBundle {

    protected final ImmutableSet<String> affectedWorlds;

    /**
     * Initializes this bundle with the given identifier and the given world
     * names. The bundle will only affect the given worlds.
     * 
     * @param identifier
     *            the identifier
     * @param worldNames
     *            an iterable containing the names of worlds that the bundle
     *            should affect. Can be <code>null</code> to affect all worlds.
     */
    public MultiworldValueBundle(String identifier, @Nullable Iterable<String> worldNames) {
        super(identifier);
        if (worldNames != null) {
            affectedWorlds = ImmutableSet.copyOf(worldNames);
        } else {
            // the limit is global!
            affectedWorlds = null;
        }
    }

    /**
     * Gets a list of all worlds that are affected by this bundle.
     * 
     * @return a list with all affected worlds
     */
    public List<World> getAffectedWorlds() {
        if (isGlobal()) {
            return MyWarp.server().getWorlds();
        }
        List<World> ret = new ArrayList<World>();
        for (String worldName : affectedWorlds) {
            ret.add(MyWarp.server().getWorld(worldName));
        }
        return ret;
    }

    /**
     * Returns whether this bundle affects all worlds.
     * 
     * @return true if this bundle affects all worlds
     */
    public boolean isGlobal() {
        return affectedWorlds == null;
    }

    /**
     * Returns whether the given world is affected by this bundle.
     * 
     * @param world
     *            the world to check
     * @return true if the given world is affected
     */
    public boolean isAffectedWorld(World world) {
        return isGlobal() || affectedWorlds.contains(world.getName());
    }
}

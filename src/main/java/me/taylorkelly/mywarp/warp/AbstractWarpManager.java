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
package me.taylorkelly.mywarp.warp;

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.MatchList;
import me.taylorkelly.mywarp.util.profile.Profile;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * An abstract {@link WarpManager} implementation that implements 'matching'
 * methods that do not depend on the implementation.
 */
public abstract class AbstractWarpManager implements WarpManager {

    @Override
    public Optional<Profile> getMatchingCreator(String filter, Predicate<Warp> predicate) {
        Profile ret = null;
        for (Warp warp : filter(predicate)) {
            Profile creator = warp.getCreator();

            Optional<String> creatorName = creator.getName();
            if (!creatorName.isPresent()) {
                // the name cannot be resolved so we cannot do any matching
                continue;
            }
            if (StringUtils.equalsIgnoreCase(creatorName.get(), filter)) {
                // minecraft names are, as of 1.7.x case insensitive
                return Optional.of(creator);
            }
            if (StringUtils.containsIgnoreCase(creatorName.get(), filter)) {
                if (ret != null) {
                    // no clear match so there is no point in continuing
                    return Optional.absent();
                }
                ret = creator;
            }
        }
        return Optional.fromNullable(ret);
    }

    @Override
    public Optional<LocalWorld> getMatchingWorld(String filter, Predicate<Warp> predicate) {
        LocalWorld ret = null;
        for (Warp warp : filter(predicate)) {
            LocalWorld world = warp.getWorld();
            if (world.getName().equals(filter)) {
                return Optional.of(world);
            }
            if (StringUtils.containsIgnoreCase(world.getName(), filter)) {
                if (ret != null) {
                    // no clear match so there is no point in continuing
                    return Optional.absent();
                }
                ret = world;
            }
        }
        return Optional.fromNullable(ret);
    }

    @Override
    public MatchList getMatchingWarps(String filter, Predicate<Warp> predicate) {
        return new MatchList(filter, filter(predicate));
    }
}

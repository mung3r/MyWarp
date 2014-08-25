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

import java.util.HashSet;
import java.util.Set;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.permissions.PermissionsManager;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * A value-bundle-manager implementation that manages
 * {@link MultiworldValueBundle}s.
 * 
 * @see ValueBundleManager
 * 
 * @param <T>
 *            the value-bundle implementation this manager should manage
 */
public class MultiworldValueBundleManager<T extends MultiworldValueBundle> extends ValueBundleManager<T> {

    /**
     * Initializes this multiworld-value-bundle manager with the given bundles.
     * 
     * @param manager
     *            the permissions-manager that stores this value-bundle-manager
     * @param bundles
     *            all non-default bundles that a player can optionally have
     * @param defaultBundle
     *            the default bundle that acts as a fallback whenever none of
     *            the more specific bundles applies. This bundle is enforced to
     *            be global.
     * @throws IllegalArgumentException
     *             if the defaultBundle is not global.
     */
    public MultiworldValueBundleManager(PermissionsManager manager, Iterable<T> bundles, T defaultBundle)
            throws IllegalArgumentException {
        super(manager, bundles, defaultBundle);
        Preconditions.checkArgument(defaultBundle.isGlobal(), "The given defaultBundle %s is not global.",
                defaultBundle);

    }

    /**
     * Gets the bundle that applies for the given player in his current
     * location. If none of the specific bundles matches, this method will
     * return the defaultBundle.
     * 
     * @param player
     *            the player
     * @return the bundle that applies for the given player
     */
    public T getBundle(Player player) {
        return getBundle(player, player.getWorld());
    }

    /**
     * Gets the bundle that applies for the given player on the given world. If
     * none of the specific bundles matches, this method will return the
     * defaultBundle.
     * 
     * @param player
     *            the player
     * @param world
     *            the world
     * @return the bundle that applies for the given player on the given world
     */
    public T getBundle(Player player, World world) {
        for (T bundle : bundles) {
            if (bundle.isAffectedWorld(world)
                    && MyWarp.inst().getPermissionsManager().hasPermission(player, bundle.getPermission())) {
                return bundle;
            }
        }
        return defaultBundle;
    }

    /**
     * Gets all bundles that affect or could affect the given player, depending
     * on his location.
     * 
     * @param player
     *            the player
     * @return all bundles that could affect the given player
     */
    public ImmutableList<T> getAffectiveBundles(Player player) {
        Builder<T> ret = ImmutableList.builder();
        Set<World> worlds = new HashSet<World>();
        for (T bundle : bundles) {
            if (!MyWarp.inst().getPermissionsManager().hasPermission(player, bundle.getPermission())) {
                continue;
            }
            if (worlds.containsAll(bundle.getAffectedWorlds())) {
                // the affective bundles already cover all worlds that this
                // bundle covers, so it is effectively overwritten.
                continue;
            }
            ret.add(bundle);
            worlds.addAll(bundle.getAffectedWorlds());
        }
        // if there is a world that is not covered by all bundles, the default
        // bundle (always global) is needed
        if (!worlds.containsAll(defaultBundle.getAffectedWorlds())) {
            ret.add(defaultBundle);
        }
        return ret.build();
    }
}

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

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.permissions.PermissionsManager;

import org.bukkit.command.CommandSender;

/**
 * A simple value-bundle-manager implementation: bundles apply only when a
 * command-sender has the corresponding permission.
 * 
 * @see ValueBundleManager
 * 
 * @param <T>
 *            the value-bundle implementation this manager should manage
 */
public class SimpleValueBundleManager<T extends ValueBundle> extends ValueBundleManager<T> {

    /**
     * Initializes this value-bundle manager with the given bundles.
     * 
     * @param manager
     *            the permissions-manager that stores this value-bundle-manager
     * @param bundles
     *            all non-default bundles that a player can optionally have
     * @param defaultBundle
     *            the default bundle that acts as a fallback whenever none of
     *            the more specific bundles applies
     */
    public SimpleValueBundleManager(PermissionsManager manager, Iterable<T> bundles, T defaultBundle) {
        super(manager, bundles, defaultBundle);
    }

    /**
     * Gets the bundle that applies for the given command-sender. If none of the
     * specific bundles matches, this method will return the defaultBundle.
     * 
     * @param sender
     *            the command-sender
     * @return the bundle that applies for the given sender
     */
    public T getBundle(CommandSender sender) {
        for (T bundle : bundles) {
            if (MyWarp.inst().getPermissionsManager().hasPermission(sender, bundle.getPermission())) {
                return bundle;
            }
        }
        return defaultBundle;
    }
}

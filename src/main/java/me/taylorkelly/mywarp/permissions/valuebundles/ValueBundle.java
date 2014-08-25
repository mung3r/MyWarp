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

/**
 * A bundle for various values that are assigned all together via a permission.
 */
public interface ValueBundle extends Comparable<ValueBundle> {

    /**
     * Gets the bundles identifier.
     * 
     * @return the identifier
     */
    String getIdentifier();

    /**
     * Gets the full permission that a user needs to have to have the bundled
     * values.
     * 
     * @return the full permission of this bundle
     */
    String getPermission();

}

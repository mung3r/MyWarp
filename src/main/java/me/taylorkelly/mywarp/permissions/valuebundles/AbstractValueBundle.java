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
 * An abstract implementation of a value-bundle.
 */
public abstract class AbstractValueBundle implements ValueBundle {

    private final String identifier;

    /**
     * Initializes this bundle with the given identifier. The identifier should
     * be unique.
     * 
     * @param identifier
     *            the identifier
     */
    public AbstractValueBundle(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public int compareTo(ValueBundle that) {
        return this.getIdentifier().compareTo(that.getIdentifier());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * me.taylorkelly.mywarp.permissions.valuebundles.ValueBundle#getIdentifier
     * ()
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the base-permission of this bundle. The base-permission is identical
     * for a set of bundles that set the same value-types. It is suffixed by a
     * bundles identifier to get the full permission that represents an
     * individual bundle.
     * 
     * @return the base-permission
     */
    protected abstract String getBasePermission();

    /*
     * (non-Javadoc)
     * 
     * @see
     * me.taylorkelly.mywarp.permissions.valuebundles.ValueBundle#getPermission
     * ()
     */
    @Override
    public String getPermission() {
        return getBasePermission() + "." + identifier;
    }

}

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
package me.taylorkelly.mywarp.safety;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Provides and manages methods to determine if a certain location is safe.
 */
public final class BlockSafety {

    /**
     * Block initialization of this class.
     */
    private BlockSafety() {
    }

    /**
     * Test if the given location is safe.
     * 
     * @param loc
     *            the location
     * @return True if the location is safe
     */
    public static boolean isLocationSafe(Location loc) {
        Location upOne = loc.clone().add(0, 1, 0);
        Location downOne = loc.clone().add(0, -1, 0);

        if (loc.getBlock().getType().isSolid() || upOne.getBlock().getType().isSolid()) {
            return false;
        }
        if (!(downOne.getBlock().getType().isSolid() || downOne.getBlock().getType() == Material.WATER)) {
            return false;
        }
        if (isUnsafeBlock(loc.getBlock().getType()) || isUnsafeBlock(upOne.getBlock().getType())) {
            return false;
        }
        if (isUnsafeBlock(downOne.getBlock().getType())) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the given material is unsafe, meaning it causes damage upon
     * contact.
     * 
     * Must be updated for new Minecraft versions if new matching blocks were
     * introduced!
     * 
     * @param type
     *            the Material to check
     * @return true if this material is unsafe
     */
    private static boolean isUnsafeBlock(Material type) {
        switch (type) {
        case LAVA:
        case STATIONARY_LAVA:
        case FIRE:
        case CACTUS:
            return true;
        default:
            return false;
        }
    }
}

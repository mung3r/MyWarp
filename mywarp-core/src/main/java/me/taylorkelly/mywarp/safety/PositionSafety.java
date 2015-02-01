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

package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.Vector3;

import com.google.common.base.Optional;

/**
 * Tests if a position in a world is safe and searches for safe positions if
 * not.
 */
public interface PositionSafety {

    /**
     * Returns whether the given position in the given world is safe.
     * 
     * @param world
     *            the world where the position is placed it
     * @param position
     *            the position vector
     * @return true if the location is safe
     */
    boolean isPositionSafe(LocalWorld world, Vector3 position);

    /**
     * Gets an Optional with the first safe position found within the given
     * tolerance, starting from the given position, if such a position exits.
     * 
     * @param world
     *            the world where the position is placed it
     * @param position
     *            the position vector
     * @param tolerance
     *            the acceptable tolerance
     * @return the first safe position
     */
    Optional<Vector3> getSafePosition(LocalWorld world, Vector3 position, int tolerance);

}

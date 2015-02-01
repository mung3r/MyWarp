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
 * Searches for safe positions within a cube centered at the starting point.
 */
public class CubicLocationSafety extends BlockSafety {

    @Override
    public Optional<Vector3> getSafePosition(LocalWorld world, Vector3 center, int tolerance) {
        if (tolerance < 0) {
            throw new IllegalArgumentException("halfEdgeLength must be greater than 0.");
        }
        if (isPositionSafe(world, center)) {
            return Optional.of(center);
        }
        Optional<Vector3> safePosition; // never modify the given location!

        for (int i = 2; i <= tolerance; i++) {
            safePosition = checkCubeSurface(world, center, i);
            if (safePosition.isPresent()) {
                return safePosition;
            }
        }
        return Optional.absent();
    }

    /**
     * Gets an Optional the first safe position from the cube surface of the
     * given half-edge-length centered at the given position in the given world,
     * if such a position exits.
     * 
     * @param world
     *            the world where the position is placed it
     * @param center
     *            the central position vector
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe location found, or null if none could be found
     */
    private Optional<Vector3> checkCubeSurface(LocalWorld world, Vector3 center, int halfEdgeLength) {
        Optional<Vector3> safePosition;

        int diameter = getEdgeLength(halfEdgeLength);
        for (int i = 0; i < diameter; i++) {
            // makes the location 'swing' up/down (+1, -2, +3, -4...)
            center = center.add(0, i % 2 == 0 ? -i : i, 0);
            if (i < diameter - 2) {
                // if we are more than 2 steps away from the ending, we are in
                // the "middle" of the cube and only need to check the outline
                safePosition = checkHorizontalSquareOutline(world, center, halfEdgeLength);
            } else {
                // check bottom and top areas
                safePosition = checkHorizontalSquare(world, center, halfEdgeLength);
            }
            if (safePosition.isPresent()) {
                return safePosition;
            }
        }
        return Optional.absent();
    }

    /**
     * Gets an Optional with the first safe position from a horizontal square
     * with the given half-edge-length centered at the given position in the
     * given world, if such a position exits.
     * 
     * @param world
     *            the world where the position is placed it
     * @param center
     *            the central position vector
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe position
     */
    private Optional<Vector3> checkHorizontalSquare(LocalWorld world, Vector3 center, int halfEdgeLength) {
        if (isPositionSafe(world, center)) {
            return Optional.of(center);
        }
        Optional<Vector3> checkPosition;

        // loop through surrounding blocks, starting with a half-edge-length of
        // 2 (1 would just be the central block)
        for (int i = 2; i <= halfEdgeLength; i++) {
            checkPosition = checkHorizontalSquareOutline(world, center, i);
            if (checkPosition.isPresent()) {
                return checkPosition;
            }
        }
        return Optional.absent();
    }

    /**
     * Gets an Optional with the first safe position from the outline of
     * horizontal square with the given half-edge-length centered at the given
     * position in the given world, if such a position exits.
     * 
     * @param world
     *            the world where the position is placed it
     * @param center
     *            the central position vector
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe position
     */
    private Optional<Vector3> checkHorizontalSquareOutline(LocalWorld world, Vector3 center,
            int halfEdgeLength) {
        int blockSteps = getEdgeLength(halfEdgeLength) - 1;
        Vector3 checkPosition = center.add(halfEdgeLength - 1, 0, halfEdgeLength - 1);

        for (int i = 0; i < blockSteps; i++) {
            checkPosition = checkPosition.add(-1, 0, 0);
            if (isPositionSafe(world, checkPosition)) {
                return Optional.of(checkPosition);
            }
        }

        for (int i = 0; i < blockSteps; i++) {
            checkPosition = checkPosition.add(0, 0, -1);
            if (isPositionSafe(world, checkPosition)) {
                return Optional.of(checkPosition);
            }
        }

        for (int i = 0; i < blockSteps; i++) {
            checkPosition = checkPosition.add(1, 0, 0);
            if (isPositionSafe(world, checkPosition)) {
                return Optional.of(checkPosition);
            }
        }
        for (int i = 0; i < blockSteps; i++) {
            checkPosition = checkPosition.add(0, 0, 1);
            if (isPositionSafe(world, checkPosition)) {
                return Optional.of(checkPosition);
            }
        }
        return Optional.absent();
    }

    /**
     * Gets the edge length of a square with the given half-edge-length. The
     * later is expected to include the block at the center, e.g. the
     * half-edge-length '2' would result in a edge-length of '3'.
     * 
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the edge length
     */
    private int getEdgeLength(int halfEdgeLength) {
        return (halfEdgeLength - 1) * 2 + 1;
    }

}

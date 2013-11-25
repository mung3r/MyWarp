package me.taylorkelly.mywarp.safety;

import org.bukkit.Location;

/**
 * Provides an algorithm to search for a safe-location.
 */
public class SafeLocation {

    /**
     * Gets the first safe location found in a cube of the given
     * half-edge-length centered at the given location.
     * 
     * If the given location is not safe, the algorithm will loop through all
     * blocks that directly surround it, than through all blocks surrounding
     * these and so one.
     * 
     * @param center
     *            the center
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe location found, or null if none could be found
     */
    public static Location getSafeLocation(final Location center, int halfEdgeLength) {
        if (halfEdgeLength < 0) {
            throw new IllegalArgumentException("halfEdgeLength must be greater than 0.");
        }
        if (BlockSafety.isLocationSafe(center)) {
            return center;
        }
        Location safe; // never modify the given location!

        for (int i = 2; i <= halfEdgeLength; i++) {
            safe = checkCubeSurface(center, i);
            if (safe != null) {
                return safe;
            }
        }
        return null;
    }

    /**
     * Gets the first safe location from the cube surface of the given
     * half-edge-length centered at the given location.
     * 
     * @param center
     *            the center
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe location found, or null if none could be found
     */
    private static Location checkCubeSurface(final Location center, int halfEdgeLength) {
        Location safe; // never modify the given location!

        int diameter = getEdgeLength(halfEdgeLength);
        for (int i = 0; i < diameter; i++) {
            // makes the location 'swing' up/down (+1, -2, +3, -4...)
            center.add(0, i % 2 == 0 ? -i : i, 0);
            if (i < diameter - 2) {
                // if we are more than 2 steps away from the ending, we are in
                // the "middle" of the cube and only need to check the outline
                safe = checkHorizontalSquareOutline(center, halfEdgeLength);
            } else {
                // check bottom and top areas
                safe = checkHorizontalSquare(center, halfEdgeLength);
            }
            if (safe != null) {
                return safe;
            }
        }
        return null;
    }

    /**
     * Gets the first safe location from a horizontal square with the given
     * half-edge-length centered at the given location.
     * 
     * @param center
     *            the center
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe location found, or null if none could be found
     */
    private static Location checkHorizontalSquare(final Location center, int halfEdgeLength) {
        if (BlockSafety.isLocationSafe(center)) {
            return center;
        }
        Location safe; // never modify the given location!

        // loop through surrounding blocks, starting with a half-edge-length of
        // 2 (1 would just be the central block)
        for (int i = 2; i <= halfEdgeLength; i++) {
            safe = checkHorizontalSquareOutline(center, i);
            if (safe != null) {
                return safe;
            }
        }
        return null;
    }

    /**
     * Gets the first safe location from the outline of horizontal square with
     * the given half-edge-length centered at the given location.
     * 
     * @param center
     *            the center
     * @param halfEdgeLength
     *            half of the effective edge length, including the block in the
     *            center
     * @return the first safe location found, or null if none could be found
     */
    private static Location checkHorizontalSquareOutline(final Location center, int halfEdgeLength) {
        Location checkLoc = center.clone(); // never modify the given location!

        int blockSteps = getEdgeLength(halfEdgeLength) - 1;
        checkLoc.add(halfEdgeLength - 1, 0, halfEdgeLength - 1);

        for (int i = 0; i < blockSteps; i++) {
            checkLoc.add(-1, 0, 0);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }

        for (int i = 0; i < blockSteps; i++) {
            checkLoc.add(0, 0, -1);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }

        for (int i = 0; i < blockSteps; i++) {
            checkLoc.add(1, 0, 0);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }
        for (int i = 0; i < blockSteps; i++) {
            checkLoc.add(0, 0, 1);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }
        return null;
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
    private static int getEdgeLength(int halfEdgeLength) {
        return (halfEdgeLength - 1) * 2 + 1;
    }

}

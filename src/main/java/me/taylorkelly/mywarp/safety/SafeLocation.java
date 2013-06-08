package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.Location;

/**
 * This class provides and manages methods to search for a safe location
 */
public class SafeLocation {

    /**
     * Searches for a safe location close to the given one.
     * 
     * Returns null if no safe location could be found.
     * 
     * @param l
     *            the location
     * @return a safe location or null if none could be found
     */
    public static Location getSafeLocation(Location l) {
        return getSafeLocation(l,
                MyWarp.inst().getWarpSettings().verticalTolerance, MyWarp
                        .inst().getWarpSettings().searchRadius);
    }

    /**
     * Checks if the given location is safe. If not, it calls
     * {@link #checkAboveAndBelowLocation(Location, int, int)} to search for a
     * safe location using the provided margins.
     * 
     * Returns null if no safe location could be found.
     * 
     * @param l
     *            the location that is used as center
     * @param verticalTolerance
     *            the maximal vertical tolerance
     * @param radius
     *            the maximal horizontal search radius
     * @return a safe location or null if none could be found
     */
    private static Location getSafeLocation(Location l, int tolerance,
            int radius) {
        if (BlockSafety.isLocationSafe(l)) {
            return l;
        }
        Location safe = checkAboveAndBelowLocation(l, tolerance, radius);
        if (safe != null) {
            safe.add(.5, 0, .5);
        }
        return safe;
    }

    /**
     * Searches above and below a given for a safe location. This method will
     * call {@link #checkHorizontalAroundLocation(Location, int)} with the given
     * radius using the same high as the center location. If no safe location
     * can be found, it will call it again one level above and, afterwards below
     * the center-location. This continues until a safe locatio is found or the
     * max. vertical tolerance is met.
     * 
     * Returns null if no safe location could be found.
     * 
     * @param l
     *            the location that is used as center
     * @param verticalTolerance
     *            the maximal vertical tolerance
     * @param radius
     *            the maximal horizontal search radius
     * @return a safe location or null if none could be found
     */
    private static Location checkAboveAndBelowLocation(Location l,
            int verticalTolerance, int radius) {
        Location locToCheck = l.clone();
        Location safe = checkHorizontalAroundLocation(locToCheck, radius);

        if (safe != null) {
            return safe;
        }

        for (int specVerticalTolerance = 1; specVerticalTolerance < verticalTolerance; specVerticalTolerance++) {
            locToCheck = l.clone();
            locToCheck.add(0, specVerticalTolerance, 0);
            safe = checkHorizontalAroundLocation(locToCheck, radius);

            if (safe != null) {
                return safe;
            }
            locToCheck = l.clone();
            locToCheck.subtract(0, specVerticalTolerance, 0);
            safe = checkHorizontalAroundLocation(locToCheck, radius);
            if (safe != null) {
                return safe;
            }
        }
        return null;
    }

    /**
     * Searches horizontal around a given location for a safe location within
     * the search radius. This method will than call
     * {@link #checkHorizontalAroundDiameter(Location, int)} with different
     * diameters until the given max. search radius is met (for reference, the
     * maximal diameter is always twice the radius).
     * 
     * Returns null if no safe location could be found.
     * 
     * @param l
     *            the location that is used as center
     * @param radius
     *            the maximal search radius
     * @return a safe location or null if none could be found
     */
    private static Location checkHorizontalAroundLocation(Location l, int radius) {
        int maxDiameter = radius * 2;
        for (int specDiameter = 3; specDiameter < maxDiameter; specDiameter += 2) {
            Location safeLocation = checkHorizontalAroundDiameter(l,
                    specDiameter);
            if (safeLocation != null) {
                return safeLocation;
            }
        }
        return null;
    }

    /**
     * Searches for a safe location around the given location. The given one is
     * used as center, and the method loops through all blocks that surround
     * this location. This method will only loop through the most distant blocks
     * within the given diameter!
     * 
     * Returns null if no safe location could be found.
     * 
     * @param l
     *            the location that is used as center
     * @param diameter
     *            the diameter in which to search
     * @return a safe location or null if none could be found
     */
    private static Location checkHorizontalAroundDiameter(Location l,
            int diameter) {
        Location checkLoc = l.clone();

        int blockStep = (diameter - 1) / 2;
        checkLoc.add(blockStep, 0, 0);
        if (BlockSafety.isLocationSafe(checkLoc)) {
            return checkLoc;
        }

        for (int i = 0; i < diameter; i++) {
            checkLoc.add(0, 0, 1);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }

        for (int i = 0; i < diameter; i++) {
            checkLoc.add(-1, 0, 0);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }

        for (int i = 0; i < diameter; i++) {
            checkLoc.add(0, 0, -1);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }

        for (int i = 0; i < diameter; i++) {
            checkLoc.add(1, 0, -0);
            if (BlockSafety.isLocationSafe(checkLoc)) {
                return checkLoc;
            }
        }
        return null;
    }
}

package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.Location;

public class SafeLocation {

    public static Location getSafeLocation(Location l) {
        return getSafeLocation(l, WarpSettings.verticalTolerance, WarpSettings.searchRadius);
    }

    public static String getKoords(Location l) {
        return (l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ".");
    }

    private static Location getSafeLocation(Location l, int tolerance, int radius) {
        if (BlockSafety.isLocationSafe(l)) {
            return l;
        }
        Location safe = checkAboveAndBelowLocation(l, tolerance, radius);
        if (safe != null) {
            safe.add(.5, 0, .5);
        }
        return safe;
    }

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

    private static Location checkHorizontalAroundLocation(Location l, int radius) {
        int maxDiameter = radius * 2;
        for (int specDiameter = 3; specDiameter < maxDiameter; specDiameter += 2) {
            Location safeLocation = checkHorizontalAroundDiameter(l, specDiameter);
            if (safeLocation != null) {
                return safeLocation;
            }
        }
        return null;
    }

    private static Location checkHorizontalAroundDiameter(Location l, int diameter) {
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

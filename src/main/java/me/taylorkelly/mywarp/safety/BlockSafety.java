package me.taylorkelly.mywarp.safety;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockSafety {

    /**
     * Test if the given location is safe
     * 
     * @param l the location
     * @return True if the location is safe
     */
    public static boolean isLocationSafe(Location l) {
        Location upOne = l.clone();
        Location downOne = l.clone();
        upOne.setY(upOne.getY() + 1);
        downOne.setY(downOne.getY() - 1);

        if (l.getBlock().getType().isSolid()
                || upOne.getBlock().getType().isSolid()) {
            return false;
        }
        if (isUnsafeBlock(l.getBlock().getType())
                || isUnsafeBlock(upOne.getBlock().getType())) {
            return false;
        }
        if (isUnsafeBlock(downOne.getBlock().getType())) {
            return false;
        }
        if (!downOne.getBlock().getType().isSolid()) {
            return (hasEnoughBlockOfWater(l));
        }
        return true;
    }

    private static boolean isUnsafeBlock(Material type) {
        switch (type) {
        case LAVA:
            return true;
        case STATIONARY_LAVA:
            return true;
        case FIRE:
            return true;
        case CACTUS:
            return true;
        default:
            return false;
        }
    }

    private static int heightAboveGround(Location l) {
        if (l.getBlockY() < 0) {
            return 0;
        }
        Location straightDown = l.clone();
        for (int i = 0; i < straightDown.getBlockY(); i++) {
            if (straightDown.getBlock().getType() != Material.AIR) {
                return i;
            }
            straightDown.subtract(0, 1, 0);
        }
        return 0;
    }

    private static boolean hasEnoughBlockOfWater(Location l) {
        int height = heightAboveGround(l);
        Location ground = l.clone();
        ground.subtract(0, height++, 0);
        if (height < 5) {
            if (isUnsafeBlock(ground.getBlock().getType())) {
                return false;
            }
            return true;
        }
        if (ground.getBlock().getType() == Material.WATER
                || ground.getBlock().getType() == Material.STATIONARY_WATER) {
            if (height < 17) {
                return true;
            }
            Location oneBelow = ground.clone();
            oneBelow.subtract(0, 1, 0);
            if (oneBelow.getBlock().getType() == Material.WATER
                    || oneBelow.getBlock().getType() == Material.STATIONARY_WATER) {
                if (height < 31) {
                    return true;
                }
                Location twoBelow = oneBelow.clone();
                twoBelow.subtract(0, 1, 0);
                if (twoBelow.getBlock().getType() == Material.WATER
                        || twoBelow.getBlock().getType() == Material.STATIONARY_WATER) {
                    return true;
                }
            }
        }
        return false;
    }
}

package me.taylorkelly.mywarp.safety;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Provides and manages methods to determine if a certain location is safe
 */
public class BlockSafety {

    /**
     * Test if the given location is safe
     * 
     * @param l
     *            the location
     * @return True if the location is safe
     */
    public static boolean isLocationSafe(Location l) {
        Location upOne = l.clone();
        Location downOne = l.clone();
        upOne.setY(upOne.getY() + 1);
        downOne.setY(downOne.getY() - 1);

        if (l.getBlock().getType().isSolid() || upOne.getBlock().getType().isSolid()) {
            return false;
        }
        if (isUnsafeBlock(l.getBlock().getType()) || isUnsafeBlock(upOne.getBlock().getType())) {
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

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

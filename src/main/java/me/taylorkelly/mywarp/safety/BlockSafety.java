package me.taylorkelly.mywarp.safety;

import org.bukkit.Location;
import org.bukkit.Material;

public class BlockSafety {

    public static boolean isLocationSafe(Location l) {
        Location upOne = l.clone();
        Location downOne = l.clone();
        upOne.setY(upOne.getY() + 1);
        downOne.setY(downOne.getY() - 1);

        if (isSolidBlock(l.getBlock().getType())
                || isSolidBlock(upOne.getBlock().getType())) {
            return false;
        }
        if (isUnsafeBlock(l.getBlock().getType())
                || isUnsafeBlock(upOne.getBlock().getType())) {
            return false;
        }
        if (isUnsafeBlock(downOne.getBlock().getType())) {
            return false;
        }
        if (!isSolidBlock(downOne.getBlock().getType())) {
            return (hasEnoughBlockOfWater(l));
        }
        return true;
    }

    private static boolean isSolidBlock(Material type) {
        switch (type) {
        case AIR:
            return false;
        case WATER:
            return false;
        case STATIONARY_WATER:
            return false;
        case LAVA:
            return false;
        case STATIONARY_LAVA:
            return false;
        case POWERED_RAIL:
            return false;
        case DETECTOR_RAIL:
            return false;
        case WEB:
            return false;
        case YELLOW_FLOWER:
            return false;
        case RED_ROSE:
            return false;
        case TORCH:
            return false;
        case FIRE:
            return false;
        case REDSTONE_WIRE:
            return false;
        case CROPS:
            return false;
        case SIGN_POST:
            return false;
        case WOODEN_DOOR:
            return false;
        case LADDER:
            return false;
        case RAILS:
            return false;
        case WALL_SIGN:
            return false;
        case LEVER:
            return false;
        case IRON_DOOR_BLOCK:
            return false;
        case REDSTONE_TORCH_OFF:
            return false;
        case REDSTONE_TORCH_ON:
            return false;
        case STONE_BUTTON:
            return false;
        case SNOW:
            return false;
        case SUGAR_CANE_BLOCK:
            return false;
        case PUMPKIN_STEM:
            return false;
        case MELON_STEM:
            return false;
        case VINE:
            return false;
        case NETHER_WARTS:
            return false;
        case ENDER_PORTAL:
            return false;
        case WOOD_BUTTON:
            return false;
        case CARROT:
            return false;
        case POTATO:
            return false;
        default:
            return true;
        }
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

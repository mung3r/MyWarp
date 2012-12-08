package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SafeTeleport {

    public static boolean safeTeleport(Player player, Location l, String name) {
        if (isHalfBlock(l.getBlock().getType())) {
            l.add(0, 1, 0);
        }
        if (WarpSettings.useWarpSafety) {
            Location safe = SafeLocation.getSafeLocation(l);
            if (safe == null) {
                player.sendMessage(LanguageManager.getString("safety.notFound")
                        .replaceAll("%warp%", name));
                return false;
            }
            if (safe != l) {
                warpEffect(player.getLocation());
                player.teleport(safe);
                player.sendMessage(LanguageManager.getString("safety.found")
                        .replaceAll("%warp%", name));
                return false;
            }
        }
        warpEffect(player.getLocation());

        if (WarpSettings.loadChunks
                && !l.getWorld().isChunkLoaded(l.getBlockX(), l.getBlockZ())) {
            l.getWorld().refreshChunk(l.getBlockX(), l.getBlockZ());
        }

        player.teleport(l);
        return true;
    }

    private static void warpEffect(Location loc) {
        if (WarpSettings.warpEffect) {
            loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
            loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
            loc.getWorld().playEffect(loc, Effect.SMOKE, 4);
        }
    }

    private static boolean isHalfBlock(Material type) {
        switch (type) {
        case STEP:
            return true;
        case WOOD_STEP:
            return true;
        case WOOD_STAIRS:
            return true;
        case COBBLESTONE_STAIRS:
            return true;
        case CAKE_BLOCK:
            return true;
        case BRICK_STAIRS:
            return true;
        case SMOOTH_STAIRS:
            return true;
        case NETHER_BRICK_STAIRS:
            return true;
        case SANDSTONE_STAIRS:
            return true;
        case SPRUCE_WOOD_STAIRS:
            return true;
        case BIRCH_WOOD_STAIRS:
            return true;
        case JUNGLE_WOOD_STAIRS:
            return true;
        case SKULL:
            return true;
        case FLOWER_POT:
            return true;
        default:
            return false;
        }
    }
}

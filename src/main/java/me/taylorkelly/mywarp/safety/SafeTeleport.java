package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Handles teleports
 * 
 */
public class SafeTeleport {

    /**
     * Teleports the player to the given location if it is safe. If not, it
     * searches the closest safe location and teleports the player there
     * 
     * @param player
     *            the player
     * @param l
     *            the location
     * @param name
     *            the name of the warp
     * @return True if the player could be teleported to the given location
     */
    public static boolean safeTeleport(Player player, Location l, String name) {
        if (!l.getBlock().getType().isOccluding()) {
            l.add(0, 1, 0);
        }
        if (WarpSettings.useWarpSafety) {
            Location safe = SafeLocation.getSafeLocation(l);
            if (safe == null) {
                player.sendMessage(LanguageManager.getEffectiveString(
                        "safety.notFound", "%warp%", name));
                return false;
            }
            if (safe != l) {
                teleport(player, safe);
                player.sendMessage(LanguageManager.getEffectiveString(
                        "safety.found", "%warp%", name));
                return false;
            }
        }
        teleport(player, l);
        player.teleport(l);
        return true;
    }

    private static void teleport(Player player, Location to) {
        Location from = player.getLocation();
        
        if (player.isInsideVehicle()) {
            player.getVehicle().eject();
        }

        if (WarpSettings.warpEffect) {
            from.getWorld().playEffect(from, Effect.SMOKE, 4);
            from.getWorld().playEffect(from, Effect.SMOKE, 4);
            from.getWorld().playEffect(from, Effect.SMOKE, 4);
        }
        if (WarpSettings.loadChunks
                && !to.getWorld().isChunkLoaded(to.getBlockX(), to.getBlockZ())) {
            to.getWorld().refreshChunk(to.getBlockX(), to.getBlockZ());
        }
        player.teleport(to);
    }

}

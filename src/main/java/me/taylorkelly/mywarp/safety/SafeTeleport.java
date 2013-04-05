package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Provides several static methods to handle safe teleports
 * 
 */
public class SafeTeleport {

    /**
     * Teleports the player to the given location if it is safe. If not, it
     * searches the closest safe location and teleports the player there.
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
        // warp height is always the block's Y so we may need to adjust the
        // height for blocks that are smaller than one full block (steps,
        // skulls...)
        if (isNotFullHeight(l.getBlock().getType())) {
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
        return true;
    }

    /**
     * Checks if the given material is solid AND has a material specific height
     * smaller than 1 (full block). A player might stand 'inside' such a block
     * without being stuck. 
     * 
     * It is important to differ between solid and non
     * solid: a solid block blocks a player's movement, a non solid allows
     * players to move freely through it.
     * 
     * Must be updated for new Minecraft versions if new matching blocks were
     * introduced!
     * 
     * @param type
     *            the Material to check
     * @return true if this Material is solid and is not one full block height
     */
    private static boolean isNotFullHeight(Material type) {
        switch (type) {
        case BED_BLOCK: // 26
        case STEP: // 44
        case WOOD_STAIRS: // 53
        case CHEST: // 54
        case COBBLESTONE_STAIRS: // 67
        case CAKE_BLOCK: // 92
        case TRAP_DOOR: // 96
        case BRICK_STAIRS: // 108
        case SMOOTH_STAIRS: // 109
        case NETHER_BRICK_STAIRS: // 114
        case ENCHANTMENT_TABLE: // 116
        case BREWING_STAND: // 117
        case CAULDRON: // 118
        case WOOD_STEP: // 126
        case SANDSTONE_STAIRS: // 128
        case ENDER_CHEST: // 130
        case SPRUCE_WOOD_STAIRS: // 134
        case BIRCH_WOOD_STAIRS: // 135
        case JUNGLE_WOOD_STAIRS: // 136
        case SKULL: // 144
        case TRAPPED_CHEST: // 146
        case DAYLIGHT_DETECTOR: // 151
        case QUARTZ_STAIRS: // 156
            return true;
        default:
            return false;
        }
    }

    /**
     * Teleports a player to a location. Before teleporting the player is
     * ejected if he uses any vehicle, the warp-effect is played (if enabled)
     * and chunks are loaded (if enabled and not loaded before).
     * 
     * @param player
     *            the player to teleport
     * @param to
     *            the location the player should be teleported to
     */
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

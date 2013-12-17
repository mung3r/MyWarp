package me.taylorkelly.mywarp.safety;

import java.util.ArrayList;
import java.util.List;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;

/**
 * Provides and manages several methods to teleport entity to safe locations
 */
public class SafeTeleport {

    /**
     * The individual teleport-status
     */
    public enum TeleportStatus {
        NONE, ORIGINAL_LOC, SAFE_LOC,
    };

    /**
     * Teleports the entity to the given location if it is safe. If not, it
     * searches the closest safe location and teleports the entity there.
     * 
     * @param entity
     *            the entity
     * @param l
     *            the location
     * @return The resulting {@link TeleportStatus}
     */
    public static TeleportStatus safeTeleport(Entity entity, Location l) {
        // warp height is always the block's Y so we may need to adjust the
        // height for blocks that are smaller than one full block (steps,
        // skulls...)
        if (isNotFullHeight(l.getBlock().getType())) {
            l.add(0, 1, 0);
        }
        if (MyWarp.inst().getWarpSettings().safetyEnabled) {
            Location safe = SafeLocation.getSafeLocation(l,
                    MyWarp.inst().getWarpSettings().safetySearchRadius);
            if (safe == null) {
                return TeleportStatus.NONE;
            }
            if (safe != l) {
                teleport(entity, safe);
                return TeleportStatus.SAFE_LOC;
            }
        }
        teleport(entity, l);
        return TeleportStatus.ORIGINAL_LOC;
    }

    /**
     * Checks if the given material is solid AND has a material specific height
     * smaller than 1 (full block). An entity might stand 'inside' such a block
     * without being stuck.
     * 
     * It is important to differ between solid and non solid: a solid block
     * blocks an entity's movement, a non solid allows entity to move freely
     * through it.
     * 
     * Must be updated for new Minecraft versions if new matching blocks were
     * introduced!
     * 
     * @param type
     *            the Material to check
     * @return true if this Material is solid and lower than one block
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
        case ACACIA_STAIRS: //163
        case DARK_OAK_STAIRS: //164
            return true;
        default:
            return false;
        }
    }

    /**
     * Teleports an entity to a location. Before teleporting, the entity is
     * ejected if needed, the warp-effect is played (if enabled), chunks are
     * loaded (if enabled and not loaded before) and leashed or ridden entities
     * are teleported (if enabled).
     * 
     * @param entity
     *            the entity to teleport
     * @param to
     *            the location the entity should be teleported to
     */
    private static void teleport(final Entity entity, final Location to) {
        Location from = entity.getLocation();

        // teleport horses if enabled
        Entity vehicle = null;
        if (MyWarp.inst().getWarpSettings().safetyTeleportHorses && entity.getVehicle() instanceof Horse
                && ((Horse) entity.getVehicle()).isTamed()) {
            vehicle = entity.getVehicle();
        }
        entity.leaveVehicle();

        // teleport leashed entities if enabled
        List<LivingEntity> leashedEntities = null;
        if (MyWarp.inst().getWarpSettings().safetyTeleportLeashed) {
            leashedEntities = new ArrayList<LivingEntity>();
            for (Entity leashed : entity.getNearbyEntities(10, 7, 10)) {
                if (!(leashed instanceof LivingEntity)) {
                    continue;
                }
                LivingEntity leashedEntity = (LivingEntity) leashed;
                if (!leashedEntity.isLeashed() || leashedEntity.getLeashHolder() != entity) {
                    continue;
                }
                leashedEntities.add(leashedEntity);
            }
        }

        if (MyWarp.inst().getWarpSettings().warpEffect) {
            from.getWorld().playEffect(from, Effect.SMOKE, 4);
            from.getWorld().playEffect(from, Effect.SMOKE, 4);
            from.getWorld().playEffect(from, Effect.SMOKE, 4);
        }
        if (MyWarp.inst().getWarpSettings().loadChunks
                && !to.getWorld().isChunkLoaded(to.getBlockX(), to.getBlockZ())) {
            to.getWorld().refreshChunk(to.getBlockX(), to.getBlockZ());
        }
        entity.teleport(to);

        if (vehicle != null) {
            teleport(vehicle, to);
            vehicle.setPassenger(entity);
        }

        if (leashedEntities != null && !leashedEntities.isEmpty()) {
            for (LivingEntity leashedEntity : leashedEntities) {
                teleport(leashedEntity, to);
                leashedEntity.setLeashHolder(entity);
            }
        }
    }
}

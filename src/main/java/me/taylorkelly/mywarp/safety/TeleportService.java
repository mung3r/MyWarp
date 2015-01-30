/*
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
package me.taylorkelly.mywarp.safety;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;

import com.google.common.base.Optional;

/**
 * A service provider for entity teleports.
 */
public final class TeleportService {

    private final PositionSafety positionSafety = new CubicLocationSafety();

    /**
     * The status of a teleport.
     */
    public enum TeleportStatus {
        /**
         * The entity has not been teleported, e.g. because no safe location in
         * the given margins could be found.
         */
        NONE,
        /**
         * The entity has been teleported to the original location.
         */
        ORIGINAL_LOC,
        /**
         * The entity has been teleported, but to a safe location within the
         * given margins.
         */
        SAFE_LOC,
    }

    /**
     * Teleports the entity to the given position in the given world and sets
     * his rotation to the given one if the position is safe. If not, it
     * searches the closest safe position and teleports the entity there.
     * 
     * @param entity
     *            the entity to teleport
     * @param world
     *            the world where the position is placed it
     * @param position
     *            the position vector
     * @param rotation
     *            the rotation
     * @return The resulting {@link TeleportStatus}
     */
    public TeleportStatus safeTeleport(LocalEntity entity, LocalWorld world, Vector3 position,
            EulerDirection rotation) {
        // warp height is always the block's Y so we may need to adjust the
        // height for blocks that are smaller than one full block (steps,
        // skulls...)
        if (world.getBlock(position).isNotFullHeight()) {
            position = position.add(0, 1, 0);
        }
        if (MyWarp.getInstance().getSettings().isSafetyEnabled()) {
            Optional<Vector3> safePosition = positionSafety.getSafePosition(world, position, MyWarp
                    .getInstance().getSettings().getSafetySearchRadius());
            if (!safePosition.isPresent()) {
                return TeleportStatus.NONE;
            }
            if (!position.equals(safePosition.get())) {
                entity.teleport(world, safePosition.get(), rotation);
                return TeleportStatus.SAFE_LOC;
            }
        }
        entity.teleport(world, position, rotation);
        return TeleportStatus.ORIGINAL_LOC;
    }
}

package me.taylorkelly.mywarp.teleport;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;

/**
 * Provides a managed way to teleport an entity. Implementations may call additional validation before, or call
 * additional callback after a teleport is executed.
 * <p>For raw teleports use {@link LocalEntity#teleport(LocalWorld, Vector3, EulerDirection)}.</p>
 */
public interface TeleportManager {

  /**
   * The status of a teleport.
   */
  enum TeleportStatus {
    /**
     * The entity has not been teleported.
     */
    NONE(false),
    /**
     * The entity has been teleported to desired position.
     */
    ORIGINAL(true),
    /**
     * The entity has been teleported, but the position is not equal to the desired one.
     */
    MODIFIED(true);

    private final boolean positionModified;

    TeleportStatus(boolean positionModified) {
      this.positionModified = positionModified;
    }

    /**
     * Returns whether this status implies that the position has been modified.
     *
     * @return {@code true} if a position change is implied
     */
    public boolean isPositionModified() {
      return positionModified;
    }
  }

  /**
   * Teleports the given {@code entity} to the given {@code position} in the given {@code world} and sets its rotation
   * to the given one.
   * <p>Implementations must return a {@link TeleportStatus} that represents the status of the individual teleport.</p>
   *
   * @param entity   the entity to teleport
   * @param world    the world to teleport to
   * @param position the position to teleport to
   * @param rotation the rotation  to teleport to
   * @return The resulting {@code TeleportStatus}
   */
  TeleportStatus teleport(LocalEntity entity, LocalWorld world, Vector3 position, EulerDirection rotation);

}

package me.taylorkelly.mywarp.teleport;

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;

/**
 * A {@link TeleportManager} implementation that checks if a teleport destination is safe for a normal entity. If not,
 * a safe position is searched within a defined margin. If one is found the entity is teleported there, if not, no
 * teleport occurs.
 */
public class SafeTeleportManager implements TeleportManager {

  private final CubicPositionSafety positionSafety = new CubicPositionSafety();
  private final Settings settings;

  /**
   * Creates an instance that uses the given {@code settings}.
   *
   * @param settings the settings to use
   */
  public SafeTeleportManager(Settings settings) {
    this.settings = settings;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity, LocalWorld world, Vector3 position, EulerDirection rotation) {
    // In MyWarp 2.x, the warp height was equivalent with the Y coordinate of the block. If the warp was located on
    // top of a block that was smaller than a full black (e.g. a half step), the height needed to be adjusted or the
    // player would have been teleported inside of the block.
    if (world.getBlock(position).isNotFullHeight()) {
      position = position.add(0, 1, 0);
    }
    if (settings.isSafetyEnabled()) {
      Optional<Vector3>
          safePosition =
          positionSafety.getSafePosition(world, position, settings.getSafetySearchRadius());
      if (!safePosition.isPresent()) {
        return TeleportStatus.NONE;
      }
      if (!position.equals(safePosition.get())) {
        entity.teleport(world, safePosition.get(), rotation);
        return TeleportStatus.MODIFIED;
      }
    }
    entity.teleport(world, position, rotation);
    return TeleportStatus.ORIGINAL;
  }

}

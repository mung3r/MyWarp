/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

package me.taylorkelly.mywarp;

import java.util.List;
import java.util.Locale;

/**
 * Provides all user-configurable settings for MyWarp. Implementations are expected to be immutable. <p>Typically an
 * implementation is provided by the platform running MyWarp.</p>
 */
public interface Settings {

  /**
   * Returns whether the world access should be controlled directly by MyWarp.
   *
   * @return {@code true} if world access should be controlled
   */
  boolean isControlWorldAccess();

  /**
   * Returns whether currently unloaded chunks should be loaded manually teleport an entity there.
   *
   * @return {@code true} if chunks should be loaded before
   */
  boolean isPreloadChunks();

  /**
   * Returns whether horses ridden by the entity who is teleported, should be teleported too.
   *
   * @return {@code true} if ridden horses should be teleported too
   */
  boolean isTeleportTamedHorses();

  /**
   * Returns whether an effect should be shown at the location of entities who are teleported.
   *
   * @return {@code true} if the effect should be shown
   */
  boolean isShowTeleportEffect();

  /**
   * Gets the default locale.
   *
   * @return the default locale
   */
  Locale getLocalizationDefaultLocale();

  /**
   * Returns whether localizations should be resolved individually per player rather than globally.
   *
   * @return {@code true} if localizations are per player
   */
  boolean isLocalizationPerPlayer();

  /**
   * Returns whether safety checks for teleports are enabled.
   *
   * @return {@code true} if the location's safety should be checked before teleporting an entity
   */
  boolean isSafetyEnabled();

  /**
   * Gets the radius that is used to search a safe location.
   *
   * @return the search radius
   */
  int getSafetySearchRadius();

  /**
   * Returns whether warp signs are enabled.
   *
   * @return {@code true} if warp signs are enabled
   */
  boolean isWarpSignsEnabled();

  /**
   * Gets all identifiers for warp signs.
   *
   * @return all warp sign identifiers
   */
  List<String> getWarpSignsIdentifiers();

  /**
   * Returns whether warp creation limits are enabled.
   *
   * @return {@code true} if limits are enabled
   */
  boolean isLimitsEnabled();

  /**
   * Returns whether timers are enabled.
   *
   * @return {@code true} if timers are enabled
   */
  boolean isTimersEnabled();

  /**
   * Returns whether the warp-cooldown should notify users when they have cooled down.
   *
   * @return {@code true} if users should be notified
   */
  boolean isTimersCooldownNotifyOnFinish();

  /**
   * Returns whether the warp-warmup should be aborted if the users takes any damage while warming up.
   *
   * @return {@code true} if the warp-warmuo should be aborted
   */
  boolean isTimersWarmupAbortOnDamage();

  /**
   * Returns whether the warp-warmup should be aborted if the user moves while warming up.
   *
   * @return {@code true} if the warp-warmup should be aborted
   */
  boolean isTimersWarmupAbortOnMove();

  /**
   * Returns whether the warp-warmup should notify users when the warmup starts.
   *
   * @return {@code true} if users should be notified
   */
  boolean isTimersWarmupNotifyOnStart();

  /**
   * Returns whether economy support is enabled.
   *
   * @return {@code true} if economy support is enabled
   */
  boolean isEconomyEnabled();

  /**
   * Returns whether the economy link should inform users after successful transactions.
   *
   * @return {@code {@code true}} if users should be informed after a transaction
   */
  boolean isEconomyInformAfterTransaction();

}

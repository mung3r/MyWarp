/**
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
package me.taylorkelly.mywarp;

import java.util.List;
import java.util.Locale;
import me.taylorkelly.mywarp.data.LimitBundle;
import me.taylorkelly.mywarp.economy.FeeBundle;
import me.taylorkelly.mywarp.timer.TimeBundle;

/**
 * This interface provides all user-configurable values for MyWarp.
 * Implementations are expected to be immutable.
 */
public interface Settings extends Reloadable {

    /**
     * Returns whether the world access should be controlled directly by MyWarp.
     * 
     * @return true if world access should be controlled
     */
    boolean isControlWorldAccess();

    /**
     * Returns whether currently unloaded chunks should be loaded manually
     * teleport an entity there.
     * 
     * @return true if chunks should be loaded before
     */
    boolean isPreloadChunks();

    /**
     * Returns whether horses ridden by the entity who is teleported, should be
     * teleported too.
     * 
     * @return true if ridden horses should be teleported too
     */
    boolean isTeleportTamedHorses();

    /**
     * Returns whether entities who are leashed by the entity who is teleported,
     * should be teleported too.
     * 
     * @return true if leashed entities should be teleported too
     */
    boolean isTeleportLeashedEntities();

    /**
     * Returns whether an effect should be shown at the location of entities who
     * are teleported.
     * 
     * @return true if the effect should be shown
     */
    boolean isShowTeleportEffect();

    /**
     * Returns whether warps should be suggested base on popularity when
     * multiple warps match a query.
     * 
     * @return true if warps should be suggested
     */
    boolean isDynamicsSuggestWarps();

    /**
     * Returns whether MySQL is enabled.
     * 
     * @return true if MySQL is enabled
     */
    boolean isMysqlEnabled();

    /**
     * Gets data source name of the MySQL server.
     * 
     * @return the MySQL server's dsn
     */
    String getMysqlDsn();

    /**
     * Gets the username of the MySQL user.
     * 
     * @return the username
     */
    String getMysqlUsername();

    /**
     * Gets the password of the MySQL user.
     * 
     * @return the password
     */
    String getMysqlPassword();

    /**
     * Gets the default locale.
     * 
     * @return the default locale
     */
    Locale getLocalizationDefaultLocale();

    /**
     * Returns whether localizations should be resolved individually per player
     * rather than globally.
     * 
     * @return true if localizations are per player
     */
    boolean isLocalizationPerPlayer();

    /**
     * Returns whether safety checks for teleports are enabled.
     * 
     * @return true if the location's safety should be checked before
     *         teleporting an entity
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
     * @return true if warp signs are enabled
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
     * @return true if limits are enabled
     */
    boolean isLimitsEnabled();

    /**
     * Gets the default limit-bundle.
     * 
     * @return the default limit-bundle.
     */
    LimitBundle getLimitsDefaultLimitBundle();

    /**
     * Gets a list of additional configured limit-bundles.
     * 
     * @return all configured limit-bundles
     */
    List<LimitBundle> getLimitsConfiguredLimitBundles();

    /**
     * Returns whether timers are enabled.
     * 
     * @return true if timers are enabled
     */
    boolean isTimersEnabled();

    /**
     * Returns whether the warp-cooldown should notify users when they have
     * cooled down.
     * 
     * @return true if users should be notified
     */
    boolean isTimersCooldownNotifyOnFinish();

    /**
     * Returns whether the warp-warmup should be aborted if the users takes any
     * damage while warming up.
     * 
     * @return true if the warp-warmuo should be aborted
     */
    boolean isTimersWarmupAbortOnDamage();

    /**
     * Returns whether the warp-warmup should be aborted if the user moves while
     * warming up.
     * 
     * @return true if the warp-warmup should be aborted
     */
    boolean isTimersWarmupAbortOnMove();

    /**
     * Returns whether the warp-warmup should notify users when the warmup
     * starts.
     * 
     * @return true if users should be notified
     */
    boolean isTimersWarmupNotifyOnStart();

    /**
     * Gets the default time-bundle.
     * 
     * @return the default time-bundle
     */
    TimeBundle getTimersDefaultTimeBundle();

    /**
     * Gets a list of all additional configured time-bundles.
     * 
     * @return all configured time-bundles
     */
    List<TimeBundle> getTimersConfiguredTimeBundles();

    /**
     * Returns whether economy support is enabled.
     * 
     * @return true if economy support is enabled
     */
    boolean isEconomyEnabled();

    /**
     * Returns whether the economy link should inform users after successful
     * transactions.
     * 
     * @return true if users should be informed after a transaction
     */
    boolean isEconomyInformAfterTransaction();

    /**
     * Gets the default fee-bundle.
     * 
     * @return the default fee-bundle.
     */
    FeeBundle getEconomyDefaultFeeBundle();

    /**
     * Gets a list of additional configured fee-bundles.
     * 
     * @return all configured fee-bundles
     */
    List<FeeBundle> getEconomyConfiguredFeeBundles();

    /**
     * Returns whether Dynmap should be used as marker-service.
     * 
     * @return true if Dynmap should be used
     */
    boolean isDynmapEnabled();

    /**
     * Gets the display name of the layer that stores MyWarp's markers.
     * 
     * @return the layer's display name
     */
    String getDynmapLayerDisplayName();

    /**
     * Returns whether the layer that stores MyWarp's markers is hidden by
     * default.
     * 
     * @return true if the layer is hidden by default
     */
    boolean isDynmapLayerHiddenByDefault();

    /**
     * Gets the priority of the layer that stores MyWarp's markers.
     * 
     * @return the layer's priority
     */
    int getDynmapLayerPriority();

    /**
     * Gets the Dynmap identifier of the icon that is used for MyWarp's markers.
     * 
     * @return the icon's identifier
     */
    String getDynmapMarkerIconID();

    /**
     * Gets the minimal zoom level that must be meat to display MyWarp's
     * markers.
     * 
     * @return the minimal zoom level
     */
    int getDynmapMarkerMinZoom();

    /**
     * Returns whether MyWarp's markers should show a label by default.
     * 
     * @return true if the label is visible by default
     */
    boolean isDynmapMarkerShowLable();

}

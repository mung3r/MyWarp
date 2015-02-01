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

package me.taylorkelly.mywarp.bukkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.bukkit.economy.BukkitFeeProvider.FeeBundle;
import me.taylorkelly.mywarp.bukkit.limits.LimitBundle;
import me.taylorkelly.mywarp.bukkit.timer.BukkitDurationProvider.DurationBundle;
import me.taylorkelly.mywarp.timer.Duration;

import org.apache.commons.lang.LocaleUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * The settings when running on Bukkit. This implementation relies on Bukkit's
 * configuration API to manage the actual configuration file.
 */
public class BukkitSettings implements Settings {

    private static final Logger LOG = Logger.getLogger(BukkitSettings.class.getName());

    private final File configFile;
    private final Configuration defaultConfiguration;
    private final BukkitAdapter adapter;

    private Configuration config;
    private Locale defaultLocale;

    /**
     * Initializes this instance.
     * 
     * @param configFile
     *            the file that holds the configuration
     * @param defaultConfiguration
     *            the default configuration that acts as a fallback
     * @param adapter
     *            the adapter
     */
    public BukkitSettings(File configFile, FileConfiguration defaultConfiguration, BukkitAdapter adapter) {
        this.configFile = configFile;
        this.defaultConfiguration = defaultConfiguration;
        this.adapter = adapter;

        reload();
    }

    /**
     * Reloads the configuration.
     */
    public void reload() {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                LOG.info("Default '" + configFile.getName() + "' created successfully."); // NON-NLS
                                                                                          // NON-NLS
            } catch (IOException e) {
                LOG.log(Level.SEVERE,
                        "Failed to create the default configuration file ('" + configFile.getAbsolutePath() // NON-NLS
                                + "'), using build-in defaults for all values.", e); // NON-NLS
                config = defaultConfiguration;
                return;
            }
        }
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);

        // add defaults
        fileConfig.setDefaults(defaultConfiguration);
        fileConfig.options().copyDefaults(true);
        try {
            fileConfig.save(configFile);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to save configuration to '" + configFile.getAbsolutePath() // NON-NLS
                    + "', using build-in defaults for missing values.", e); // NON-NLS
        }
        config = fileConfig;

        // Bukkit's config does not support Locale objects and this call is quit
        // expensive so we cache the Locale
        defaultLocale = LocaleUtils.toLocale(config.getString("localization.defaultLocale")); // NON-NLS
    }

    @Override
    public boolean isControlWorldAccess() {
        return config.getBoolean("settings.controlWorldAccess"); // NON-NLS
    }

    @Override
    public boolean isPreloadChunks() {
        return config.getBoolean("settings.preloadChunks"); // NON-NLS
    }

    @Override
    public boolean isShowTeleportEffect() {
        return config.getBoolean("settings.showTeleportEffect"); // NON-NLS
    }

    @Override
    public boolean isTeleportTamedHorses() {
        return config.getBoolean("settings.teleportHorses"); // NON-NLS
    }

    @Override
    public boolean isTeleportLeashedEntities() {
        return config.getBoolean("settings.teleportLeashed"); // NON-NLS
    }

    @Override
    public boolean isDynamicsSuggestWarps() {
        return config.getBoolean("dynamics.suggestWarps"); // NON-NLS
    }

    @Override
    public Locale getLocalizationDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public boolean isLocalizationPerPlayer() {
        return config.getBoolean("localization.perPlayer"); // NON-NLS
    }

    @Override
    public boolean isSafetyEnabled() {
        return config.getBoolean("teleportSafety.enabled"); // NON-NLS
    }

    @Override
    public int getSafetySearchRadius() {
        return config.getInt("teleportSafety.searchRadius"); // NON-NLS
    }

    @Override
    public boolean isWarpSignsEnabled() {
        return config.getBoolean("warpSigns.enabled"); // NON-NLS
    }

    @Override
    public List<String> getWarpSignsIdentifiers() {
        return config.getStringList("warpSigns.identifiers"); // NON-NLS
    }

    @Override
    public boolean isMysqlEnabled() {
        return config.getBoolean("mysql.enabled"); // NON-NLS
    }

    @Override
    public String getMysqlDsn() {
        return config.getString("mysql.dsn"); // NON-NLS
    }

    @Override
    public String getMysqlUsername() {
        return config.getString("mysql.username"); // NON-NLS
    }

    @Override
    public String getMysqlPassword() {
        return config.getString("mysql.password"); // NON-NLS
    }

    @Override
    public boolean isLimitsEnabled() {
        return config.getBoolean("limits.enabled"); // NON-NLS
    }

    /**
     * Gets the default LimitBundle.
     * 
     * @return the default LimitBundle.
     */
    public LimitBundle getLimitsDefaultLimitBundle() {
        // the default bundle must cover all worlds!
        ConfigurationSection values = config.getConfigurationSection("limits.defaultLimit"); // NON-NLS
        return new LimitBundle("default", values.getInt("totalLimit"), values.getInt("publicLimit"), // NON-NLS
                // NON-NLS NON-NLS
                values.getInt("privateLimit"), adapter); // NON-NLS
    }

    /**
     * Gets a list of additional configured LimitBundles.
     * 
     * @return all configured LimitBundle
     */
    public List<LimitBundle> getLimitsConfiguredLimitBundles() {
        List<LimitBundle> ret = new ArrayList<LimitBundle>();
        ConfigurationSection configuredLimits = config.getConfigurationSection("limits.configuredLimits"); // NON-NLS
        if (configuredLimits == null) {
            // the section contains no values
            return ret;
        }
        for (String key : configuredLimits.getKeys(false)) {
            ret.add(toLimitBundle(key, configuredLimits.getConfigurationSection(key)));
        }
        return ret;
    }

    /**
     * Creates a LimitBundle out of the given identifier and
     * configuration-section.
     * 
     * @param identifier
     *            the identifier
     * @param values
     *            the configuration-section that contains all values
     * @return a LimitBundle out of the given values
     */
    private LimitBundle toLimitBundle(String identifier, ConfigurationSection values) {
        if (values.contains("affectedWorlds")) { // NON-NLS
            List<LocalWorld> worlds = new ArrayList<LocalWorld>();
            for (String name : values.getStringList("affectedWorlds")) { // NON-NLS
                World world = Bukkit.getWorld(name);
                if (world != null) {
                    // REVIEW log error on null?
                    worlds.add(adapter.adapt(world));
                }
            }
            return new LimitBundle(identifier, values.getInt("totalLimit"), values.getInt("publicLimit"),
            // NON-NLS NON-NLS
                    values.getInt("privateLimit"), worlds, adapter); // NON-NLS
        }
        return new LimitBundle(identifier, values.getInt("totalLimit"), values.getInt("publicLimit"),
        // NON-NLS NON-NLS
                values.getInt("privateLimit"), adapter); // NON-NLS
    }

    @Override
    public boolean isTimersEnabled() {
        return config.getBoolean("timers.enabled"); // NON-NLS
    }

    @Override
    public boolean isTimersCooldownNotifyOnFinish() {
        return config.getBoolean("timers.warpCooldown.notifyOnFinish"); // NON-NLS
    }

    @Override
    public boolean isTimersWarmupAbortOnDamage() {
        return config.getBoolean("timers.warpWarmup.abortOnDamage"); // NON-NLS
    }

    @Override
    public boolean isTimersWarmupAbortOnMove() {
        return config.getBoolean("timers.warpWarmup.abortOnMove"); // NON-NLS
    }

    @Override
    public boolean isTimersWarmupNotifyOnStart() {
        return config.getBoolean("timers.warpWarmup.notifyOnStart"); // NON-NLS
    }

    /**
     * Gets the default DurationBundle.
     * 
     * @return the default DurationBundle
     */
    public DurationBundle getTimersDefaultDurationBundle() {
        return toDurationBundle("default", config.getConfigurationSection("timers.defaultTimer")); // NON-NLS
                                                                                                   // NON-NLS
    }

    /**
     * Gets a list of additional configured DurationBundles.
     * 
     * @return all configured DurationBundles
     */
    public List<DurationBundle> getTimersConfiguredDurationBundles() {
        List<DurationBundle> ret = new ArrayList<DurationBundle>();
        ConfigurationSection configuredTimers = config.getConfigurationSection("timers.configuredTimers"); // NON-NLS
        if (configuredTimers == null) {
            // the section contains no values
            return ret;
        }
        for (String key : configuredTimers.getKeys(false)) {
            ret.add(toDurationBundle(key, configuredTimers.getConfigurationSection(key)));
        }
        return ret;
    }

    /**
     * Creates a time-bundle out of the given identifier and
     * configuration-section.
     * 
     * @param identifier
     *            the identifier
     * @param values
     *            the configuration-section that contains all values
     * @return a time-bundle out of the given values
     */
    private DurationBundle toDurationBundle(String identifier, ConfigurationSection values) {
        return new DurationBundle(identifier, new Duration(values.getLong("warpCooldown", 0), // NON-NLS
                TimeUnit.SECONDS), new Duration(values.getLong("warpWarmup", 0), TimeUnit.SECONDS)); // NON-NLS
    }

    @Override
    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled"); // NON-NLS
    }

    @Override
    public boolean isEconomyInformAfterTransaction() {
        return config.getBoolean("economy.informAfterTransaction"); // NON-NLS
    }

    /**
     * Gets the default FeeBundle.
     * 
     * @return the default FeeBundle
     */
    public FeeBundle getEconomyDefaultFeeBundle() {
        return toFeeBundle("default", config.getConfigurationSection("economy.defaultFee")); // NON-NLS
                                                                                             // NON-NLS
    }

    /**
     * Gets a list of additional configured FeeBundles.
     * 
     * @return all configured FeeBundles
     */
    public List<FeeBundle> getEconomyConfiguredFeeBundles() {
        List<FeeBundle> ret = new ArrayList<FeeBundle>();
        ConfigurationSection configuredFees = config.getConfigurationSection("economy.configuredFees"); // NON-NLS
        if (configuredFees == null) {
            // the section contains no values
            return ret;
        }
        for (String key : configuredFees.getKeys(false)) {
            ret.add(toFeeBundle(key, configuredFees.getConfigurationSection(key)));
        }
        return ret;
    }

    /**
     * Creates a FeeBundle out of the given identifier and
     * configuration-section.
     * 
     * @param identifier
     *            the identifier
     * @param values
     *            the configuration-section that contains all values
     * @return a FeeBundle out of the given values
     */
    private FeeBundle toFeeBundle(String identifier, ConfigurationSection values) {
        return new FeeBundle(identifier, values.getDouble("assets", 0), values.getDouble("create", 0),
        // NON-NLS NON-NLS
                values.getDouble("createPrivate", 0), values.getDouble("delete", 0), values.getDouble("give", // NON-NLS
                        // NON-NLS NON-NLS
                        0), values.getDouble("help", 0), values.getDouble("info", 0), values.getDouble(
                // NON-NLS NON-NLS
                        "invite", 0), values.getDouble("list", 0), values.getDouble("point", 0), // NON-NLS
                // NON-NLS NON-NLS
                values.getDouble("private", 0), values.getDouble("public", 0),
                // NON-NLS NON-NLS
                values.getDouble("uninvite", 0), values.getDouble("update", 0), values.getDouble(
                // NON-NLS NON-NLS
                        "warpPlayer", 0), values.getDouble("warpSignCreate", 0), values.getDouble(
                // NON-NLS NON-NLS
                        "warpSignUse", 0), values.getDouble("warpTo", 0), values.getDouble("welcome", 0)); // NON-NLS
                                                                                                           // NON-NLS
                                                                                                           // NON-NLS
    }

    @Override
    public boolean isDynmapEnabled() {
        return config.getBoolean("dynmap.enabled"); // NON-NLS
    }

    @Override
    public String getDynmapLayerDisplayName() {
        return config.getString("dynmap.layer.displayName"); // NON-NLS
    }

    @Override
    public boolean isDynmapLayerHiddenByDefault() {
        return config.getBoolean("dynmap.layer.hiddenByDefault"); // NON-NLS
    }

    @Override
    public int getDynmapLayerPriority() {
        return config.getInt("dynmap.layer.priority"); // NON-NLS
    }

    @Override
    public String getDynmapMarkerIconID() {
        return config.getString("dynmap.marker.iconID"); // NON-NLS
    }

    @Override
    public int getDynmapMarkerMinZoom() {
        return config.getInt("dynmap.marker.minZoom"); // NON-NLS
    }

    @Override
    public boolean isDynmapMarkerShowLable() {
        return config.getBoolean("dynmap.marker.showLabel"); // NON-NLS
    }

}

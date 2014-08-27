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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import me.taylorkelly.mywarp.data.LimitBundle;
import me.taylorkelly.mywarp.economy.FeeBundle;
import me.taylorkelly.mywarp.timer.TimeBundle;

import org.apache.commons.lang.LocaleUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * The settings when running on Bukkit. This implementation relies on Bukkit's
 * configuration API to manage the actual configuration file.
 */
public class BukkitSettings implements Settings {

    private final File configFile;
    private final Configuration defaultConfiguration;

    private Configuration config;

    /**
     * Initializes this instance.
     * 
     * @param configFile
     *            the file that holds the configuration
     * @param defaultConfiguration
     *            the default configuration that acts as a fallback
     */
    public BukkitSettings(File configFile, FileConfiguration defaultConfiguration) {
        this.configFile = configFile;
        this.defaultConfiguration = defaultConfiguration;

        reload();
    }

    @Override
    public void reload() {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                MyWarp.logger().info("Default '" + configFile.getName() + "' created successfully.");
            } catch (IOException e) {
                MyWarp.logger().log(
                        Level.SEVERE,
                        "Failed to create the default configuration file ('" + configFile.getAbsolutePath()
                                + "'), using build-in defaults for all values.", e);
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
            MyWarp.logger().log(
                    Level.SEVERE,
                    "Failed to save configuration to '" + configFile.getAbsolutePath()
                            + "', using build-in defaults for missing values.", e);
        }
        config = fileConfig;
    }

    @Override
    public boolean isControlWorldAccess() {
        return config.getBoolean("settings.controlWorldAccess");
    }

    @Override
    public boolean isPreloadChunks() {
        return config.getBoolean("settings.preloadChunks");
    }

    @Override
    public boolean isShowTeleportEffect() {
        return config.getBoolean("settings.showTeleportEffect");
    }

    @Override
    public boolean isTeleportTamedHorses() {
        return config.getBoolean("settings.teleportHorses");
    }

    @Override
    public boolean isTeleportLeashedEntities() {
        return config.getBoolean("settings.teleportLeashed");
    }

    @Override
    public boolean isDynamicsSuggestWarps() {
        return config.getBoolean("dynamics.suggestWarps");
    }

    @Override
    public Locale getLocalizationDefaultLocale() {
        return LocaleUtils.toLocale(config.getString("localization.defaultLocale"));
    }

    @Override
    public boolean isLocalizationPerPlayer() {
        return config.getBoolean("localization.perPlayer");
    }

    @Override
    public boolean isSafetyEnabled() {
        return config.getBoolean("teleportSafety.enabled");
    }

    @Override
    public int getSafetySearchRadius() {
        return config.getInt("teleportSafety.searchRadius");
    }

    @Override
    public boolean isWarpSignsEnabled() {
        return config.getBoolean("warpSigns.enabled");
    }

    @Override
    public List<String> getWarpSignsIdentifiers() {
        return config.getStringList("warpSigns.identifiers");
    }

    @Override
    public boolean isMysqlEnabled() {
        return config.getBoolean("mysql.enabled");
    }

    @Override
    public String getMysqlDsn() {
        return config.getString("mysql.dsn");
    }

    @Override
    public String getMysqlUsername() {
        return config.getString("mysql.username");
    }

    @Override
    public String getMysqlPassword() {
        return config.getString("mysql.password");
    }

    @Override
    public boolean isLimitsEnabled() {
        return config.getBoolean("limits.enabled");
    }

    @Override
    public LimitBundle getLimitsDefaultLimitBundle() {
        // the default bundle must cover all worlds!
        ConfigurationSection values = config.getConfigurationSection("limits.defaultLimit");
        return new LimitBundle("default", values.getInt("totalLimit"), values.getInt("publicLimit"),
                values.getInt("privateLimit"));
    }

    @Override
    public List<LimitBundle> getLimitsConfiguredLimitBundles() {
        List<LimitBundle> ret = new ArrayList<LimitBundle>();
        ConfigurationSection configuredLimits = config.getConfigurationSection("limits.configuredLimits");
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
     * Creates a limit-bundle out of the given identifier and
     * configuration-section.
     * 
     * @param identifier
     *            the identifier
     * @param values
     *            the configuration-section that contains all values
     * @return a limit-bundle out of the given values
     */
    private LimitBundle toLimitBundle(String identifier, ConfigurationSection values) {
        if (values.contains("affectedWorlds")) {
            return new LimitBundle(identifier, values.getInt("totalLimit"), values.getInt("publicLimit"),
                    values.getInt("privateLimit"), values.getStringList("affectedWorlds"));
        }
        return new LimitBundle(identifier, values.getInt("totalLimit"), values.getInt("publicLimit"),
                values.getInt("privateLimit"));
    }

    @Override
    public boolean isTimersEnabled() {
        return config.getBoolean("timers.enabled");
    }

    @Override
    public boolean isTimersCooldownNotifyOnFinish() {
        return config.getBoolean("timers.warpCooldown.notifyOnFinish");
    }

    @Override
    public boolean isTimersWarmupAbortOnDamage() {
        return config.getBoolean("timers.warpWarmup.abortOnDamage");
    }

    @Override
    public boolean isTimersWarmupAbortOnMove() {
        return config.getBoolean("timers.warpWarmup.abortOnMove");
    }

    @Override
    public boolean isTimersWarmupNotifyOnStart() {
        return config.getBoolean("timers.warpWarmup.notifyOnStart");
    }

    @Override
    public TimeBundle getTimersDefaultTimeBundle() {
        return toTimeBundle("default", config.getConfigurationSection("timers.defaultTimer"));
    }

    @Override
    public List<TimeBundle> getTimersConfiguredTimeBundles() {
        List<TimeBundle> ret = new ArrayList<TimeBundle>();
        ConfigurationSection configuredTimers = config.getConfigurationSection("timers.configuredTimers");
        if (configuredTimers == null) {
            // the section contains no values
            return ret;
        }
        for (String key : configuredTimers.getKeys(false)) {
            ret.add(toTimeBundle(key, configuredTimers.getConfigurationSection(key)));
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
    private TimeBundle toTimeBundle(String identifier, ConfigurationSection values) {
        return new TimeBundle(identifier, values.getDouble("cooldown", 0), values.getDouble("warmup", 0));
    }

    @Override
    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled");
    }

    @Override
    public boolean isEconomyInformAfterTransaction() {
        return config.getBoolean("economy.informAfterTransaction");
    }

    @Override
    public FeeBundle getEconomyDefaultFeeBundle() {
        return toFeeBundle("default", config.getConfigurationSection("economy.defaultFee"));
    }

    @Override
    public List<FeeBundle> getEconomyConfiguredFeeBundles() {
        List<FeeBundle> ret = new ArrayList<FeeBundle>();
        ConfigurationSection configuredFees = config.getConfigurationSection("economy.configuredFees");
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
     * Creates a fee-bundle out of the given identifier and
     * configuration-section.
     * 
     * @param identifier
     *            the identifier
     * @param values
     *            the configuration-section that contains all values
     * @return a fee-bundle out of the given values
     */
    private FeeBundle toFeeBundle(String identifier, ConfigurationSection values) {
        return new FeeBundle(identifier, values.getDouble("assets", 0), values.getDouble("create", 0),
                values.getDouble("createPrivate", 0), values.getDouble("delete", 0), values.getDouble("give",
                        0), values.getDouble("help", 0), values.getDouble("info", 0), values.getDouble(
                        "invite", 0), values.getDouble("list", 0), values.getDouble("point", 0),
                values.getDouble("private", 0), values.getDouble("public", 0), values.getDouble("search", 0),
                values.getDouble("uninvite", 0), values.getDouble("update", 0), values.getDouble(
                        "warpPlayer", 0), values.getDouble("warpSignCreate", 0), values.getDouble(
                        "warpSignUse", 0), values.getDouble("warpTo", 0), values.getDouble("welcome", 0));
    }

    @Override
    public boolean isDynmapEnabled() {
        return config.getBoolean("dynmap.enabled");
    }

    @Override
    public String getDynmapLayerDisplayName() {
        return config.getString("dynmap.layer.displayName");
    }

    @Override
    public boolean isDynmapLayerHiddenByDefault() {
        return config.getBoolean("dynmap.layer.hiddenByDefault");
    }

    @Override
    public int getDynmapLayerPriority() {
        return config.getInt("dynmap.layer.priority");
    }

    @Override
    public String getDynmapMarkerIconID() {
        return config.getString("dynmap.marker.iconID");
    }

    @Override
    public int getDynmapMarkerMinZoom() {
        return config.getInt("dynmap.marker.minZoom");
    }

    @Override
    public boolean isDynmapMarkerShowLable() {
        return config.getBoolean("dynmap.marker.showLabel");
    }

}

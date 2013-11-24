package me.taylorkelly.mywarp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;

import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.economy.WarpFees;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.ConfigUtils;
import me.taylorkelly.mywarp.utils.PropertiesFile;

import org.apache.commons.lang.LocaleUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpSettings {

    private static final String CONFIG_FILE = "config.yml";
    private static final String LEGACY_CONFIG_FILE = "MyWarp.settings";

    private FileConfiguration config;
    private final File configFile;

    // Settings
    public boolean controlWorldAccess;
    public boolean loadChunks;
    public boolean warpEffect;

    // Dynamics
    public boolean dynamicsSuggestWarps;

    // MySQL
    public boolean mysqlEnabled;
    public String mysqlHost;
    public int mysqlPort;
    public String mysqlDatabase;
    public String mysqlTable;
    public String mysqlUsername;
    public String mysqlPassword;

    // Localization
    public Locale localizationDefLocale;
    public boolean localizationPerPlayer;

    // WarpSafety
    public boolean safetyEnabled;
    public int safetySearchRadius;
    public int safetyVerticalTolerance;
    public boolean safetyTeleportHorses;
    public boolean safetyTeleportLeashed;

    // WarpSigns
    public boolean warpSignsEnabled;
    public TreeSet<String> warpSignsIdentifiers;

    // Limits
    public boolean limitsEnabled;
    public WarpLimit limitsDefaultWarpLimit;
    public List<WarpLimit> limitsWarpLimits;

    // Timers
    public boolean timersEnabled;
    public boolean timersCooldownNotify;
    public boolean timersWarmupNotify;
    public boolean timersAbortOnMove;
    public boolean timersAbortOnDamage;
    public Time timersDefaultCooldown;
    public List<Time> timersCooldowns;
    public Time timersDefaultWarmup;
    public List<Time> timersWarmups;

    // Economy
    public boolean economyEnabled;
    public boolean economyInformAfterTransaction;
    public WarpFees economyDefaultFees;
    public List<WarpFees> economyFees;

    // Dynmap
    public boolean dynmapEnabled;
    // Dynmap - Layer
    public boolean dynmapLayerHideByDefault;
    public String dynmapLayerDisplayName;
    public int dynmapLayerPriority;
    // Dynmap - Marker
    public String dynmapMarkerIconID;
    public int dynmapMarkerMinZoom;
    public boolean dynmapMarkerShowLable;

    public WarpSettings() {
        configFile = new File(MyWarp.inst().getDataFolder(), CONFIG_FILE);
        config = ConfigUtils.getYamlConfig(configFile,
                YamlConfiguration.loadConfiguration(MyWarp.inst().getResource(CONFIG_FILE)), false);

        // migrate the legacy configuration, if it exists
        File legacyConfigFile = new File(MyWarp.inst().getDataFolder(), LEGACY_CONFIG_FILE);
        if (legacyConfigFile.exists()) {
            migrateLegacyConfig(legacyConfigFile);
        }

        // finally load all values
        loadValues(config);
    }

    /**
     * Loads all values out of the FileConfiguration into the intern-logic
     */
    private void loadValues(FileConfiguration config) {
        limitsWarpLimits = new ArrayList<WarpLimit>();
        timersCooldowns = new ArrayList<Time>();
        timersWarmups = new ArrayList<Time>();
        economyFees = new ArrayList<WarpFees>();
        warpSignsIdentifiers = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

        // Settings
        controlWorldAccess = config.getBoolean("settings.controlWorldAccess");
        loadChunks = config.getBoolean("settings.loadChunks");
        warpEffect = config.getBoolean("settings.warpEffect");

        // Dynamics
        dynamicsSuggestWarps = config.getBoolean("dynamics.suggestWarps");

        // MySQL
        mysqlEnabled = config.getBoolean("mysql.enabled");
        mysqlHost = config.getString("mysql.host");
        mysqlPort = config.getInt("mysql.port");
        mysqlUsername = config.getString("mysql.username");
        mysqlPassword = config.getString("mysql.password");
        mysqlDatabase = config.getString("mysql.database");
        mysqlTable = config.getString("mysql.table");

        // Localization
        localizationDefLocale = LocaleUtils.toLocale(config.getString("localization.defaultLocale"));
        localizationPerPlayer = config.getBoolean("localization.perPlayer");

        // Safety
        safetyEnabled = config.getBoolean("warpSafety.enabled");
        safetySearchRadius = config.getInt("warpSafety.searchRadius");
        safetyVerticalTolerance = config.getInt("warpSafety.verticalTolerance");
        safetyTeleportHorses = config.getBoolean("warpSafety.teleportHorses");
        safetyTeleportLeashed = config.getBoolean("warpSafety.teleportLeashed");

        // WarpSigns
        warpSignsEnabled = config.getBoolean("warpSigns.enabled");
        warpSignsIdentifiers.addAll(config.getStringList("warpSigns.identifiers"));

        // Limits
        limitsEnabled = config.getBoolean("limits.enabled");
        ConfigurationSection configuredLimits = config.getConfigurationSection("limits");
        for (String key : config.getConfigurationSection("limits").getKeys(false)) {
            if (key.equals("enabled")) {
                // ignore the enabled option
                continue;
            } else if (key.equals("default")) {
                limitsDefaultWarpLimit = new WarpLimit(key, configuredLimits.getInt(key + ".maxTotal", 0),
                        configuredLimits.getInt(key + ".maxPublic", 0), configuredLimits.getInt(key
                                + ".maxPrivate", 0));
            } else {
                limitsWarpLimits.add(new WarpLimit(key, configuredLimits.getInt(key + ".maxTotal", 0),
                        configuredLimits.getInt(key + ".maxPublic", 0), configuredLimits.getInt(key
                                + ".maxPrivate", 0)));
            }
        }
        Collections.sort(limitsWarpLimits);

        // Timers
        timersEnabled = config.getBoolean("timers.enabled");
        timersCooldownNotify = config.getBoolean("timers.coolDownNotify");
        timersWarmupNotify = config.getBoolean("timers.warmUpNotify");
        timersAbortOnMove = config.getBoolean("timers.abortOnMove");
        timersAbortOnDamage = config.getBoolean("timers.abortOnDamage");
        ConfigurationSection configuredCooldowns = config.getConfigurationSection("timers.cooldowns");
        for (String key : configuredCooldowns.getKeys(false)) {
            if (key.equals("default")) {
                timersDefaultCooldown = new Time(key, configuredCooldowns.getDouble(key));
            } else {
                timersCooldowns.add(new Time(key, configuredCooldowns.getDouble(key)));
            }
        }
        Collections.sort(timersCooldowns);

        ConfigurationSection configuredWarmups = config.getConfigurationSection("timers.warmups");
        for (String key : configuredWarmups.getKeys(false)) {
            if (key.equals("default")) {
                timersDefaultWarmup = new Time(key, configuredWarmups.getDouble(key));
            } else {
                timersWarmups.add(new Time(key, configuredWarmups.getDouble(key)));
            }
        }
        Collections.sort(timersWarmups);

        // Economy
        economyEnabled = config.getBoolean("economy.enabled");
        economyInformAfterTransaction = config.getBoolean("economy.informAfterTransaction");

        ConfigurationSection configuredFees = config.getConfigurationSection("economy.fees");
        for (String key : configuredFees.getKeys(false)) {
            WarpFees fees = new WarpFees(key, configuredFees.getDouble(key + ".accept", 0),
                    configuredFees.getDouble(key + ".assets", 0),
                    configuredFees.getDouble(key + ".create", 0), configuredFees.getDouble(key
                            + ".create-private", 0), configuredFees.getDouble(key + ".delete", 0),
                    configuredFees.getDouble(key + ".give", 0), configuredFees.getDouble(key + ".help", 0),
                    configuredFees.getDouble(key + ".info", 0), configuredFees.getDouble(key + ".invite", 0),
                    configuredFees.getDouble(key + ".list", 0), configuredFees.getDouble(key + ".point", 0),
                    configuredFees.getDouble(key + ".private", 0), configuredFees.getDouble(key + ".public",
                            0), configuredFees.getDouble(key + ".search", 0), configuredFees.getDouble(key
                            + ".uninvite", 0), configuredFees.getDouble(key + ".update", 0),
                    configuredFees.getDouble(key + ".warp-player", 0), configuredFees.getDouble(key
                            + ".warp-sign-create", 0), configuredFees.getDouble(key + ".warp-sign-use", 0),
                    configuredFees.getDouble(key + ".warp-to", 0), configuredFees.getDouble(key + ".welcome",
                            0));
            if (key.equals("default")) {
                economyDefaultFees = fees;
            } else {
                economyFees.add(fees);
            }
            Collections.sort(economyFees);
        }

        // Dynmap
        dynmapEnabled = config.getBoolean("dynmap.enabled");
        dynmapLayerHideByDefault = config.getBoolean("dynmap.layer.hideByDefault");
        dynmapLayerDisplayName = config.getString("dynmap.layer.displayName");
        dynmapLayerPriority = config.getInt("dynmap.layer.priority");
        dynmapMarkerIconID = config.getString("dynmap.marker.iconID");
        dynmapMarkerMinZoom = config.getInt("dynmap.marker.minZoom");
        dynmapMarkerShowLable = config.getBoolean("dynmap.marker.showLabel");
    }

    /**
     * Migrates the legacy configuration (properties-file) to the new format
     * 
     * @param newConfig
     *            the file configuration that should be used as new
     *            configuration
     */
    private void migrateLegacyConfig(File legacyConfig) {
        PropertiesFile file = new PropertiesFile(legacyConfig);
        // port settings
        config.set("settings.adminPrivateWarps", file.getBoolean("adminPrivateWarps", true,
                "Whether or not admins can see private warps in their list"));
        config.set("settings.loadChunks", file.getBoolean("loadChunks", false,
                "Force sending of the chunk which people teleport to - default: false"));

        // port limits
        config.set(
                "limits.default.maxTotal",
                file.getInt("maxPublic", 5, "Maximum number of public warps any player can make")
                        + file.getInt("maxPrivate", 10, "Maximum number of private warps any player can make"));
        config.set("limits.default.maxPublic",
                file.getInt("maxPublic", 5, "Maximum number of public warps any player can make"));
        config.set("limits.default.maxPrivate",
                file.getInt("maxPrivate", 10, "Maximum number of private warps any player can make"));

        // port database
        String mySQLconn = file.getString("mySQLconn", "jdbc:mysql://localhost:3306/minecraft",
                "MySQL Connection (only if using MySQL)");
        mySQLconn = mySQLconn.substring(mySQLconn.indexOf("//") + 2);
        String[] mySQLconnParts = mySQLconn.split("[\\W]");
        config.set("mysql.enabled", file.getBoolean("usemySQL", false,
                "MySQL usage --  true = use MySQL database / false = use SQLite"));
        config.set("mysql.host", mySQLconnParts[0]);
        config.set("mysql.port", mySQLconnParts[1]);
        config.set("mysql.database", mySQLconnParts[2]);
        config.set("mysql.username",
                file.getString("mySQLuname", "root", "MySQL Username (only if using MySQL)"));
        config.set("mysql.password",
                file.getString("mySQLpass", "password", "MySQL Password (only if using MySQL)"));

        try {
            config.save(configFile);
            if (!legacyConfig.renameTo(new File(legacyConfig.getAbsolutePath() + ".old"))) {
                MyWarp.logger().warning("Could not rename old settings file, please remove it manually!");
            } else {
                MyWarp.logger().info("Successfully ported old settings file");
            }
        } catch (IOException e) {
            MyWarp.logger().log(Level.SEVERE, "Failed to port old settings file", e);
        }
    }

    /**
     * Reloads the configuration from the configuration-file
     */
    public void reload() {
        config = ConfigUtils.getYamlConfig(configFile,
                YamlConfiguration.loadConfiguration(MyWarp.inst().getResource(CONFIG_FILE)), true);
        loadValues(config);
    }

}

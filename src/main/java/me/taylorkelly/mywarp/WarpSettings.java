package me.taylorkelly.mywarp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.economy.WarpFees;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.PropertiesFile;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class WarpSettings {

    private static final String CONFIG_FILE = "config.yml";
    private static final String LEGACY_CONFIG_FILE = "MyWarp.settings";

    private FileConfiguration config;
    private final File configFile;

    public boolean worldAccess;
    public boolean loadChunks;
    public boolean warpEffect;

    public boolean suggestWarps;

    public String locale;

    public boolean useWarpSafety;
    public int searchRadius;
    public int verticalTolerance;

    public boolean useWarpLimits;
    public ArrayList<WarpLimit> warpLimits;
    public WarpLimit defaultLimit;

    public boolean useTimers;
    public boolean coolDownNotify;
    public boolean warmUpNotify;
    public boolean abortOnMove;
    public boolean abortOnDamage;
    public ArrayList<Time> warpCooldowns;
    public Time defaultCooldown;
    public ArrayList<Time> warpWarmups;
    public Time defaultWarmup;

    public boolean usemySQL;
    public String mySQLhost;
    public int mySQLport;
    public String mySQLuname;
    public String mySQLpass;
    public String mySQLdb;
    public String mySQLtable;

    public boolean useEconomy;
    public boolean informAfterTransaction;
    public ArrayList<WarpFees> warpFees;
    public WarpFees defaultWarpFees;

    public boolean useDynmap;
    public boolean hideLayerByDefault;
    public String layerDisplayName;
    public int layerPriority;
    public String markerIconID;
    public int markerMinZoom;
    public boolean showMarkerLable;

    public WarpSettings() {
        configFile = new File(MyWarp.inst().getDataFolder(), CONFIG_FILE);
        config = getYAMLConfig(
                configFile,
                YamlConfiguration.loadConfiguration(MyWarp.inst().getResource(
                        CONFIG_FILE)));

        // migrate the legacy configuration, if it exists
        File legacyConfigFile = new File(MyWarp.inst().getDataFolder(),
                LEGACY_CONFIG_FILE);
        if (legacyConfigFile.exists()) {
            migrateLegacyConfig(legacyConfigFile);
        }

        // finally load all values
        loadValues(config);
    }

    /**
     * Returns the FileConfiguration that belongs to the given file. If the file
     * does not exist, it will be created. In both cases, missing values are
     * added from the provided default-Configuration.
     * 
     * If the configuration-file is unreadable, this method will return the
     * provided default-configuration.
     * 
     * @param defaultConfig
     *            a FileConfiguration that contains all default values
     * 
     * @return the FileConfiguraion of the given file
     */
    private FileConfiguration getYAMLConfig(File configFile,
            FileConfiguration defaultConfig) {
        FileConfiguration config = defaultConfig;

        try {
            // create the configuration file if it does not exist
            if (!configFile.exists()) {
                configFile.mkdirs();
                configFile.createNewFile();

                MyWarp.logger().info(
                        "Default " + configFile.getName()
                                + " created successfully!");
            }

            // load the configuration-file
            config = YamlConfiguration.loadConfiguration(configFile);

            // copy defaults for missing values
            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);
            config.save(configFile);
        } catch (IOException e) {
            MyWarp.logger().log(
                    Level.SEVERE,
                    "Failed to create default " + configFile.getName()
                            + " , using build-in defaults: ", e);
        }

        return config;
    }

    /**
     * Loads all values out of the FileConfiguration into the intern-logic
     */
    private void loadValues(FileConfiguration config) {
        warpLimits = new ArrayList<WarpLimit>();
        warpCooldowns = new ArrayList<Time>();
        warpWarmups = new ArrayList<Time>();
        warpFees = new ArrayList<WarpFees>();

        // settings
        worldAccess = config.getBoolean("settings.controlWorldAccess");
        loadChunks = config.getBoolean("settings.loadChunks");
        warpEffect = config.getBoolean("settings.warpEffect");

        // dynamics
        suggestWarps = config.getBoolean("dynamics.suggestWarps");

        // language
        locale = config.getString("locale.locale");

        // saftey
        useWarpSafety = config.getBoolean("warpSafety.enabled");
        searchRadius = config.getInt("warpSafety.searchRadius");
        verticalTolerance = config.getInt("warpSafety.verticalTolerance");

        // limits
        useWarpLimits = config.getBoolean("limits.enabled");
        ConfigurationSection configuredLimits = config
                .getConfigurationSection("limits");
        for (String key : config.getConfigurationSection("limits").getKeys(
                false)) {
            if (key.equals("enabled")) {
                // ignore the enabled option
                continue;
            } else if (key.equals("default")) {
                defaultLimit = new WarpLimit(key, configuredLimits.getInt(key
                        + ".maxTotal"), configuredLimits.getInt(key
                        + ".maxPublic"), configuredLimits.getInt(key
                        + ".maxPrivate"));
            } else {
                warpLimits.add(new WarpLimit(key, configuredLimits.getInt(key
                        + ".maxTotal"), configuredLimits.getInt(key
                        + ".maxPublic"), configuredLimits.getInt(key
                        + ".maxPrivate")));
            }
        }
        Collections.sort(warpLimits);

        // timers
        useTimers = config.getBoolean("timers.enabled");
        coolDownNotify = config.getBoolean("timers.coolDownNotify");
        warmUpNotify = config.getBoolean("timers.warmUpNotify");
        abortOnMove = config.getBoolean("timers.abortOnMove");
        abortOnDamage = config.getBoolean("timers.abortOnDamage");
        ConfigurationSection configuredCooldowns = config
                .getConfigurationSection("timers.cooldowns");
        for (String key : configuredCooldowns.getKeys(false)) {
            if (key.equals("default")) {
                defaultCooldown = new Time(key,
                        configuredCooldowns.getDouble(key));
            } else {
                warpCooldowns.add(new Time(key, configuredCooldowns
                        .getDouble(key)));
            }
        }
        Collections.sort(warpCooldowns);

        ConfigurationSection configuredWarmups = config
                .getConfigurationSection("timers.warmups");
        for (String key : configuredWarmups.getKeys(
                false)) {
            if (key.equals("default")) {
                defaultWarmup = new Time(key, configuredWarmups.getDouble(key));
            } else {
                warpWarmups
                        .add(new Time(key, configuredWarmups.getDouble(key)));
            }
        }
        Collections.sort(warpWarmups);

        // database
        usemySQL = config.getBoolean("mysql.enabled");
        mySQLhost = config.getString("mysql.host");
        mySQLport = config.getInt("mysql.port");
        mySQLuname = config.getString("mysql.username");
        mySQLpass = config.getString("mysql.password");
        mySQLdb = config.getString("mysql.database");
        mySQLtable = config.getString("mysql.table");

        // economy
        useEconomy = config.getBoolean("economy.enabled");
        informAfterTransaction = config
                .getBoolean("economy.informAfterTransaction");

        ConfigurationSection configuredFees = config
                .getConfigurationSection("economy.fees");
        for (String key : configuredFees.getKeys(false)) {
            WarpFees fees = new WarpFees(key, configuredFees.getDouble(key
                    + ".accept"), configuredFees.getDouble(key + ".create"),
                    configuredFees.getDouble(key + ".create-private"),
                    configuredFees.getDouble(key + ".delete"),
                    configuredFees.getDouble(key + ".give"),
                    configuredFees.getDouble(key + ".help"),
                    configuredFees.getDouble(key + ".info"),
                    configuredFees.getDouble(key + ".invite"),
                    configuredFees.getDouble(key + ".list"),
                    configuredFees.getDouble(key + ".listall"),
                    configuredFees.getDouble(key + ".point"),
                    configuredFees.getDouble(key + ".private"),
                    configuredFees.getDouble(key + ".public"),
                    configuredFees.getDouble(key + ".search"),
                    configuredFees.getDouble(key + ".uninvite"),
                    configuredFees.getDouble(key + ".update"),
                    configuredFees.getDouble(key + ".warp-player"),
                    configuredFees.getDouble(key + ".warp-sign-create"),
                    configuredFees.getDouble(key + ".warp-sign-use"),
                    configuredFees.getDouble(key + ".warp-to"),
                    configuredFees.getDouble(key + ".welcome"));
            if (key.equals("default")) {
                defaultWarpFees = fees;
            } else {
                warpFees.add(fees);
            }
            Collections.sort(warpFees);
        }

        // dynmap
        useDynmap = config.getBoolean("dynmap.enabled");
        hideLayerByDefault = config.getBoolean("dynmap.layer.hideByDefault");
        layerDisplayName = config.getString("dynmap.layer.displayName");
        layerPriority = config.getInt("dynmap.layer.priority");
        markerIconID = config.getString("dynmap.marker.iconID");
        markerMinZoom = config.getInt("dynmap.marker.minZoom");
        showMarkerLable = config.getBoolean("dynmap.marker.showLabel");
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
        config.set("settings.adminPrivateWarps", file.getBoolean(
                "adminPrivateWarps", true,
                "Whether or not admins can see private warps in their list"));
        config.set(
                "settings.loadChunks",
                file.getBoolean("loadChunks", false,
                        "Force sending of the chunk which people teleport to - default: false"));

        // port limits
        config.set(
                "limits.default.maxTotal",
                file.getInt("maxPublic", 5,
                        "Maximum number of public warps any player can make")
                        + file.getInt("maxPrivate", 10,
                                "Maximum number of private warps any player can make"));
        config.set("limits.default.maxPublic", file.getInt("maxPublic", 5,
                "Maximum number of public warps any player can make"));
        config.set("limits.default.maxPrivate", file.getInt("maxPrivate", 10,
                "Maximum number of private warps any player can make"));

        // port database
        String mySQLconn = file.getString("mySQLconn",
                "jdbc:mysql://localhost:3306/minecraft",
                "MySQL Connection (only if using MySQL)");
        mySQLconn = mySQLconn.substring(mySQLconn.indexOf("//") + 2);
        String[] mySQLconnParts = mySQLconn.split("[\\W]");
        config.set(
                "mysql.enabled",
                file.getBoolean("usemySQL", false,
                        "MySQL usage --  true = use MySQL database / false = use SQLite"));
        config.set("mysql.host", mySQLconnParts[0]);
        config.set("mysql.port", mySQLconnParts[1]);
        config.set("mysql.database", mySQLconnParts[2]);
        config.set("mysql.username", file.getString("mySQLuname", "root",
                "MySQL Username (only if using MySQL)"));
        config.set("mysql.password", file.getString("mySQLpass", "password",
                "MySQL Password (only if using MySQL)"));

        try {
            config.save(configFile);
            if (!legacyConfig.renameTo(new File(legacyConfig.getAbsolutePath()
                    + ".old"))) {
                MyWarp.logger()
                        .warning(
                                "Could not rename old settings file, please remove it manually!");
            } else {
                MyWarp.logger().info("Successfully ported old settings file");
            }
        } catch (IOException e) {
            MyWarp.logger().log(Level.SEVERE,
                    "Failed to port old settings file", e);
        }
    }

    /**
     * Reloads the configuration from the configuration-file
     */
    public void reload() {
        config = getYAMLConfig(
                configFile,
                YamlConfiguration.loadConfiguration(MyWarp.inst().getResource(
                        CONFIG_FILE)));
        loadValues(config);
    }

}

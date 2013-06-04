package me.taylorkelly.mywarp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.economy.WarpFees;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.PropertiesFile;
import me.taylorkelly.mywarp.utils.WarpLogger;

public class WarpSettings {

    private static final String CONFIG_FILE = "config.yml";
    private static final String settingsFile = "MyWarp.settings";
    public static File dataDir;

    public static boolean worldAccess;
    public static boolean loadChunks;
    public static boolean warpEffect;

    public static boolean suggestWarps;

    public static String locale;

    public static boolean useWarpSafety;
    public static int searchRadius;
    public static int verticalTolerance;

    public static boolean useWarpLimits;
    public static ArrayList<WarpLimit> warpLimits;
    public static WarpLimit defaultLimit;

    public static boolean useTimers;
    public static boolean coolDownNotify;
    public static boolean warmUpNotify;
    public static boolean abortOnMove;
    public static boolean abortOnDamage;
    public static ArrayList<Time> warpCooldowns;
    public static Time defaultCooldown;
    public static ArrayList<Time> warpWarmups;
    public static Time defaultWarmup;

    public static boolean usemySQL;
    public static String mySQLhost;
    public static int mySQLport;
    public static String mySQLuname;
    public static String mySQLpass;
    public static String mySQLdb;
    public static String mySQLtable;

    public static boolean useEconomy;
    public static boolean informAfterTransaction;
    public static ArrayList<WarpFees> warpFees;
    public static WarpFees defaultWarpFees;

    public static boolean useDynmap;
    public static boolean hideLayerByDefault;
    public static String layerDisplayName;
    public static int layerPriority;
    public static String markerIconID;
    public static int markerMinZoom;
    public static boolean showMarkerLable;

    private static FileConfiguration config;
    private static File configFile;
    private static MyWarp plugin;

    public static void initialize(MyWarp plugin) {
        WarpSettings.plugin = plugin;
        dataDir = plugin.getDataFolder();
        configFile = new File(dataDir, CONFIG_FILE);
        config = getConfig(configFile);

        warpLimits = new ArrayList<WarpLimit>();
        warpCooldowns = new ArrayList<Time>();
        warpWarmups = new ArrayList<Time>();
        warpFees = new ArrayList<WarpFees>();

        ConfigurationSection confSettings = config
                .getConfigurationSection("settings");
        ConfigurationSection confDynamics = config
                .getConfigurationSection("dynamics");
        ConfigurationSection confLocale = config
                .getConfigurationSection("locale");
        ConfigurationSection confSafety = config
                .getConfigurationSection("warpSafety");
        ConfigurationSection confLimits = config
                .getConfigurationSection("limits");
        ConfigurationSection confTimers = config
                .getConfigurationSection("timers");
        ConfigurationSection confTimersCool = confTimers
                .getConfigurationSection("cooldowns");
        ConfigurationSection confTimersWarm = confTimers
                .getConfigurationSection("warmups");
        ConfigurationSection confDatabase = config
                .getConfigurationSection("mysql");
        ConfigurationSection confEconomy = config
                .getConfigurationSection("economy");
        ConfigurationSection confEconomyFees = confEconomy
                .getConfigurationSection("fees");
        ConfigurationSection confDynmap = config
                .getConfigurationSection("dynmap");

        File oldConfigFile = new File(dataDir, settingsFile);
        if (oldConfigFile.exists()) {
            PropertiesFile file = new PropertiesFile(oldConfigFile);
            // port settings
            confSettings
                    .set("adminPrivateWarps",
                            file.getBoolean("adminPrivateWarps", true,
                                    "Whether or not admins can see private warps in their list"));
            confSettings
                    .set("loadChunks",
                            file.getBoolean("loadChunks", false,
                                    "Force sending of the chunk which people teleport to - default: false"));

            // port limits
            confLimits
                    .set("default.maxTotal",
                            file.getInt("maxPublic", 5,
                                    "Maximum number of public warps any player can make")
                                    + file.getInt("maxPrivate", 10,
                                            "Maximum number of private warps any player can make"));
            confLimits.set("default.maxPublic", file.getInt("maxPublic", 5,
                    "Maximum number of public warps any player can make"));
            confLimits.set("default.maxPrivate", file.getInt("maxPrivate", 10,
                    "Maximum number of private warps any player can make"));

            // port database
            String mySQLconn = file.getString("mySQLconn",
                    "jdbc:mysql://localhost:3306/minecraft",
                    "MySQL Connection (only if using MySQL)");
            mySQLconn = mySQLconn.substring(mySQLconn.indexOf("//") + 2);
            String[] mySQLconnParts = mySQLconn.split("[\\W]");
            confDatabase
                    .set("enabled",
                            file.getBoolean("usemySQL", false,
                                    "MySQL usage --  true = use MySQL database / false = use SQLite"));
            confDatabase.set("host", mySQLconnParts[0]);
            confDatabase.set("port", mySQLconnParts[1]);
            confDatabase.set("database", mySQLconnParts[2]);
            confDatabase.set("username", file.getString("mySQLuname", "root",
                    "MySQL Username (only if using MySQL)"));
            confDatabase.set("password", file.getString("mySQLpass",
                    "password", "MySQL Password (only if using MySQL)"));

            try {
                config.save(configFile);
                if (!oldConfigFile.renameTo(new File(dataDir, settingsFile
                        + ".old"))) {
                    WarpLogger
                            .warning("Could not rename old settings file, better remove it by yourself");
                } else {
                    WarpLogger.info("Successfully ported old settings file");
                }
            } catch (IOException ex) {
                WarpLogger.severe("Failed to port old settings file", ex);
            }
        }

        // settings
        worldAccess = confSettings.getBoolean("controlWorldAccess");
        loadChunks = confSettings.getBoolean("loadChunks");
        warpEffect = confSettings.getBoolean("warpEffect");

        // dynamics
        suggestWarps = confDynamics.getBoolean("suggestWarps");

        // language
        locale = confLocale.getString("locale");

        // saftey
        useWarpSafety = confSafety.getBoolean("enabled");
        searchRadius = confSafety.getInt("searchRadius");
        verticalTolerance = confSafety.getInt("verticalTolerance");

        // limits
        useWarpLimits = confLimits.getBoolean("enabled");
        for (String key : confLimits.getKeys(false)) {
            if (key.equals("enabled")) {
                // ignore the enabled option
                continue;
            } else if (key.equals("default")) {
                defaultLimit = new WarpLimit(key, confLimits.getInt(key
                        + ".maxTotal"), confLimits.getInt(key + ".maxPublic"),
                        confLimits.getInt(key + ".maxPrivate"));
            } else {
                warpLimits.add(new WarpLimit(key, confLimits.getInt(key
                        + ".maxTotal"), confLimits.getInt(key + ".maxPublic"),
                        confLimits.getInt(key + ".maxPrivate")));
            }
        }
        Collections.sort(warpLimits);

        // timers
        useTimers = confTimers.getBoolean("enabled");
        coolDownNotify = confTimers.getBoolean("coolDownNotify");
        warmUpNotify = confTimers.getBoolean("warmUpNotify");
        abortOnMove = confTimers.getBoolean("abortOnMove");
        abortOnDamage = confTimers.getBoolean("abortOnDamage");
        for (String key : confTimersCool.getKeys(false)) {
            if (key.equals("default")) {
                defaultCooldown = new Time(key, confTimersCool.getDouble(key));
            } else {
                warpCooldowns.add(new Time(key, confTimersCool.getDouble(key)));
            }
        }
        Collections.sort(warpCooldowns);
        for (String key : confTimersWarm.getKeys(false)) {
            if (key.equals("default")) {
                defaultWarmup = new Time(key, confTimersWarm.getDouble(key));
            } else {
                warpWarmups.add(new Time(key, confTimersWarm.getDouble(key)));
            }
        }
        Collections.sort(warpWarmups);

        // database
        usemySQL = confDatabase.getBoolean("enabled");
        mySQLhost = confDatabase.getString("host");
        mySQLport = confDatabase.getInt("port");
        mySQLuname = confDatabase.getString("username");
        mySQLpass = confDatabase.getString("password");
        mySQLdb = confDatabase.getString("database");
        mySQLtable = confDatabase.getString("table");

        // economy
        useEconomy = confEconomy.getBoolean("enabled");
        informAfterTransaction = confEconomy
                .getBoolean("informAfterTransaction");

        for (String key : confEconomyFees.getKeys(false)) {
            WarpFees fees = new WarpFees(key, confEconomyFees.getDouble(key
                    + ".accept"), confEconomyFees.getDouble(key + ".create"),
                    confEconomyFees.getDouble(key + ".create-private"),
                    confEconomyFees.getDouble(key + ".delete"),
                    confEconomyFees.getDouble(key + ".give"),
                    confEconomyFees.getDouble(key + ".help"),
                    confEconomyFees.getDouble(key + ".invite"),
                    confEconomyFees.getDouble(key + ".list"),
                    confEconomyFees.getDouble(key + ".listall"),
                    confEconomyFees.getDouble(key + ".point"),
                    confEconomyFees.getDouble(key + ".private"),
                    confEconomyFees.getDouble(key + ".public"),
                    confEconomyFees.getDouble(key + ".search"),
                    confEconomyFees.getDouble(key + ".uninvite"),
                    confEconomyFees.getDouble(key + ".update"),
                    confEconomyFees.getDouble(key + ".warp-player"),
                    confEconomyFees.getDouble(key + ".warp-sign-create"),
                    confEconomyFees.getDouble(key + ".warp-sign-use"),
                    confEconomyFees.getDouble(key + ".warp-to"),
                    confEconomyFees.getDouble(key + ".welcome"));
            if (key.equals("default")) {
                defaultWarpFees = fees;
            } else {
                warpFees.add(fees);
            }
            Collections.sort(warpFees);
        }

        // dynmap
        useDynmap = confDynmap.getBoolean("enabled");
        hideLayerByDefault = confDynmap.getBoolean("layer.hideByDefault");
        layerDisplayName = confDynmap.getString("layer.displayName");
        layerPriority = confDynmap.getInt("layer.priority");
        markerIconID = confDynmap.getString("marker.iconID");
        markerMinZoom = confDynmap.getInt("marker.minZoom");
        showMarkerLable = confDynmap.getBoolean("marker.showLabel");
    }

    private static FileConfiguration getConfig(File file) {
        FileConfiguration config = null;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
                WarpLogger.info("Default config created successfully!");
            }

            config = plugin.getConfig();
            config.setDefaults(YamlConfiguration.loadConfiguration(plugin
                    .getResource(file.getName())));
            config.options().copyDefaults(true);
            config.save(file);
        } catch (Exception e) {
            WarpLogger.warning("Default config could not be created!");
        }

        return config;
    }
}

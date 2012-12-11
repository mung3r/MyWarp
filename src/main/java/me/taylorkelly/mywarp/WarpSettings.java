package me.taylorkelly.mywarp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.timer.Cooldown;
import me.taylorkelly.mywarp.timer.Warmup;
import me.taylorkelly.mywarp.utils.PropertiesFile;
import me.taylorkelly.mywarp.utils.WarpLogger;

public class WarpSettings {
    
    private static final String CONFIG_FILE = "config.yml";
    private static final String settingsFile = "MyWarp.settings";
    public static File dataDir;
    
    public static boolean worldAccess;
    public static boolean loadChunks;
    public static boolean warpEffect;
    
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
    public static ArrayList<Cooldown> warpCooldowns;
    public static Cooldown defaultCooldown;
    public static ArrayList<Warmup> warpWarmups;
    public static Warmup defaultWarmup;
    
    public static boolean usemySQL;
    public static String mySQLhost;
    public static int mySQLport;
    public static String mySQLuname;
    public static String mySQLpass;
    public static String mySQLdb;
    public static String mySQLtable;

    private static FileConfiguration config;
    private static File configFile;
    private static MyWarp plugin;

    public static void initialize(MyWarp plugin) {
        WarpSettings.plugin = plugin;
        dataDir = plugin.getDataFolder();
        configFile = new File(dataDir, CONFIG_FILE);
        config = getConfig(configFile);
        
        warpLimits = new ArrayList<WarpLimit>();
        warpCooldowns = new ArrayList<Cooldown>();
        warpWarmups = new ArrayList<Warmup>();
        
        ConfigurationSection confsettings = config.getConfigurationSection("settings");
        ConfigurationSection conflocale = config.getConfigurationSection("locale");
        ConfigurationSection confsafety = config.getConfigurationSection("warpSafety");
        ConfigurationSection conflimits = config.getConfigurationSection("limits");
        ConfigurationSection conftimers = config.getConfigurationSection("timers");
        ConfigurationSection conftimerscool = conftimers.getConfigurationSection("cooldowns");
        ConfigurationSection conftimerswarm = conftimers.getConfigurationSection("warmups");
        ConfigurationSection confdatabase = config.getConfigurationSection("mysql");
        
        File oldConfigFile  = new File(dataDir, settingsFile);
        if (oldConfigFile.exists()) {
            PropertiesFile file = new PropertiesFile(oldConfigFile);
            //port settings
            confsettings.set("adminPrivateWarps", file.getBoolean("adminPrivateWarps", true, "Whether or not admins can see private warps in their list"));
            confsettings.set("loadChunks", file.getBoolean("loadChunks", false, "Force sending of the chunk which people teleport to - default: false"));

            //port limits
            conflimits.set("default.maxTotal", file.getInt("maxPublic", 5, "Maximum number of public warps any player can make")
                    + file.getInt("maxPrivate", 10, "Maximum number of private warps any player can make"));
            conflimits.set("default.maxPublic", file.getInt("maxPublic", 5, "Maximum number of public warps any player can make"));
            conflimits.set("default.maxPrivate", file.getInt("maxPrivate", 10, "Maximum number of private warps any player can make"));

            //port database
            String mySQLconn = file.getString("mySQLconn", "jdbc:mysql://localhost:3306/minecraft", "MySQL Connection (only if using MySQL)");
            mySQLconn = mySQLconn.substring(mySQLconn.indexOf("//")+2);
            String[] mySQLconnParts = mySQLconn.split("[\\W]");
            confdatabase.set("enabled", file.getBoolean("usemySQL", false, "MySQL usage --  true = use MySQL database / false = use SQLite"));
            confdatabase.set("host", mySQLconnParts[0]);
            confdatabase.set("port", mySQLconnParts[1]);
            confdatabase.set("database", mySQLconnParts[2]);
            confdatabase.set("username", file.getString("mySQLuname", "root", "MySQL Username (only if using MySQL)"));
            confdatabase.set("password", file.getString("mySQLpass", "password", "MySQL Password (only if using MySQL)"));

            try {
                config.save(configFile);
                if (!oldConfigFile.renameTo(new File(dataDir, settingsFile + ".old"))) {
                    WarpLogger.warning("Could not rename old settings file, better remove it by yourself");
                }
                else {
                    WarpLogger.info("Successfully ported old settings file");
                }
            }
            catch (IOException ex) {
                WarpLogger.severe("Failed to port old settings file", ex);
            }
        }
        
        // settings
        worldAccess = confsettings.getBoolean("controlWorldAccess");
        loadChunks = confsettings.getBoolean("loadChunks");
        warpEffect = confsettings.getBoolean("warpEffect");
        
        //language
        locale = conflocale.getString("locale");

        // saftey
        useWarpSafety = confsafety.getBoolean("enabled");
        searchRadius = confsafety.getInt("searchRadius");
        verticalTolerance = confsafety.getInt("verticalTolerance");

        // limits
        useWarpLimits = conflimits.getBoolean("enabled");
        for (String key : conflimits.getKeys(false)) {
            if (key.equals("enabled")) {
                //ignore the enabled option
                continue;
            } else if (key.equals("default")) {
                defaultLimit = new WarpLimit(key, conflimits.getInt(key + ".maxTotal"),
                        conflimits.getInt(key + ".maxPublic"), conflimits.getInt(key
                                + ".maxPrivate"));
            } else {
                warpLimits.add(new WarpLimit(key, conflimits.getInt(key + ".maxTotal"),
                        conflimits.getInt(key + ".maxPublic"), conflimits.getInt(key
                                + ".maxPrivate")));
            }
        }
        Collections.sort(warpLimits);
        
        // timers
        useTimers = conftimers.getBoolean("enabled");
        coolDownNotify = conftimers.getBoolean("coolDownNotify");
        warmUpNotify = conftimers.getBoolean("warmUpNotify");
        abortOnMove = conftimers.getBoolean("abortOnMove");
        abortOnDamage = conftimers.getBoolean("abortOnDamage");
        for (String key : conftimerscool.getKeys(false)) {
            if (key.equals("default")) {
                defaultCooldown = new Cooldown (key, conftimerscool.getDouble(key));
            } else {
                warpCooldowns.add(new Cooldown (key, conftimerscool.getDouble(key)));
            }
        }
        Collections.sort(warpCooldowns);
        for (String key : conftimerswarm.getKeys(false)) {
            if (key.equals("default")) {
                defaultWarmup = new Warmup (key, conftimerswarm.getDouble(key));
            } else {
                warpWarmups.add(new Warmup (key, conftimerswarm.getDouble(key)));
            }
        }
        Collections.sort(warpWarmups);
        

        // database
        usemySQL = confdatabase.getBoolean("enabled");
        mySQLhost = confdatabase.getString("host");
        mySQLport = confdatabase.getInt("port");
        mySQLuname = confdatabase.getString("username");
        mySQLpass = confdatabase.getString("password");
        mySQLdb = confdatabase.getString("database");
        mySQLtable = confdatabase.getString("table");

    }
    
    private static FileConfiguration getConfig(File file)
    {
        FileConfiguration config = null;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
                WarpLogger.info("Default config created successfully!");
            }

            config = plugin.getConfig();
            config.setDefaults(YamlConfiguration.loadConfiguration(plugin.getResource(file.getName())));
            config.options().copyDefaults(true);
            config.save(file);
        }
        catch (Exception e) {
            WarpLogger.warning("Default config could not be created!");
        }

        return config;
    }
}

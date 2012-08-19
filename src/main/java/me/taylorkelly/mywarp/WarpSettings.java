package me.taylorkelly.mywarp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.taylorkelly.mywarp.data.WarpLimit;
import me.taylorkelly.mywarp.utils.PropertiesFile;
import me.taylorkelly.mywarp.utils.WarpLogger;

public class WarpSettings {
    
    private static final String CONFIG_FILE = "config.yml";
    private static final String settingsFile = "MyWarp.settings";
    public static File dataDir;

    public static ArrayList<WarpLimit> warpLimits;
    public static boolean adminPrivateWarps;
    public static boolean loadChunks;
    
    public static boolean usemySQL;
    public static String mySQLuname;
    public static String mySQLpass;
    public static String mySQLconn;

    private static FileConfiguration config;
    private static File configFile;
    private static MyWarp plugin;
    
    public static void initialize(MyWarp plugin) {
        WarpSettings.plugin = plugin;
        dataDir = plugin.getDataFolder();
        configFile = new File(dataDir, CONFIG_FILE);
        config = getConfig(configFile);
        
        warpLimits = new ArrayList<WarpLimit>();
        
        File oldConfigFile  = new File(dataDir, settingsFile);
        if (oldConfigFile.exists()) {
            PropertiesFile file = new PropertiesFile(oldConfigFile);

            config.set("limits.default.maxTotal", file.getInt("maxPublic", 5, "Maximum number of public warps any player can make")
                    + file.getInt("maxPrivate", 10, "Maximum number of private warps any player can make"));
            config.set("limits.default.maxPublic", file.getInt("maxPublic", 5, "Maximum number of public warps any player can make"));
            config.set("limits.default.maxPrivate", file.getInt("maxPrivate", 10, "Maximum number of private warps any player can make"));
            config.set("adminPrivateWarps", file.getBoolean("adminPrivateWarps", true, "Whether or not admins can see private warps in their list"));
            config.set("loadChunks", file.getBoolean("loadChunks", false, "Force sending of the chunk which people teleport to - default: false"));

            config.set("usemySQL", file.getBoolean("usemySQL", false, "MySQL usage --  true = use MySQL database / false = use SQLite"));
            config.set("mySQLconn", file.getString("mySQLconn", "jdbc:mysql://localhost:3306/minecraft", "MySQL Connection (only if using MySQL)"));
            config.set("mySQLuname", file.getString("mySQLuname", "root", "MySQL Username (only if using MySQL)"));
            config.set("mySQLpass", file.getString("mySQLpass", "password", "MySQL Password (only if using MySQL)"));

            config.set("opPermissions", file.getBoolean("opPermissions", true, "Enable OP permissions with SuperPerms"));

            try {
                config.save(configFile);
                if (!oldConfigFile.renameTo(new File(dataDir, settingsFile + ".old"))) {
                    WarpLogger.warning("Could not rename old settings file, better remove it by yourself");
                }
                else {
                    WarpLogger.info("Successfully ported old settings file");
                }
            }
            catch (IOException e) {
                WarpLogger.severe("Failed to port old settings file", e);
            }
        }
        
        adminPrivateWarps = config.getBoolean("adminPrivateWarps");
        loadChunks = config.getBoolean("loadChunks");
        
        usemySQL = config.getBoolean("usemySQL");
        mySQLconn = config.getString("mySQLconn");
        mySQLuname = config.getString("mySQLuname");
        mySQLpass = config.getString("mySQLpass");
        
        for (String key : config.getConfigurationSection("limits").getKeys(false)) {
            warpLimits.add(new WarpLimit(key, config
                    .getInt("limits." + key + ".maxTotal"), config.getInt("limits."
                    + key + ".maxPublic"), config
                    .getInt("limits." + key + ".maxPrivate")));
        }
    }
    
    private static FileConfiguration getConfig(File file)
    {
        FileConfiguration config = null;

        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();
                file.createNewFile();
                InputStream inputStream = plugin.getResource(file.getName());
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[8192];
                int length = 0;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                inputStream.close();
                outputStream.close();

                WarpLogger.info("Default config created successfully!");
            }

            config = plugin.getConfig();
            config.setDefaults(YamlConfiguration.loadConfiguration(plugin.getResource(file.getName())));
            config.options().copyDefaults(true);
        }
        catch (Exception e) {
            WarpLogger.warning("Default config could not be created!");
        }

        return config;
    }
}

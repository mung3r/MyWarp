package me.taylorkelly.mywarp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import me.taylorkelly.mywarp.commands.RootCommands;
import me.taylorkelly.mywarp.data.WarpList;
import me.taylorkelly.mywarp.dataconnections.ConnectionManager;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.economy.EconomyLink;
import me.taylorkelly.mywarp.economy.VaultLink;
import me.taylorkelly.mywarp.listeners.MWBlockListener;
import me.taylorkelly.mywarp.listeners.MWEntityListener;
import me.taylorkelly.mywarp.listeners.MWPlayerListener;
import me.taylorkelly.mywarp.markers.DynmapMarkers;
import me.taylorkelly.mywarp.markers.Markers;
import me.taylorkelly.mywarp.permissions.WarpPermissions;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.WarpLogger;
import me.taylorkelly.mywarp.utils.commands.CommandsManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MyWarp extends JavaPlugin {

    private WarpList warpList;
    private MWBlockListener blockListener;
    private MWEntityListener entityListener;
    private MWPlayerListener playerListener;

    public String name;
    public String version;
    private PluginManager pm;
    public CommandsManager commandManager;
    public static WarpPermissions warpPermissions;
    public static ConnectionManager connectionManager;
    public static Markers markers;
    private EconomyLink economyLink;

    @Override
    public void onDisable() {
        if (connectionManager != null) {
            connectionManager.close();
        }
        Bukkit.getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        name = this.getDescription().getName();
        version = this.getDescription().getVersion();
        pm = getServer().getPluginManager();

        WarpSettings.initialize(this);
        LanguageManager.initialize(this);

        // initialize the connection
        try {
            connectionManager = new ConnectionManager(WarpSettings.usemySQL,
                    true, true, this);
        } catch (DataConnectionException e) {
            WarpLogger
                    .severe("Could not establish database connection. Disabling MyWarp.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // check for old database (h-mod) and convert it
        File newDatabase = new File(getDataFolder(), "warps.db");
        File oldDatabase = new File("homes-warps.db");
        if (!newDatabase.exists() && oldDatabase.exists()) {
            updateFiles(oldDatabase, newDatabase);
        }

        warpList = new WarpList(getServer());
        warpPermissions = new WarpPermissions(this);

        // register event listeners
        blockListener = new MWBlockListener(this);
        entityListener = new MWEntityListener();
        playerListener = new MWPlayerListener(this);

        pm.registerEvents(blockListener, this);
        pm.registerEvents(entityListener, this);
        pm.registerEvents(playerListener, this);
        
        try {
            economyLink = new VaultLink(this);
        } catch (ClassNotFoundException e) {
            WarpLogger.severe("Unable to hook into Vault. Disabling Economy support.");
        }

        // initialize Dynmap support
        if (WarpSettings.useDynmap) {
            if (!pm.isPluginEnabled("dynmap")) {
                WarpLogger
                        .severe("Failed to hook into Dynmap. Disabeling Dynmap support.");
            } else {
                markers = new DynmapMarkers(this);
            }
        }

        // initialize the command manager and register all used commands
        commandManager = new CommandsManager(this);
        commandManager.register(RootCommands.class);

        new CommandUtils(this);

        WarpLogger.info(name + " " + version + " enabled");
    }

    private void updateFiles(File oldDatabase, File newDatabase) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (newDatabase.exists()) {
            newDatabase.delete();
        }
        try {
            newDatabase.createNewFile();
        } catch (IOException ex) {
            WarpLogger.severe("Could not create new database file", ex);
        }
        copyFile(oldDatabase, newDatabase);
    }

    /**
     * File copier from xZise
     * 
     * @param fromFile
     * @param toFile
     */
    private void copyFile(File fromFile, File toFile) {
        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            WarpLogger.severe("Failed to rename " + fromFile.getName() + "to "
                    + toFile.getName() + ": ", ex);
        } finally {

            try {
                if (from != null) {
                    from.close();
                }
                if (to != null) {
                    to.close();
                }
            } catch (IOException e) {
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        return commandManager.handleBukkitCommand(sender, command,
                commandLabel, args);
    }

    public WarpList getWarpList() {
        return warpList;
    }

    public EconomyLink getEconomyLink() {
        return economyLink;
    }
}

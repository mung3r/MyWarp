package me.taylorkelly.mywarp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import me.taylorkelly.mywarp.commands.RootCommands;
import me.taylorkelly.mywarp.data.SignWarp;
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

    public String name;
    public String version;

    private PluginManager pm;

    private WarpList warpList;
    private CommandsManager commandsManager;
    private ConnectionManager connectionManager;
    private Markers markers;
    private SignWarp signWarp;
    private EconomyLink economyLink;

    private static WarpPermissions warpPermissions;

    @Override
    public void onDisable() {
        if (getConnectionManager() != null) {
            getConnectionManager().close();
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

        try {
            connectionManager = new ConnectionManager(WarpSettings.usemySQL,
                    true, true, this);
        } catch (DataConnectionException e) {
            WarpLogger
                    .severe("Could not establish database connection. Disabling MyWarp.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // check for old database and convert it
        File newDatabase = new File(getDataFolder(), "warps.db");
        File oldDatabase = new File("homes-warps.db");
        if (!newDatabase.exists() && oldDatabase.exists()) {
            updateFiles(oldDatabase, newDatabase);
        }

        warpList = new WarpList(this);
        warpPermissions = new WarpPermissions(this);

        // register event listeners
        MWBlockListener blockListener = new MWBlockListener(this);
        MWEntityListener entityListener = new MWEntityListener();
        MWPlayerListener playerListener = new MWPlayerListener(this);

        pm.registerEvents(blockListener, this);
        pm.registerEvents(entityListener, this);
        pm.registerEvents(playerListener, this);

        signWarp = new SignWarp(this);

        // initialize EconomySupport
        if (WarpSettings.useEconomy) {
            try {
                economyLink = new VaultLink(this);
            } catch (NoClassDefFoundError e) {
                WarpLogger
                        .severe("Failed to hook into Vault. Disabling Economy support.");
                WarpSettings.useEconomy = false;
            }
        }

        // initialize Dynmap support
        if (WarpSettings.useDynmap) {
            if (!pm.isPluginEnabled("dynmap")) {
                WarpLogger
                        .severe("Failed to hook into Dynmap. Disabling Dynmap support.");
                WarpSettings.useDynmap = false;
            } else {
                markers = new DynmapMarkers(this);
            }
        }

        // initialize the command manager and register all used commands
        commandsManager = new CommandsManager(this);
        getCommandsManager().register(RootCommands.class);
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
        return getCommandsManager().handleBukkitCommand(sender, command,
                commandLabel, args);
    }

    public WarpList getWarpList() {
        return warpList;
    }

    public EconomyLink getEconomyLink() {
        return economyLink;
    }

    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public Markers getMarkers() {
        return markers;
    }

    public SignWarp getSignWarp() {
        return signWarp;
    }

    public static WarpPermissions getWarpPermissions() {
        return warpPermissions;
    }
}

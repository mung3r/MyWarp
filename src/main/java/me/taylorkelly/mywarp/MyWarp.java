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
import java.util.logging.Level;
import java.util.logging.Logger;

import me.taylorkelly.mywarp.commands.RootCommands;
import me.taylorkelly.mywarp.data.WarpManager;
import me.taylorkelly.mywarp.data.WarpSignManager;
import me.taylorkelly.mywarp.dataconnections.DataConnection;
import me.taylorkelly.mywarp.dataconnections.MySQLConnection;
import me.taylorkelly.mywarp.dataconnections.SQLiteConnection;
import me.taylorkelly.mywarp.economy.EconomyLink;
import me.taylorkelly.mywarp.economy.VaultLink;
import me.taylorkelly.mywarp.localization.LocalizationException;
import me.taylorkelly.mywarp.localization.LocalizationManager;
import me.taylorkelly.mywarp.markers.DynmapMarkers;
import me.taylorkelly.mywarp.markers.Markers;
import me.taylorkelly.mywarp.permissions.PermissionsManager;
import me.taylorkelly.mywarp.timer.TimerManager;
import me.taylorkelly.mywarp.utils.commands.CommandsManager;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;
import org.dynmap.DynmapCommonAPI;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * The MyWarp plugin implementation.
 */
public class MyWarp extends JavaPlugin implements Reloadable {

    /**
     * The plugin instance for MyWarp.
     */
    private static MyWarp instance;

    /**
     * The commands manager, which handles all commands together with arguments,
     * flags etc.
     */
    private CommandsManager commandsManager;

    /**
     * The connection to the configured data-source.
     */
    private DataConnection dataConnection;

    /**
     * The economy Link in use.
     */
    private EconomyLink economyLink;

    /**
     * The language-manger.
     */
    private LocalizationManager localizationManager;

    /**
     * Represents the marker API in use.
     */
    private Markers markers;

    /**
     * The timer-factory.
     */
    private TimerManager timerManager;

    /**
     * The warp-manager.
     */
    private WarpManager warpManager;

    /**
     * The permissions-manage that handles all permission-related tasks.
     */
    private PermissionsManager permissionsManager;

    /**
     * The parsed plugin-configuration.
     */
    private Settings settings;

    /**
     * Constructs the instance.
     */
    public MyWarp() {
        super();

        instance = this;
    }

    /**
     * Returns the plugin's instance.
     * 
     * @return the plugin's instance
     */
    public static MyWarp inst() {
        return instance;
    }

    /**
     * Return the plugin's logger.
     * 
     * @return the logger
     */
    public static Logger logger() {
        return instance.getLogger();
    }

    /**
     * Returns the server instance that runs this plugin.
     * 
     * @return the server instance
     */
    public static Server server() {
        return instance.getServer();
    }

    /**
     * Gets MyWarp's commands-manager.
     * 
     * @return the commands-manager
     */
    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    /**
     * Gets MyWarp's active data-connection that provides access to the active
     * data-source.
     * 
     * This method may return null if the connection is not yet setup.
     * 
     * @return the connection manager
     */
    public DataConnection getDataConnection() {
        return dataConnection;
    }

    /**
     * Gets the economy-link service if it exists, this method should be used
     * for economic actions.
     * 
     * This method can return null. Use {@link #isEconomySetup()} to check if
     * the economy link is setup and usable before accessing the link directly.
     * 
     * @return the economy link
     */
    public EconomyLink getEconomyLink() {
        return economyLink;
    }

    /**
     * Gets MyWarp's localization-manager, that handles all translations.
     * 
     * @return the language manager
     */
    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    /**
     * Gets the {@link Markers} service, this method provides access to all
     * marker-APIs in use
     * 
     * This method can return null. Use {@link #isMarkerSetup()} to check if the
     * Marker service is setup and usable before accessing the link directly.
     * 
     * @return the markers
     */
    public Markers getMarkers() {
        return markers;
    }

    /**
     * Gets MyWarp's permissions-Manager, this method should be used for tasks
     * involving direct permission-access.
     * 
     * @return the permissions manager
     */
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    /**
     * Gets the timer-manager, that manages all per-object timers (warmups,
     * cooldowns...).
     * 
     * @return the timer-manager
     */
    public TimerManager getTimerManager() {
        return timerManager;
    }

    /**
     * Returns the warp-manager that holds all warps.
     * 
     * @return the warp manager
     */
    public WarpManager getWarpManager() {
        return warpManager;
    }

    /**
     * Gets MyWarp's settings which provides direct access to the configuration.
     * 
     * @return the warp settings
     */
    public Settings getSettings() {
        return settings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(CommandSender, Command, String, String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        return getCommandsManager().handleBukkitCommand(sender, command, commandLabel, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        // close all open database connections
        if (getDataConnection() != null) {
            getDataConnection().close();
        }
        // remove cached resource bundles
        if (localizationManager != null) {
            localizationManager.reload();
        }
        // unregister dynamic permissions
        if (permissionsManager != null) {
            permissionsManager.unregisterPermissions();
        }
        // cancel all pending tasks
        getServer().getScheduler().cancelTasks(this);

        // null the static instance to prevent memory leaks
        instance = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // setup the configurations
        settings = new BukkitSettings(new File(getDataFolder(), "config.yml"),
                YamlConfiguration.loadConfiguration(getTextResource("config.yml")));

        // initialize the command manager and register all used commands
        commandsManager = new CommandsManager();
        getCommandsManager().register(RootCommands.class);

        // rename extremely old sqlite-database file - this needs to be done
        // before creating the database connection
        File newDatabase = new File(getDataFolder(), "warps.db");
        File oldDatabase = new File("homes-warps.db");
        if (!newDatabase.exists() && oldDatabase.exists()) {
            if (!FileUtil.copy(oldDatabase, newDatabase)) {
                logger().severe(
                        "Failed to copy " + oldDatabase.getName() + "to " + newDatabase.getName()
                                + ", the old database will be ignored!");
            } else {
                logger().info("Your old SQlite database has been copied to the new format.");
            }
        }

        // initialize the database connection
        ListenableFuture<DataConnection> futureConnection;
        if (getSettings().isMysqlEnabled()) {
            futureConnection = MySQLConnection.getConnection(getSettings().getMysqlHostAdress(),
                    getSettings().getMysqlPort(), getSettings().getMysqlDatabaseName(), getSettings()
                            .getMysqlUsername(), getSettings().getMysqlPassword(), true);
        } else {
            futureConnection = SQLiteConnection.getConnection(new File(getDataFolder(), "mywarps.db"), true);
        }
        try {
            // block main thread until we have a connection
            dataConnection = futureConnection.get();
        } catch (Exception e) {
            logger().log(Level.SEVERE, "Could not establish database connection. Disabling MyWarp.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // initialize language support
        try {
            localizationManager = new LocalizationManager();
        } catch (LocalizationException e) {
            logger().log(Level.SEVERE, "Failed to access bundled localization files. Disabling MyWarp.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // setup the core functions
        warpManager = new WarpManager();
        setupPlugin();
    }

    /**
     * This method setups non-core functions of the plugin (put short, this
     * functions can be enabled/disabled at runtime). Core functions MUST be
     * setup correctly before executing this method.
     */
    private void setupPlugin() {
        // block the main thread until the warps are loaded
        try {
            warpManager.populate(getDataConnection().getWarps().get());
            logger().info(warpManager.getLoadedWarpNumber() + " warps loaded.");
        } catch (Exception e) {
            MyWarp.logger().log(Level.SEVERE, "Failed to load warps from database.", e);
        }

        // register dynamic permissions
        permissionsManager = new PermissionsManager();

        // initialize timers
        if (getSettings().isTimersEnabled()) {
            timerManager = new TimerManager();
        }

        // initialize warp-signs
        if (getSettings().isWarpSignsEnabled()) {
            getServer().getPluginManager().registerEvents(
                    new WarpSignManager(getSettings().getWarpSignsIdentifiers()), this);
        }

        // initialize EconomySupport
        if (getSettings().isEconomyEnabled()) {
            try {
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class);
                Preconditions.checkNotNull(economyProvider, "EconomyProvider cannnot be null.");
                economyLink = new VaultLink(economyProvider);
            } catch (NoClassDefFoundError e) {
                // economy provider class is not present
                logger().severe(
                        "Failed to hook into Vault (EconomyProviderClass not available). Disabling Economy support.");
            } catch (NullPointerException e) {
                // economy provider is not registered
                logger().severe(
                        "Failed to hook into Vault (" + e.getMessage() + "). Disabling Economy support.");
            }
        }

        // initialize Dynmap support
        if (getSettings().isDynmapEnabled()) {
            Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap");
            if (dynmap != null && dynmap.isEnabled()) {
                markers = new DynmapMarkers((DynmapCommonAPI) dynmap);
            } else {
                logger().severe("Failed to hook into Dynmap. Disabling Dynmap support.");
            }
        }
    }

    /**
     * Returns whether the economy-link is setup and usable.
     * 
     * @return true if the economy-link is set up
     */
    public boolean isEconomySetup() {
        return economyLink != null;
    }

    /**
     * Returns whether Markers are setup and usable.
     * 
     * @return true if Markers are set up
     */
    public boolean isMarkerSetup() {
        return markers != null;
    }

    @Override
    public void reload() {
        // unload old stuff from the server
        HandlerList.unregisterAll(this);
        // REVIEW make permissionManager reloadable?
        permissionsManager.unregisterPermissions();
        // REVIEW move into warpManager?
        warpManager.clear();

        // load new stuff
        settings.reload();
        localizationManager.reload();
        setupPlugin();
    }
}

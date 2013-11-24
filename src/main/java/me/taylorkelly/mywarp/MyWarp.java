package me.taylorkelly.mywarp;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.taylorkelly.mywarp.commands.RootCommands;
import me.taylorkelly.mywarp.data.EventListener;
import me.taylorkelly.mywarp.data.WarpManager;
import me.taylorkelly.mywarp.data.WarpSignManager;
import me.taylorkelly.mywarp.dataconnections.ConnectionManager;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.economy.EconomyLink;
import me.taylorkelly.mywarp.economy.VaultLink;
import me.taylorkelly.mywarp.localization.LocalizationManager;
import me.taylorkelly.mywarp.localization.LocalizationException;
import me.taylorkelly.mywarp.markers.DynmapMarkers;
import me.taylorkelly.mywarp.markers.Markers;
import me.taylorkelly.mywarp.permissions.PermissionsManager;
import me.taylorkelly.mywarp.timer.TimerFactory;
import me.taylorkelly.mywarp.utils.commands.CommandsManager;
import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.Validate;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;
import org.dynmap.DynmapCommonAPI;

public class MyWarp extends JavaPlugin implements Reloadable {

    /**
     * The plugin instance for MyWarp
     */
    private static MyWarp instance;

    /**
     * The commands manager, which handles all commands together with arguments,
     * flags etc.
     */
    private CommandsManager commandsManager;

    /**
     * Manages the database connections
     */
    private ConnectionManager connectionManager;

    /**
     * The economy Link in use
     */
    private EconomyLink economyLink;

    /**
     * The language-manger
     */
    private LocalizationManager localizationManager;

    /**
     * Represents the marker API in use
     */
    private Markers markers;

    /**
     * The timer-factory
     */
    private TimerFactory timerFactory;

    /**
     * The warp-manager
     */
    private WarpManager warpManager;

    /**
     * the permissions-manage that handles all permission-related tasks
     */
    private PermissionsManager permissionsManager;

    /**
     * The parsed plugin-configuration
     */
    private WarpSettings warpSettings;

    /**
     * Constructs the instance
     */
    public MyWarp() {
        instance = this;
    }

    /**
     * Returns the plugin's instance
     * 
     * @return the plugin's instance
     */
    public static MyWarp inst() {
        return instance;
    }

    /**
     * Return the plugin's logger
     * 
     * @return the logger
     */
    public static Logger logger() {
        return instance.getLogger();
    }

    /**
     * Returns the server instance that runs this plugin
     * 
     * @return the server instance
     */
    public static Server server() {
        return instance.getServer();
    }

    /**
     * Gets MyWarp's {@link CommandsManager}
     * 
     * @return the commands-manager
     */
    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    /**
     * Gets MyWarp's {@link ConnectionManager}, this method should be used for
     * all database access.
     * 
     * This method can return null.
     * 
     * @return the connection manager
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Gets the {@link EconomyLink} service if it exists, this method should be
     * used for economic actions.
     * 
     * This method can return null.
     * 
     * @return the economy link
     */
    public EconomyLink getEconomyLink() {
        return economyLink;
    }

    /**
     * Gets MyWarp's {@link LocalizationManager}, that handles all translations
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
     * This method can return null.
     * 
     * @return the markers
     */
    public Markers getMarkers() {
        return markers;
    }

    /**
     * Gets MyWarp's {@link PermissionsManager}, this method should be used for
     * tasks involving direct permission-access
     * 
     * @return the permissions manager
     */
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    /**
     * Gets the timer-factory, necessary for all tasks involving per-object
     * timers (warmups, cooldowns...)
     * 
     * @return the timer-factory
     */
    public TimerFactory getTimerFactory() {
        return timerFactory;
    }

    /**
     * Returns the warp-manager that holds all warps
     * 
     * @return the warp manager
     */
    public WarpManager getWarpManager() {
        return warpManager;
    }

    /**
     * Gets MyWarp's {@link WarpSettings}, and therefore provides direct access
     * to the settings
     * 
     * @return the warp settings
     */
    public WarpSettings getWarpSettings() {
        return warpSettings;
    }

    /**
     * Called on command-execution
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        return getCommandsManager().handleBukkitCommand(sender, command, commandLabel, args);
    }

    /**
     * Called when the plugin is disabled
     */
    @Override
    public void onDisable() {
        // close all open database connections
        if (getConnectionManager() != null) {
            getConnectionManager().close();
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

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {

        // setup the configurations
        warpSettings = new WarpSettings();

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
        try {
            connectionManager = new ConnectionManager(getWarpSettings().mysqlEnabled, true, true);
        } catch (DataConnectionException e) {
            logger().severe("Could not establish database connection. Disabling MyWarp.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // initialize language support
        try {
            localizationManager = new LocalizationManager();
        } catch (LocalizationException e) {
            logger().log(Level.SEVERE, "Failed to acces bundled localization files. Disabling MyWarp.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // setup the core functions
        permissionsManager = new PermissionsManager();
        warpManager = new WarpManager();

        setupPlugin();
    }

    /**
     * This method setups non-core functions of the plugin (put short, this
     * functions can be enabled/disabled at runtime). Core functions MUST be
     * setup correctly before executing this method.
     */
    private void setupPlugin() {
        // register dynamic permissions
        permissionsManager.registerPermissions();

        // initialize timers
        if (getWarpSettings().timersEnabled) {
            timerFactory = new TimerFactory();
        }

        // initialize EconomySupport
        if (getWarpSettings().economyEnabled) {
            try {
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class);
                Validate.notNull(economyProvider, "EconomyProvider cannnot be null.");
                economyLink = new VaultLink(economyProvider);
            } catch (NoClassDefFoundError e) {
                // economy provider class is not present
                logger().severe(
                        "Failed to hook into Vault (EconomyProviderClass not available). Disabling Economy support.");
                getWarpSettings().economyEnabled = false;
            } catch (NullPointerException e) {
                // economy provider is not registered
                logger().severe(
                        "Failed to hook into Vault (" + e.getMessage() + "). Disabling Economy support.");
                getWarpSettings().economyEnabled = false;
            }
        }

        // initialize Dynmap support
        if (getWarpSettings().dynmapEnabled) {
            Plugin dynmap = getServer().getPluginManager().getPlugin("dynmap");
            if (dynmap != null && dynmap.isEnabled()) {
                markers = new DynmapMarkers((DynmapCommonAPI) dynmap);
            } else {
                logger().severe("Failed to hook into Dynmap. Disabling Dynmap support.");
                getWarpSettings().dynmapEnabled = false;
            }
        }

        // register events
        if (getWarpSettings().warpSignsEnabled) {
            getServer().getPluginManager().registerEvents(new WarpSignManager(), this);
        }
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void reload() {
        // unload old stuff from the server
        HandlerList.unregisterAll(this);
        permissionsManager.unregisterPermissions();

        // load new stuff
        getWarpSettings().reload();
        getLocalizationManager().reload();
        setupPlugin();
    }
}
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
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;
import org.dynmap.DynmapCommonAPI;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

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
     * The connection to the configured data-source
     */
    private DataConnection dataConnection;

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
    private TimerManager timerManager;

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
        super();

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
     * Gets MyWarp's active {@link DataConnection} that provides access to the
     * active data-source.
     * 
     * This method may return null if the connection is not yet setup.
     * 
     * @return the connection manager
     */
    public DataConnection getDataConnection() {
        return dataConnection;
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
     * Gets the timer-manager, that manages all per-object timers (warmups,
     * cooldowns...)
     * 
     * @return the timer-manager
     */
    public TimerManager getTimerManager() {
        return timerManager;
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
        ListenableFuture<DataConnection> futureConnection;
        if (getWarpSettings().mysqlEnabled) {
            futureConnection = MySQLConnection.getConnection(getWarpSettings().mysqlHost,
                    getWarpSettings().mysqlPort, getWarpSettings().mysqlDatabase,
                    getWarpSettings().mysqlUsername, getWarpSettings().mysqlPassword, true);
        } else {
            futureConnection = SQLiteConnection.getConnection(new File(getDataFolder(), "mywarps.db"), true);
        }
        try {
            // block main thread until we have a connection
            dataConnection = futureConnection.get();
        } catch (Exception e) {
            logger().severe(
                    "Could not establish database connection (" + e.getMessage() + "). Disabling MyWarp.");
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
        if (getWarpSettings().timersEnabled) {
            timerManager = new TimerManager();
        }

        // initialize warp-signs
        if (getWarpSettings().warpSignsEnabled) {
            getServer().getPluginManager().registerEvents(new WarpSignManager(), this);
        }

        // initialize EconomySupport
        if (getWarpSettings().economyEnabled) {
            try {
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class);
                Preconditions.checkNotNull(economyProvider, "EconomyProvider cannnot be null.");
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
        getWarpSettings().reload();
        getLocalizationManager().reload();
        setupPlugin();
    }
}
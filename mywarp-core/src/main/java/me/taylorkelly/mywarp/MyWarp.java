/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.taylorkelly.mywarp.dataconnections.DataConnection;
import me.taylorkelly.mywarp.dataconnections.EventConnectionBridge;
import me.taylorkelly.mywarp.dataconnections.MySQLConnection;
import me.taylorkelly.mywarp.dataconnections.SQLiteConnection;
import me.taylorkelly.mywarp.economy.DummyEconomyManager;
import me.taylorkelly.mywarp.economy.EconomyManager;
import me.taylorkelly.mywarp.economy.SimpleEconomyManager;
import me.taylorkelly.mywarp.limits.DummyLimitManager;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.limits.SimpleLimitManager;
import me.taylorkelly.mywarp.safety.TeleportService;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.EventWarpManager;
import me.taylorkelly.mywarp.warp.Warp;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Entry point and container for a working MyWarp implementation.
 */
public class MyWarp {

    private static final Logger LOG = Logger.getLogger(MyWarp.class.getName());

    private static MyWarp instance;

    private final EventWarpManager warpManager;
    private final Platform platform;
    private final TeleportService teleportService;

    private DataConnection dataConnection;
    private EconomyManager economyManager;
    private LimitManager limitManager;

    /**
     * Creates an instance of MyWarp, running on the given Platform.
     * 
     * @param platform
     *            the Platform MyWarp runs on
     * @throws MyWarpException
     *             if the initialization fail and MyWarp is unable to continue
     */
    public MyWarp(Platform platform) throws MyWarpException {
        this.platform = platform;

        // setup dataConnection
        ListenableFuture<DataConnection> futureConnection;
        if (getSettings().isMysqlEnabled()) {
            futureConnection = MySQLConnection.getConnection(getSettings().getMysqlDsn(), getSettings()
                    .getMysqlUsername(), getSettings().getMysqlPassword(), true);
        } else {
            futureConnection = SQLiteConnection.getConnection(
                    new File(platform.getDataFolder(), "mywarp.db"), true);
        }
        try {
            dataConnection = futureConnection.get();
        } catch (InterruptedException e) {
            throw new MyWarpException(
                    "Failed to get a connection to the database as the process was interrupted.", e);
        } catch (ExecutionException e) {
            throw new MyWarpException("Failed to get a connection to the database.", e.getCause());
        }

        // setup the warpManager
        warpManager = new EventWarpManager(new EventConnectionBridge(dataConnection));

        // setup TeleportService
        teleportService = new TeleportService();

        // setup the rest of the plugin
        setupPlugin();

        instance = this;
    }

    /**
     * Gets the static instance of MyWarp.
     * 
     * @return the running MyWarp instance
     * @throws IllegalStateException
     *             if MyWarp has not yet been initialized
     */
    // REVIEW remove singleton logic?
    public static MyWarp getInstance() throws IllegalStateException {
        Preconditions.checkState(instance != null, "MyWarp is not yet initialized."); // NON-NLS
        return instance;
    }

    /**
     * Sets up the plugin.
     */
    private void setupPlugin() {
        // setup the limitManager
        if (platform.getSettings().isLimitsEnabled()) {
            limitManager = new SimpleLimitManager(platform.getLimitProvider(), warpManager);
        } else {
            limitManager = new DummyLimitManager(warpManager);
        }

        // setup the economyManager
        try {
            if (platform.getSettings().isEconomyEnabled()) {
                economyManager = new SimpleEconomyManager(platform.getFeeProvider(),
                        platform.getEconomyService());
            } else {
                economyManager = new DummyEconomyManager();
            }
        } catch (UnsupportedOperationException e) {
            economyManager = new DummyEconomyManager();
        }

        // Populate the warpManager when the warps are loaded. The callback is
        // executed in the server thread.
        LOG.info("Loading warps..."); // NON-NLS
        Futures.addCallback(getDataConnection().getWarps(), new FutureCallback<Collection<Warp>>() {

            @Override
            public void onSuccess(Collection<Warp> result) {
                warpManager.populate(result);
                LOG.info(warpManager.getSize() + " warps loaded."); // NON-NLS
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.log(Level.SEVERE, "Failed to load warps from the database.", t); // NON-NLS
            }

        }, getServerExecutor());

    }

    /**
     * Reloads MyWarp.
     */
    public void reload() {
        // cleanup
        warpManager.clear();
        DynamicMessages.clearCache();

        // setup new stuff
        platform.reload();
        setupPlugin();
    }

    /**
     * Unloads MyWarp. Using this method will effectively shutdown MyWarp. If
     * its called, the platform running MyWarp <b>must</b> unload or deactivate
     * MyWarp too or things will get ugly.
     */
    public void unload() {
        if (dataConnection != null) {
            dataConnection.close();
        }
    }

    /**
     * Gets the DataConnection of this MyWarp instance.
     * 
     * @return the DataConnection
     */
    public DataConnection getDataConnection() {
        return dataConnection;
    }

    /**
     * Gets the WarpManager of this MyWarp instance.
     * 
     * @return the WarpManager
     */
    public EventWarpManager getWarpManager() {
        return warpManager;
    }

    /**
     * Gets the TeleportService.
     * 
     * @return the TeleportService
     */
    public TeleportService getTeleportService() {
        return teleportService;
    }

    /**
     * Gets the EconomyManager. Calling this method will always return valid
     * EconomyManager implementation, if economy support is disabled on the
     * configuration file, the returned EconomyManager will handle this
     * internally and fail quietly.
     * 
     * @return the EconomyManager
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Gets the LimitManager. Calling this method will always return valid
     * LimitManager implementation, if limit support is disabled on the
     * configuration file, the returned LimitManager will handle this internally
     * and fail quietly.
     * 
     * @return the LimitManager
     */
    public LimitManager getLimitManager() {
        return limitManager;
    }

    /**
     * Gets the Platform running MyWarp.
     * 
     * @return the Platform
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Gets the ProfileService.
     * 
     * @return the ProfileService
     */
    public ProfileService getProfileService() {
        return platform.getProfileService();
    }

    /**
     * Gets the Settings of this MyWarp instance.
     * 
     * @return the Settings
     */
    public Settings getSettings() {
        return platform.getSettings();
    }

    /**
     * Gets the ResourceBundle.Control implementation used by this Platform.
     * 
     * @return the ResourceBundle.Control
     */
    public ResourceBundle.Control getResourceBundleControl() {
        return platform.getResourceBundleControl();
    }

    /**
     * Gets the DurationProvider.
     * 
     * @return the DurationProvider
     */
    public DurationProvider getDurationProvider() {
        return platform.getDurationProvider();
    }

    /**
     * Gets the TimerService.
     * 
     * @return the TimerService
     */
    public TimerService getTimerService() {
        return platform.getTimerService();
    }

    /**
     * Gets an ImmutableSet with all worlds currently loaded on the server.
     * 
     * @return an ImmutableSet with all loaded worlds
     */
    public ImmutableSet<LocalWorld> getLoadedWorlds() {
        return platform.getLoadedWorlds();
    }

    /**
     * Gets an Optional containing the loaded world of the given name, if such a
     * world exists.
     * 
     * @param name
     *            the name of the world
     * @return an Optional containing the loaded world
     */
    public Optional<LocalWorld> getLoadedWorld(String name) {
        return platform.getLoadedWorld(name);
    }

    /**
     * Gets an Optional containing the loaded world of the unique identifier, if
     * such a world exists.
     * 
     * @param uniqueId
     *            the unique identifier of the world
     * @return an Optional containing the loaded world
     */
    public Optional<LocalWorld> getLoadedWorld(UUID uniqueId) {
        return platform.getLoadedWorld(uniqueId);
    }

    /**
     * Gets an Optional containing the online player of the given name, if such
     * a player exists.
     * 
     * @param name
     *            the name of the player
     * @return an Optional containing the player
     */
    public Optional<LocalPlayer> getOnlinePlayer(String name) {
        return platform.getOnlinePlayer(name);
    }

    /**
     * Gets an Optional containing the online player of the given UUID, if such
     * a player exists.
     * 
     * @param identifier
     *            the identifier of the player
     * @return an Optional containing the player
     */
    public Optional<LocalPlayer> getOnlinePlayer(UUID identifier) {
        return platform.getOnlinePlayer(identifier);
    }

    /**
     * Gets an Optional containing the online player of the given Profile, if
     * such a player exists.
     * 
     * @param profile
     *            the Profile of the player
     * @return an Optional containing the player
     */
    public Optional<LocalPlayer> getOnlinePlayer(Profile profile) {
        return getOnlinePlayer(profile.getUniqueId());
    }

    /**
     * Get an Executor that executes given Runnables in the main server-thread
     * ('synchronous').
     * 
     * @return the Executor
     */
    public Executor getServerExecutor() {
        return platform.getServerExecutor();
    }

}

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
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.Executor;

import me.taylorkelly.mywarp.economy.EconomyService;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.limits.LimitProvider;
import me.taylorkelly.mywarp.timer.DurationProvider;
import me.taylorkelly.mywarp.timer.TimerService;
import me.taylorkelly.mywarp.util.profile.ProfileService;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a platform MyWarp has been adapted to run on.
 */
public interface Platform {

    /**
     * Reloads the platform.
     */
    void reload();

    /**
     * Gets the data-folder when running in this Platform. The folder is
     * expected to exist and be read- and writable for MyWarp.
     * 
     * @return the data-folder
     */
    File getDataFolder();

    /**
     * Gets the ResourceBundle.Control implementation used by this Platform.
     * 
     * @return the ResourceBundle.Control
     */
    ResourceBundle.Control getResourceBundleControl();

    /**
     * Gets the Settings implementation of this Platform.
     * 
     * @return the Settings
     */
    Settings getSettings();

    /**
     * Gets the ProfileService implementation of this platform.
     * 
     * @return the ProfileService
     */
    ProfileService getProfileService();

    /**
     * Gets the EconomyService implementation of this platform.
     * 
     * @return the EconomyService
     */
    EconomyService getEconomyService();

    /**
     * Gets the FeeProvider implementation of this platform.
     * 
     * @return the FeeProvider
     */
    FeeProvider getFeeProvider();

    /**
     * Gets the LimitProvider implementation of this platform.
     * 
     * @return the LimitProvider
     */
    LimitProvider getLimitProvider();

    /**
     * Gets the DurationProvider implementation of this platform.
     * 
     * @return the DurationProvider
     */
    DurationProvider getDurationProvider();

    /**
     * Gets the TimerService implementation of this platform.
     * 
     * @return the TimerService
     */
    TimerService getTimerService();

    /**
     * Gets an Executor that executes given Runnables in the server thread of
     * this platform ('synchronous').
     * 
     * @return the Executor
     */
    Executor getServerExecutor();

    /**
     * Gets an ImmutableSet with all worlds currently loaded on the server.
     * 
     * @return an ImmutableSet with all loaded worlds
     */
    ImmutableSet<LocalWorld> getLoadedWorlds();

    /**
     * Gets an Optional containing the loaded world of the given name, if such a
     * world exists.
     * 
     * @param name
     *            the name of the world
     * @return an Optional containing the loaded world
     */
    Optional<LocalWorld> getLoadedWorld(String name);

    /**
     * Gets an Optional containing the loaded world of the given unique
     * identifier, if such a world exists.
     * 
     * @param uniqueId
     *            the unique Identifier of the world
     * @return an Optional containing the loaded world
     */
    Optional<LocalWorld> getLoadedWorld(UUID uniqueId);

    /**
     * Gets an Optional containing the online player of the given name, if such
     * a player exists.
     * 
     * @param name
     *            the name of the player
     * @return an Optional containing the player
     */
    Optional<LocalPlayer> getOnlinePlayer(String name);

    /**
     * Gets an Optional containing the online player of the given identifier, if
     * such a player exists.
     * 
     * @param identifier
     *            the identifier of the player
     * @return an Optional containing the player
     */
    Optional<LocalPlayer> getOnlinePlayer(UUID identifier);

}

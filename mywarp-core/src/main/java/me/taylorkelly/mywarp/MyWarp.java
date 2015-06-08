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

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import me.taylorkelly.mywarp.dataconnections.AsyncWritingDataConnection;
import me.taylorkelly.mywarp.dataconnections.DataConnection;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.dataconnections.DataConnectionFactory;
import me.taylorkelly.mywarp.economy.DummyEconomyManager;
import me.taylorkelly.mywarp.economy.EconomyManager;
import me.taylorkelly.mywarp.economy.SimpleEconomyManager;
import me.taylorkelly.mywarp.limits.DummyLimitManager;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.limits.SimpleLimitManager;
import me.taylorkelly.mywarp.safety.CubicLocationSafety;
import me.taylorkelly.mywarp.safety.TeleportService;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.EventfulWarpManager;
import me.taylorkelly.mywarp.warp.MemoryWarpManager;
import me.taylorkelly.mywarp.warp.PersistentWarpManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.WarpSignManager;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Entry point and container for a working MyWarp implementation. <p> An instance of this class holds and manages
 * MyWarp's internal logic. It is initialized with a {@link me.taylorkelly.mywarp.Platform} which provides MyWarp with
 * the needed connection to the game.</p>
 */
public class MyWarp {

  private static final Logger log = MyWarpLogger.getLogger(MyWarp.class);

  private final Platform platform;
  private final WarpManager warpManager;
  private final DataConnection dataConnection;
  private final EventBus eventBus;

  private EconomyManager economyManager;
  private LimitManager limitManager;
  private WarpSignManager warpSignManager;

  private TeleportService teleportService;

  /**
   * Creates an instance of MyWarp, running on the given Platform.
   *
   * @param platform the Platform MyWarp runs on
   * @throws InitializationException if the initialization fails and MyWarp is unable to continue
   */
  public MyWarp(final Platform platform) throws InitializationException {
    this.platform = platform;

    final ListeningExecutorService executorService = platform.getDataService().getExecutorService();

    try {
      dataConnection =
          new AsyncWritingDataConnection(
              DataConnectionFactory.createInitialized(MyWarp.this, platform.getDataService().getDataSource()),
              executorService);

    } catch (DataConnectionException e) {
      throw new InitializationException("Failed to get a connection to the database.", e);
    }

    eventBus = new EventBus();

    // setup the warpManager
    warpManager = new EventfulWarpManager(new PersistentWarpManager(new MemoryWarpManager(), dataConnection), eventBus);

    DynamicMessages.setControl(platform.getResourceBundleControl());

    // setup TeleportService
    teleportService = new TeleportService(new CubicLocationSafety(), getSettings());

    // setup the rest of the plugin
    setupPlugin();
  }

  /**
   * Sets up the plugin.
   */
  private void setupPlugin() {
    // setup the limitManager
    if (getSettings().isLimitsEnabled()) {
      limitManager = new SimpleLimitManager(platform.getLimitProvider(), warpManager);
    } else {
      limitManager = new DummyLimitManager(platform.getGame(), warpManager);
    }

    // setup the economyManager
    try {
      if (getSettings().isEconomyEnabled()) {
        economyManager =
            new SimpleEconomyManager(getSettings(), platform.getFeeProvider(), platform.getEconomyService());
      } else {
        economyManager = new DummyEconomyManager();
      }
    } catch (UnsupportedOperationException e) {
      economyManager = new DummyEconomyManager();
    }

    //TODO this might not be needed
    warpSignManager = new WarpSignManager(getSettings().getWarpSignsIdentifiers(), economyManager, warpManager);

    log.info("Loading warps...");
    ListenableFuture<List<Warp>>
        futureWarps =
        platform.getDataService().getExecutorService().submit(new Callable<List<Warp>>() {
          @Override
          public List<Warp> call() throws Exception {
            return dataConnection.getWarps();
          }
        });

    Futures.addCallback(futureWarps, new FutureCallback<Collection<Warp>>() {

      @Override
      public void onSuccess(Collection<Warp> result) {
        warpManager.populate(result);
        log.info("{} warps loaded.", warpManager.getSize());
      }

      @Override
      public void onFailure(Throwable throwable) {
        log.error("Failed to load warps from the database.", throwable);
      }

    }, platform.getGame().getExecutor());

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
   * Gets the TeleportService.
   *
   * @return the TeleportService
   */
  public TeleportService getTeleportService() {
    return teleportService;
  }

  /**
   * Gets the EconomyManager. Calling this method will always return valid EconomyManager implementation, if economy
   * support is disabled on the configuration file, the returned EconomyManager will handle this internally and fail
   * quietly.
   *
   * @return the EconomyManager
   */
  public EconomyManager getEconomyManager() {
    return economyManager;
  }

  /**
   * Gets the LimitManager. Calling this method will always return valid LimitManager implementation, if limit support
   * is disabled on the configuration file, the returned LimitManager will handle this internally and fail quietly.
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
  public WarpManager getWarpManager() {
    return warpManager;
  }

  /**
   * Gets the WarpSignManager instance of this MyWarp instance.
   *
   * @return the WarpSignManager
   */
  public WarpSignManager getWarpSignManager() {
    return warpSignManager;
  }


  /**
   * Gets the Settings instance of this MyWarp instance.
   *
   * @return the Settings
   */
  public Settings getSettings() {
    return platform.getSettings();
  }

  /**
   * Gets the ProfileService instance of this MyWarp instance.
   *
   * @return the ProfileService
   */
  public ProfileService getProfileService() {
    return platform.getProfileService();
  }

  /**
   * Gets the Game instance of this MyWarp instance.
   *
   * @return the Game
   */
  public Game getGame() {
    return platform.getGame();
  }

  /**
   * Gets the internal EventBus that keeps track of {@link me.taylorkelly.mywarp.warp.event.WarpEvent}s.
   *
   * @return the EventBus
   */
  public EventBus getEventBus() {
    return eventBus;
  }
}

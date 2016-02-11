/*
 * Copyright (C) 2011 - 2016, MyWarp team and contributors
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

import com.google.common.base.Optional;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.economy.DummyEconomyService;
import me.taylorkelly.mywarp.economy.EconomyService;
import me.taylorkelly.mywarp.economy.InformativeEconomyService;
import me.taylorkelly.mywarp.limits.DummyLimitService;
import me.taylorkelly.mywarp.limits.LimitService;
import me.taylorkelly.mywarp.limits.SimpleLimitService;
import me.taylorkelly.mywarp.storage.AsyncWritingWarpStorage;
import me.taylorkelly.mywarp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.storage.RelationalDataService;
import me.taylorkelly.mywarp.storage.StorageInitializationException;
import me.taylorkelly.mywarp.storage.WarpStorage;
import me.taylorkelly.mywarp.storage.WarpStorageFactory;
import me.taylorkelly.mywarp.teleport.CubicSafetyValidationStrategy;
import me.taylorkelly.mywarp.teleport.PositionValidationStrategy;
import me.taylorkelly.mywarp.teleport.TeleportService;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.EventfulWarpManager;
import me.taylorkelly.mywarp.warp.MemoryWarpManager;
import me.taylorkelly.mywarp.warp.StorageWarpManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationService;
import me.taylorkelly.mywarp.warp.authorization.PermissionAuthorizationStrategy;
import me.taylorkelly.mywarp.warp.authorization.WarpPropertiesAuthorizationStrategy;
import me.taylorkelly.mywarp.warp.authorization.WorldAccessAuthorizationStrategy;

import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Entry point and container for a working MyWarp implementation. <p> An instance of this class holds and manages
 * MyWarp's internal logic. It is initialized with a {@link me.taylorkelly.mywarp.Platform} which provides MyWarp with
 * the connection to the game.</p>
 */
public final class MyWarp {

  private static final Logger log = MyWarpLogger.getLogger(MyWarp.class);

  private final Platform platform;
  private final RelationalDataService dataService;
  private final WarpStorage warpStorage;
  private final WarpManager warpManager;
  private final EventBus eventBus;
  private final AuthorizationService authorizationService;
  private final CommandHandler commandHandler;

  private TeleportService teleportService;
  private EconomyService economyService;
  private LimitService limitService;

  /**
   * Creates an instance of MyWarp, running on the given Platform.
   *
   * @param platform the Platform MyWarp runs on
   * @throws InitializationException if the initialization fails and MyWarp is unable to continue
   */
  public MyWarp(Platform platform) throws InitializationException {
    this.platform = platform;

    DynamicMessages.setControl(platform.getResourceBundleControl());

    ConnectionConfiguration connectionConfiguration = platform.getSettings().getRelationalStorageConfiguration();
    dataService = platform.createDataService(connectionConfiguration);
    try {
      warpStorage =
          new AsyncWritingWarpStorage(
              WarpStorageFactory.createInitialized(this, dataService.getDataSource(), connectionConfiguration),
              dataService.getExecutorService());

    } catch (SQLException e) {
      throw new InitializationException("Failed to get a connection to the database.", e);
    } catch (StorageInitializationException e) {
      throw new InitializationException("Failed to get a connection to the database.", e);
    }

    eventBus = new EventBus();

    warpManager = new EventfulWarpManager(new StorageWarpManager(new MemoryWarpManager(), warpStorage), eventBus);

    authorizationService =
        new AuthorizationService(new WorldAccessAuthorizationStrategy(
            new PermissionAuthorizationStrategy(new WarpPropertiesAuthorizationStrategy()), platform.getSettings()));

    setupPlugin();

    commandHandler = new CommandHandler(this);
  }

  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  /**
   * Sets up the plugin.
   */
  private void setupPlugin() {
    //setup the teleportService
    PositionValidationStrategy positionValidationStrategy = new PositionValidationStrategy() {
      @Override
      public Optional<Vector3> getValidPosition(Vector3 originalPosition, LocalWorld world) {
        return Optional.of(originalPosition);
      }
    };
    if (getSettings().isSafetyEnabled()) {
      positionValidationStrategy = new CubicSafetyValidationStrategy(getSettings().getSafetySearchRadius());
    }
    teleportService = new TeleportService(positionValidationStrategy);

    // setup the limitService
    if (getSettings().isLimitsEnabled()) {
      limitService = new SimpleLimitService(platform.getLimitProvider(), warpManager);
    } else {
      limitService = new DummyLimitService(platform.getGame(), warpManager);
    }

    // setup the economyService
    try {
      if (getSettings().isEconomyEnabled()) {
        economyService =
            new InformativeEconomyService(getSettings(), platform.getFeeProvider(), platform.getEconomyService());
      } else {
        economyService = new DummyEconomyService();
      }
    } catch (UnsupportedOperationException e) {
      economyService = new DummyEconomyService();
    }

    log.info("Loading warps...");
    ListenableFuture<List<Warp>> futureWarps = dataService.getExecutorService().submit(new Callable<List<Warp>>() {
      @Override
      public List<Warp> call() throws Exception {
        return warpStorage.getWarps();
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
   * Gets the Platform running MyWarp.
   *
   * @return the Platform
   */
  public Platform getPlatform() {
    return platform;
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
   * Gets the ProfileService instance of this MyWarp instance.
   *
   * @return the ProfileService
   */
  public ProfileService getProfileService() {
    return platform.getProfileService();
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
   * Gets the WarpManager of this MyWarp instance.
   *
   * @return the WarpManager
   */
  public WarpManager getWarpManager() {
    return warpManager;
  }

  /**
   * Gets the internal EventBus that keeps track of {@link me.taylorkelly.mywarp.warp.event.WarpEvent}s.
   *
   * @return the EventBus
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  /**
   * Gets the AuthorizationService instance of this MyWarp instance.
   *
   * @return the AuthorizationService
   */
  public AuthorizationService getAuthorizationService() {
    return authorizationService;
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
   * Gets the EconomyService. Calling this method will always return valid EconomyService implementation, if economy
   * support is disabled on the configuration file, the returned EconomyService will handle this internally and fail
   * quietly.
   *
   * @return the EconomyService
   */
  public EconomyService getEconomyService() {
    return economyService;
  }

  /**
   * Gets the LimitService. Calling this method will always return valid LimitService implementation, if limit support
   * is disabled on the configuration file, the returned LimitService will handle this internally and fail quietly.
   *
   * @return the LimitService
   */
  public LimitService getLimitService() {
    return limitService;
  }
}

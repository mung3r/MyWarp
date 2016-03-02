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

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.Platform;
import me.taylorkelly.mywarp.platform.Settings;
import me.taylorkelly.mywarp.platform.capability.EconomyCapability;
import me.taylorkelly.mywarp.platform.event.PostInitializationEvent;
import me.taylorkelly.mywarp.platform.event.ReloadEvent;
import me.taylorkelly.mywarp.platform.event.WarpsLoadedEvent;
import me.taylorkelly.mywarp.service.teleport.strategy.ChainedValidationStrategy;
import me.taylorkelly.mywarp.service.teleport.strategy.CubicSafetyValidationStrategy;
import me.taylorkelly.mywarp.service.teleport.strategy.LegacyPositionCorrectionStrategy;
import me.taylorkelly.mywarp.service.teleport.strategy.PositionValidationStrategy;
import me.taylorkelly.mywarp.sign.WarpSignManager;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.teleport.StrategicTeleportHandler;
import me.taylorkelly.mywarp.util.teleport.TeleportHandler;
import me.taylorkelly.mywarp.warp.EventfulWarpManager;
import me.taylorkelly.mywarp.warp.MemoryWarpManager;
import me.taylorkelly.mywarp.warp.StorageWarpManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;
import me.taylorkelly.mywarp.warp.authorization.PermissionAuthorizationStrategy;
import me.taylorkelly.mywarp.warp.authorization.WarpPropertiesAuthorizationStrategy;
import me.taylorkelly.mywarp.warp.authorization.WorldAccessAuthorizationStrategy;
import me.taylorkelly.mywarp.warp.storage.AsyncWritingWarpStorage;
import me.taylorkelly.mywarp.warp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.warp.storage.RelationalDataService;
import me.taylorkelly.mywarp.warp.storage.StorageInitializationException;
import me.taylorkelly.mywarp.warp.storage.WarpStorage;
import me.taylorkelly.mywarp.warp.storage.WarpStorageFactory;

import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Entry point and container for a working MyWarp implementation.
 *
 * <p> An instance of this class holds and manages MyWarp's internal logic. It is initialized with a {@link Platform}
 * which provides MyWarp with the connection to the game.</p>
 */
public final class MyWarp {

  private static final Logger log = MyWarpLogger.getLogger(MyWarp.class);

  private final Platform platform;
  private final RelationalDataService dataService;
  private final WarpStorage warpStorage;
  private final WarpManager warpManager;
  private final EventBus eventBus;
  private final AuthorizationResolver authorizationResolver;

  private CommandHandler commandHandler;
  private TeleportHandler teleportHandler;

  /**
   * Creates an instance of MyWarp, operating on the given Platform.
   *
   * @param platform the Platform MyWarp operates on
   * @throws InitializationException if the initialization fails and MyWarp is unable to continue
   */
  public MyWarp(Platform platform) throws InitializationException {
    this.platform = platform;

    ConnectionConfiguration connectionConfiguration = getSettings().getRelationalStorageConfiguration();
    dataService = platform.createDataService(connectionConfiguration);
    try {
      warpStorage =
          new AsyncWritingWarpStorage(WarpStorageFactory
                                          .createInitialized(dataService.getDataSource(), connectionConfiguration,
                                                             platform.getProfileCache()),
                                      dataService.getExecutorService());

    } catch (SQLException e) {
      throw new InitializationException("Failed to get a connection to the database.", e);
    } catch (StorageInitializationException e) {
      throw new InitializationException("Failed to get a connection to the database.", e);
    }

    eventBus = new EventBus();

    warpManager = new EventfulWarpManager(new StorageWarpManager(new MemoryWarpManager(), warpStorage), eventBus);

    authorizationResolver = new AuthorizationResolver(new WorldAccessAuthorizationStrategy(
            new PermissionAuthorizationStrategy(new WarpPropertiesAuthorizationStrategy()), getGame(), getSettings()));

    setupPlugin();
  }

  /**
   * Reloads MyWarp.
   *
   * <p>Reloading will remove all loaded warps from the active WarpManager and reload them from the configured storage.
   * Interaction models (commands, signs...) are newly created. The platform running MyWarp may reload the user
   * configuration from disk.</p>
   */
  public void reload() {
    // cleanup
    warpManager.clear();
    DynamicMessages.clearCache();

    eventBus.post(new ReloadEvent());

    // setup new stuff
    setupPlugin();
  }

  /**
   * Gets the CommandHandler that holds and executes all of MyWarp's commands.
   *
   * @return the CommandHandler
   */
  public CommandHandler getCommandHandler() {
    return commandHandler;
  }

  /**
   * Gets the internal EventBus that keeps track of internal events thrown by MyWarp.
   *
   * @return the EventBus
   */
  public EventBus getEventBus() {
    return eventBus;
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
   * Gets the AuthorizationResolver instance of this MyWarp instance.
   *
   * @return the AuthorizationResolver
   */
  public AuthorizationResolver getAuthorizationResolver() {
    return authorizationResolver;
  }

  /**
   * Gets the TeleportHandler instance of this MyWarp instance.
   *
   * @return the teleportHandler
   */
  public TeleportHandler getTeleportHandler() {
    return teleportHandler;
  }

  /**
   * Creates a new WarpSignManager that hooks into the WarpManager configured for this MyWarp instance.
   *
   * @return a new WarpSign instance
   */
  public WarpSignManager createWarpSignHandler() {
    return new WarpSignManager(getSettings().getWarpSignsIdentifiers(), warpManager, authorizationResolver, getGame(),
                               teleportHandler, platform.getCapability(EconomyCapability.class));
  }

  private void setupPlugin() {

    teleportHandler = createTeleportHandler();
    commandHandler = new CommandHandler(this, platform);

    eventBus.post(new PostInitializationEvent());

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

        eventBus.post(new WarpsLoadedEvent());
        log.info("{} warps loaded.", warpManager.getSize());
      }

      @Override
      public void onFailure(Throwable throwable) {
        log.error("Failed to load warps from the database.", throwable);
      }

    }, platform.getGame().getExecutor());

  }

  private TeleportHandler createTeleportHandler() {
    List<PositionValidationStrategy> validationStrategies = new ArrayList<PositionValidationStrategy>();
    validationStrategies.add(new LegacyPositionCorrectionStrategy());

    if (getSettings().isSafetyEnabled()) {
      validationStrategies.add(new CubicSafetyValidationStrategy(getSettings()));
    }
    return new StrategicTeleportHandler(new ChainedValidationStrategy(validationStrategies));
  }
}

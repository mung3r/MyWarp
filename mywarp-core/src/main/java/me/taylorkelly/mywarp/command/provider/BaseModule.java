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

package me.taylorkelly.mywarp.command.provider;

import com.sk89q.intake.parametric.AbstractModule;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.annotation.WarpName;
import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.profile.Profile;
import me.taylorkelly.mywarp.platform.profile.ProfileCache;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;
import me.taylorkelly.mywarp.warp.storage.ConnectionConfiguration;

import java.io.File;

/**
 * Provides most of MyWarp's internal objects by converting the user given arguments.
 */
public class BaseModule extends AbstractModule {

  private final WarpManager warpManager;
  private final AuthorizationResolver authorizationResolver;
  private final ProfileCache profileCache;
  private final Game game;
  private CommandHandler commandHandler;
  private File base;

  /**
   * Creates an instance.
   *
   * @param warpManager          the WarpManager to use
   * @param authorizationResolver the AuthorizationResolver to use
   * @param profileCache       the ProfileCache to use
   * @param game                 the Game to use
   * @param commandHandler       the CommandHandler to use
   * @param base                 the base File to use
   */
  public BaseModule(WarpManager warpManager, AuthorizationResolver authorizationResolver, ProfileCache profileCache,
                    Game game, CommandHandler commandHandler, File base) {
    this.warpManager = warpManager;
    this.authorizationResolver = authorizationResolver;
    this.profileCache = profileCache;
    this.game = game;
    this.commandHandler = commandHandler;
    this.base = base;
  }

  @Override
  protected void configure() {
    bind(LocalPlayer.class).toProvider(new PlayerProvider(game));
    bind(Profile.class).toProvider(new ProfileProvider(profileCache));
    bind(Warp.class).toProvider(new WarpProvider(authorizationResolver, warpManager));
    bind(String.class).annotatedWith(WarpName.class).toProvider(new WarpNameProvider(warpManager, commandHandler));
    bind(ConnectionConfiguration.class).toProvider(new ConnectionConfigurationProvider());
    bind(File.class).toProvider(new FileProvider(base));
  }
}

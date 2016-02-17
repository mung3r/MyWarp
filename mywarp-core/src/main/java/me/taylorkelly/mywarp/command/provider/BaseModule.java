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

import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.annotation.WarpName;
import me.taylorkelly.mywarp.storage.ConnectionConfiguration;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationService;

import java.io.File;

/**
 * Provides most of MyWarp's internal objects by converting the user given arguments.
 */
public class BaseModule extends AbstractModule {

  private final WarpManager warpManager;
  private final AuthorizationService authorizationService;
  private final ProfileService profileService;
  private final Game game;
  private CommandHandler commandHandler;
  private File base;

  /**
   * Creates an instance.
   *
   * @param warpManager          the WarpManager to use
   * @param authorizationService the AuthorizationService to use
   * @param profileService       the ProfileService to use
   * @param game                 the Game to use
   * @param commandHandler       the CommandHandler to use
   * @param base                 the base File to use
   */
  public BaseModule(WarpManager warpManager, AuthorizationService authorizationService, ProfileService profileService,
                    Game game, CommandHandler commandHandler, File base) {
    this.warpManager = warpManager;
    this.authorizationService = authorizationService;
    this.profileService = profileService;
    this.game = game;
    this.commandHandler = commandHandler;
    this.base = base;
  }

  @Override
  protected void configure() {
    bind(LocalPlayer.class).toProvider(new PlayerProvider(game));
    bind(Profile.class).toProvider(new ProfileProvider(profileService));
    bind(Warp.class).toProvider(new WarpProvider(authorizationService, warpManager));
    bind(String.class).annotatedWith(WarpName.class).toProvider(new WarpNameProvider(warpManager, commandHandler));
    bind(ConnectionConfiguration.class).toProvider(new ConnectionConfigurationProvider());
    bind(File.class).toProvider(new FileProvider(base));
  }
}

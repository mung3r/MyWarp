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

package me.taylorkelly.mywarp.command.parametric.provider;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Predicate;
import com.sk89q.intake.parametric.AbstractModule;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.parametric.annotation.Modifiable;
import me.taylorkelly.mywarp.command.parametric.annotation.Usable;
import me.taylorkelly.mywarp.command.parametric.annotation.Viewable;
import me.taylorkelly.mywarp.command.parametric.annotation.WarpName;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.PlayerNameResolver;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;
import me.taylorkelly.mywarp.warp.storage.ConnectionConfiguration;

import java.io.File;
import java.util.UUID;

/**
 * Provides most of MyWarp's internal objects by converting the user given arguments.
 */
public class BaseModule extends AbstractModule {

  private final WarpManager warpManager;
  private final AuthorizationResolver authorizationResolver;
  private final PlayerNameResolver playerNameResolver;
  private final Game game;
  private CommandHandler commandHandler;
  private File base;

  /**
   * Creates an instance.
   *
   * @param warpManager           the WarpManager to use
   * @param authorizationResolver the AuthorizationResolver to use
   * @param playerNameResolver    the PlayerNameResolver to use
   * @param game                  the Game to use
   * @param commandHandler        the CommandHandler to use
   * @param base                  the base File to use
   */
  public BaseModule(WarpManager warpManager, AuthorizationResolver authorizationResolver,
                    PlayerNameResolver playerNameResolver, Game game, CommandHandler commandHandler, File base) {
    this.warpManager = warpManager;
    this.authorizationResolver = authorizationResolver;
    this.playerNameResolver = playerNameResolver;
    this.game = game;
    this.commandHandler = commandHandler;
    this.base = base;
  }

  @Override
  protected void configure() {
    //game related objects
    bind(LocalPlayer.class).toProvider(new PlayerProvider(game));
    bind(UUID.class).toProvider(new PlayerIdentifierProvider(playerNameResolver));

    //warps
    bind(Warp.class).annotatedWith(Viewable.class).toProvider(new WarpProvider(authorizationResolver, warpManager) {
      @Override
      Predicate<Warp> isValid(AuthorizationResolver resolver, Actor actor) {
        return resolver.isViewable(actor);
      }
    });
    bind(Warp.class).annotatedWith(Modifiable.class).toProvider(new WarpProvider(authorizationResolver, warpManager) {
      @Override
      Predicate<Warp> isValid(AuthorizationResolver resolver, Actor actor) {
        return resolver.isModifiable(actor);
      }
    });
    bind(Warp.class).annotatedWith(Usable.class).toProvider(new WarpProvider(authorizationResolver, warpManager) {
      @Override
      Predicate<Warp> isValid(AuthorizationResolver resolver, Actor actor) {
        checkArgument(actor instanceof LocalEntity, "This Binding must be used by an LocalEntity");
        return resolver.isUsable((LocalEntity) actor);
      }
    });

    //warp name
    bind(String.class).annotatedWith(WarpName.class).toProvider(new WarpNameProvider(warpManager, commandHandler));

    //configuration
    bind(ConnectionConfiguration.class).toProvider(new ConnectionConfigurationProvider());
    bind(File.class).toProvider(new FileProvider(base));
  }
}

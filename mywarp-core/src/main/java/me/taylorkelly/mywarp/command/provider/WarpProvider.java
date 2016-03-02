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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import me.taylorkelly.mywarp.command.annotation.Name;
import me.taylorkelly.mywarp.command.provider.exception.NoSuchWarpException;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.LocalEntity;
import me.taylorkelly.mywarp.util.IterableUtils;
import me.taylorkelly.mywarp.util.MatchList;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Provides {@link Warp} instances.
 */
class WarpProvider implements Provider<Warp> {

  private final AuthorizationResolver authorizationResolver;
  private WarpManager warpManager;

  WarpProvider(AuthorizationResolver authorizationResolver, WarpManager warpManager) {
    this.authorizationResolver = authorizationResolver;
    this.warpManager = warpManager;
  }

  @Override
  public boolean isProvided() {
    return false;
  }

  @Nullable
  @Override
  public Warp get(CommandArgs arguments, List<? extends Annotation> modifiers)
      throws ArgumentException, ProvisionException {

    //parse @Name annotation
    Optional<Name> name = IterableUtils.getFirst(Iterables.filter(modifiers, Name.class));
    if (!name.isPresent()) {
      throw new IllegalArgumentException("Warps must be used with an '@Name' annotation");
    }
    Name.Condition conditionValue = name.get().value();

    Namespace namespace = arguments.getNamespace();
    Actor actor = namespace.get(Actor.class);
    if (actor == null || !(conditionValue.getUserClass().isAssignableFrom(actor.getClass()))) {
      throw new IllegalArgumentException(
          "This Binding must be used by " + conditionValue.getUserClass().getCanonicalName() + "s.");
    }

    //build authorization
    Predicate<Warp> predicate = null;
    switch (conditionValue) {
      case MODIFIABLE:
        predicate = authorizationResolver.isModifiable(actor);
        break;
      case USABLE:
        predicate = authorizationResolver.isUsable((LocalEntity) actor);
        break;
      case VIEWABLE:
        predicate = authorizationResolver.isViewable(actor);
        break;
    }

    //query WarpManager
    String query = arguments.next();

    MatchList matches = new MatchList(query, warpManager.filter(predicate));
    Optional<Warp> exactMatch = matches.getExactMatch();

    if (!exactMatch.isPresent()) {
      throw new NoSuchWarpException(query, matches);
    }
    return exactMatch.get();
  }

  @Override
  public List<String> getSuggestions(String prefix) {
    //TODO implement
    return Collections.emptyList();
  }

}

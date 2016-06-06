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

import com.google.common.base.Optional;
import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import me.taylorkelly.mywarp.command.parametric.provider.exception.NoSuchProfileException;
import me.taylorkelly.mywarp.platform.profile.Profile;
import me.taylorkelly.mywarp.platform.profile.ProfileCache;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Provides {@link Profile} instances.
 */
class ProfileProvider implements Provider<Profile> {

  private final ProfileCache profileCache;

  ProfileProvider(ProfileCache profileCache) {
    this.profileCache = profileCache;
  }

  @Override
  public boolean isProvided() {
    return false;
  }

  @Nullable
  @Override
  public Profile get(CommandArgs arguments, List<? extends Annotation> modifiers)
      throws ArgumentException, ProvisionException {
    String query = arguments.next();

    Optional<Profile> optional = profileCache.getByName(query);

    if (!optional.isPresent()) {
      throw new NoSuchProfileException(query);
    }
    return optional.get();
  }

  @Override
  public List<String> getSuggestions(String prefix, Namespace namespace) {
    //TODO implement
    return Collections.emptyList();
  }

}

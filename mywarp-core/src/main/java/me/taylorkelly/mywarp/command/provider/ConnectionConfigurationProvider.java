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

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;

import me.taylorkelly.mywarp.warp.storage.ConnectionConfiguration;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Provides {@link ConnectionConfiguration} instances.
 */
class ConnectionConfigurationProvider implements Provider<ConnectionConfiguration> {

  @Override
  public boolean isProvided() {
    return false;
  }

  @Nullable
  @Override
  public ConnectionConfiguration get(CommandArgs arguments, List<? extends Annotation> modifiers)
      throws ArgumentException, ProvisionException {
    ConnectionConfiguration config = new ConnectionConfiguration(arguments.next());

    if (config.supportsSchemas()) {
      config.setSchema(arguments.next());
    }
    if (config.supportsAuthentication()) {
      config.setUser(arguments.next());
      config.setPassword(arguments.next());
    }
    return config;
  }

  @Override
  public List<String> getSuggestions(String prefix) {
    return Collections.emptyList();
  }
}

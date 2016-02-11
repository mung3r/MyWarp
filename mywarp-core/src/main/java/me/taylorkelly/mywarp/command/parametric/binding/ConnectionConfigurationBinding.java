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

package me.taylorkelly.mywarp.command.parametric.binding;

import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

import me.taylorkelly.mywarp.storage.ConnectionConfiguration;

/**
 * A binding for {@link ConnectionConfiguration}s.
 */
public class ConnectionConfigurationBinding extends BindingHelper {

  /**
   * Gets a {@code ConnectionConfiguration} from the given {@code context}. Values will be resolved in the following
   * order, unsupported ones will be skipped: <ul> <li>Database URL,</li> <li>Schema,</li> <li>User,</li>
   * <li>Password.</li> </ul>
   *
   * @param context the command's context
   * @return the {@code ConnectionConfiguration}
   * @throws ParameterException on a parameter error
   */
  @BindingMatch(type = ConnectionConfiguration.class, behavior = BindingBehavior.CONSUMES, consumedCount = -1)
  public ConnectionConfiguration getConnectionConfiguration(ArgumentStack context) throws ParameterException {
    ConnectionConfiguration config = new ConnectionConfiguration(context.next());

    if (config.supportsSchemas()) {
      config.setSchema(context.next());
    }
    if (config.supportsAuthentication()) {
      config.setUser(context.next());
      config.setPassword(context.next());
    }
    return config;

  }
}

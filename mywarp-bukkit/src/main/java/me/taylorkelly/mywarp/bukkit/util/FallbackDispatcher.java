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

package me.taylorkelly.mywarp.bukkit.util;

import com.google.common.base.Joiner;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.SimpleDispatcher;
import com.sk89q.intake.util.auth.AuthorizationException;
import com.sk89q.intake.util.i18n.ResourceProvider;

import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;

import java.util.Arrays;

/**
 * A custom Dispatcher implementation that, instead of simply throwing an InvalidUsageException if the given arguments
 * do not match any of the registered sub-commands, tries to parse se input against a registered fallback command.
 */
public class FallbackDispatcher extends SimpleDispatcher {

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  private final CommandCallable fallback;

  /**
   * Creates an instance, using the given ResourceProvider to resolve internal resources. The given fallback is called
   * whenever the input does not match any of the registered commands.
   *
   * @param resourceProvider the ResourceProvider
   * @param fallback         the fallback callable
   */
  public FallbackDispatcher(ResourceProvider resourceProvider, CommandCallable fallback) {
    super(resourceProvider);
    this.fallback = fallback;
  }

  @Override
  public boolean testPermission(CommandLocals locals) {
    return fallback.testPermission(locals) || super.testPermission(locals);
  }

  @Override
  public boolean call(String arguments, CommandLocals locals, String[] parentCommands)
      throws CommandException, AuthorizationException {
    // We have permission for this command if we have permissions for
    // subcommands
    if (!testPermission(locals)) {
      throw new AuthorizationException();
    }

    String[] split = CommandContext.split(arguments);
    if (split.length > 0) {
      String subCommand = split[0];
      String subArguments = Joiner.on(" ").join(Arrays.copyOfRange(split, 1, split.length));
      String[] subParents = Arrays.copyOf(parentCommands, parentCommands.length + 1);
      subParents[parentCommands.length] = subCommand;
      CommandMapping mapping = get(subCommand);

      if (mapping != null) {
        try {
          mapping.getCallable().call(subArguments, locals, subParents);
        } catch (AuthorizationException e) {
          throw e;
        } catch (CommandException e) {
          e.prependStack(subCommand);
          throw e;
        } catch (Throwable t) {
          throw new InvocationCommandException(t);
        }

        return true;
      }

    }

    // At this point, either split is empty (we fail fast) or there is no
    // sub-command matching the given argument so the fallback command should handle it.
    try {
      fallback.call(Joiner.on(" ").join(split), locals, parentCommands);
    } catch (CommandException e) {
      throw e;
    } catch (Throwable t) {
      throw new InvocationCommandException(t);
    }
    return true;
  }

}

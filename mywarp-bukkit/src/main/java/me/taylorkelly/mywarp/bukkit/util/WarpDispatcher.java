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
import com.sk89q.intake.CommandException;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.InvalidUsageException;
import com.sk89q.intake.InvocationCommandException;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.SimpleDispatcher;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.util.auth.AuthorizationException;

import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding.IllegalCommandSenderException;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.NoSuchWarpException;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.Arrays;

/**
 * A custom Dispatcher implementation that, instead of simply throwing an InvalidUsageException if the given arguments
 * do not match any of the registered sub-commands, tries to send the command caller to a Warp matching these
 * arguments.
 */
public class WarpDispatcher extends SimpleDispatcher {

  private final ExceptionConverter converter;
  private final PlayerBinding playerBinding;
  private final WarpBinding warpBinding;
  private final UsageCommands usageCommands;

  /**
   * Creates an instance.
   *
   * @param converter     the ExceptionConverter
   * @param playerBinding the PlayerBinding
   * @param warpBinding   the WarpBinding
   * @param usageCommands the UsageCommands
   */
  public WarpDispatcher(ExceptionConverter converter, PlayerBinding playerBinding, WarpBinding warpBinding,
                        UsageCommands usageCommands) {
    this.converter = converter;
    this.playerBinding = playerBinding;
    this.warpBinding = warpBinding;
    this.usageCommands = usageCommands;
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
    // sub-command matching the given argument (try to send the command
    // caller to a Warp matching the given argument).
    try {
      to(split, locals);
    } catch (NoSuchWarpException e) {
      converter.convert(e);
    } catch (ParameterException e) {
      converter.convert(e);
    } catch (IllegalCommandSenderException e) {
      converter.convert(e);
    } catch (TimerRunningException e) {
      converter.convert(e);
    }
    return true;
  }

  /**
   * Teleports the command caller to the Warp matching the first argument, if the command caller is a player, only one
   * argument is given and that argument matches an warp usable by the command caller and all requirements listed for
   * {@link UsageCommands#to(LocalPlayer, Warp)} are meat.
   *
   * @param arguments the arguments
   * @param locals    the CommandLocals
   * @throws InvalidUsageException         if {@code arguments} does not have exactly one entry
   * @throws NoSuchWarpException           if there is no Warp matching the argument or is usable by the command caller
   * @throws ParameterException            if the given CommandLocals do not contain a mapping for the {@link
   *                                       me.taylorkelly.mywarp.Actor} class
   * @throws IllegalCommandSenderException if the command caller is not a player
   * @throws TimerRunningException         if the command caller already has some sort of timer running
   */
  private void to(String[] arguments, CommandLocals locals)
      throws InvalidUsageException, NoSuchWarpException, ParameterException, IllegalCommandSenderException,
             TimerRunningException {
    if (arguments.length != 1) {
      // TODO localize!
      throw new InvalidUsageException("Please choose a sub-command.", this, true);
    }

    LocalPlayer player = playerBinding.getPlayer(locals);
    Warp warp = warpBinding.getWarp(arguments[0], WarpUtils.isUsable(player));

    usageCommands.to(player, warp);
  }

}

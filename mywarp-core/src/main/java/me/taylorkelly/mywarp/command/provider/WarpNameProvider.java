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

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.provider.exception.InvalidWarpName;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Provides {@link String} instances that are valid can be used as a name for a {@link Warp}.
 */
class WarpNameProvider implements Provider<String> {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private WarpManager warpManager;
  private CommandHandler commandHandler;

  /**
   * Creates an instance. Provides Strings will be validated against the given {@code warpManager} and the given {@code
   * CommandHandler}.
   *
   * @param warpManager    the WarpManager
   * @param commandHandler the CommandHandler
   */
  public WarpNameProvider(WarpManager warpManager, CommandHandler commandHandler) {
    this.warpManager = warpManager;
    this.commandHandler = commandHandler;
  }

  @Override
  public boolean isProvided() {
    return false;
  }

  @Nullable
  @Override
  public String get(CommandArgs arguments, List<? extends Annotation> modifiers)
      throws ArgumentException, ProvisionException {
    String name = arguments.next();

    if (warpManager.contains(name)) {
      throw new InvalidWarpName(name, InvalidWarpName.Reason.ALREADY_EXISTS);
    }
    if (name.length() > WarpUtils.MAX_NAME_LENGTH) {
      throw new InvalidWarpName(name, InvalidWarpName.Reason.TOO_LONG);
    }
    if (commandHandler.isSubCommand(name)) {
      throw new InvalidWarpName(name, InvalidWarpName.Reason.IS_CMD);
    }

    return name;
  }

  @Override
  public List<String> getSuggestions(String prefix) {
    return Collections.emptyList();
  }
}

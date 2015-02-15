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

import com.sk89q.intake.CommandException;
import com.sk89q.intake.SettableDescription;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.parametric.ParameterData;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.handler.InvokeHandler;
import com.sk89q.intake.parametric.handler.InvokeListener;

import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;

import java.lang.reflect.Method;

/**
 * By registering this InvokeHandler at a {@link com.sk89q.intake.parametric.ParametricBuilder}, command methods created
 * by this builder will have internationalized descriptions and help texts.
 */
public class I18nInvokeHandler implements InvokeListener, InvokeHandler {

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  @Override
  public InvokeHandler createInvokeHandler() {
    return this;
  }

  // FIXME (INTAKE) that only updates the description once, but it needs to be
  // dynamic
  @Override
  public void updateDescription(Object object, Method method, ParameterData[] parameters,
                                SettableDescription description) {
    // replace the description with the localized version
    String descriptionKey = description.getShortDescription();
    if (descriptionKey != null && !descriptionKey.isEmpty()) {
      description.setDescription(MESSAGES.getString(descriptionKey));
    }

    // replace the help with the localized version
    String helpKey = description.getHelp();
    if (helpKey != null && !helpKey.isEmpty()) {
      description.setHelp(MESSAGES.getString(helpKey));
    }
  }

  @Override
  public boolean preProcess(Object object, Method method, ParameterData[] parameters, CommandContext context,
                            CommandLocals locals) throws CommandException, ParameterException {
    return true;
  }

  @Override
  public boolean preInvoke(Object object, Method method, ParameterData[] parameters, Object[] args,
                           CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
    return true;
  }

  @Override
  public void postInvoke(Object object, Method method, ParameterData[] parameters, Object[] args,
                         CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
  }

}

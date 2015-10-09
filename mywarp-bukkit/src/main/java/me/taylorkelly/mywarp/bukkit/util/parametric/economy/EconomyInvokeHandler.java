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

package me.taylorkelly.mywarp.bukkit.util.parametric.economy;

import com.sk89q.intake.CommandException;
import com.sk89q.intake.context.CommandContext;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.parametric.ParameterData;
import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.handler.AbstractInvokeListener;
import com.sk89q.intake.parametric.handler.InvokeHandler;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.economy.EconomyService;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;

import java.lang.reflect.Method;

/**
 * By registering this InvokeHandler at a {@link com.sk89q.intake.parametric.ParametricBuilder}, command methods created
 * by this builder will require a certain fee when used if the method is annotated with {@link Billable}.
 */
public class EconomyInvokeHandler extends AbstractInvokeListener implements InvokeHandler {

  private final EconomyService economyService;

  /**
   * Creates an instance.
   *
   * @param economyService the EconomyService uses to handle economy tasks
   */
  public EconomyInvokeHandler(EconomyService economyService) {
    this.economyService = economyService;
  }

  @Override
  public InvokeHandler createInvokeHandler() {
    return this;
  }

  @Override
  public boolean preProcess(Object object, Method method, ParameterData[] parameters, CommandContext context,
                            CommandLocals locals) throws CommandException, ParameterException {
    return true;
  }

  @Override
  public boolean preInvoke(Object object, Method method, ParameterData[] parameters, Object[] args,
                           CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
    if (!method.isAnnotationPresent(Billable.class)) {
      return true;
    }
    Actor actor = locals.get(Actor.class);
    if (actor == null || !(actor instanceof LocalPlayer)) {
      return true;
    }

    FeeType feeType = method.getAnnotation(Billable.class).value();
    return economyService.hasAtLeast((LocalPlayer) actor, feeType);
  }

  @Override
  public void postInvoke(Object object, Method method, ParameterData[] parameters, Object[] args,
                         CommandContext context, CommandLocals locals) throws CommandException, ParameterException {
    if (!method.isAnnotationPresent(Billable.class)) {
      return;
    }
    Actor actor = locals.get(Actor.class);
    if (actor == null || !(actor instanceof LocalPlayer)) {
      return;
    }

    FeeType feeType = method.getAnnotation(Billable.class).value();
    economyService.withdraw((LocalPlayer) actor, feeType);
  }

}

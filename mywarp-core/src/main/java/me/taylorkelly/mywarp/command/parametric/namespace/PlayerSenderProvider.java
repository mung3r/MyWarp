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

package me.taylorkelly.mywarp.command.parametric.namespace;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.parametric.ProvisionException;

import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.LocalPlayer;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Provides an {@link LocalPlayer} from the command's {@link Namespace}, mapped under {@code Actor.class}.
 */
class PlayerSenderProvider extends NonProvidingProvider<LocalPlayer> {

  private final ActorProvider actorProvider = new ActorProvider();

  @Override
  public LocalPlayer get(CommandArgs arguments, List<? extends Annotation> modifiers)
          throws ArgumentException, ProvisionException {
    Actor actor = actorProvider.get(arguments, modifiers);

    if (!(actor instanceof LocalPlayer)) {
      throw new IllegalCommandSenderException(actor);
    }
    return (LocalPlayer) actor;
  }
}

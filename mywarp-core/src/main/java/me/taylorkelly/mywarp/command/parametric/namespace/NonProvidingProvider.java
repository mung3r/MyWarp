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

import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.parametric.Provider;

import java.util.Collections;
import java.util.List;

/**
 * An abstract implementation of a provider that provides its values 'magically' without parsing any user input.
 *
 * @see Provider#isProvided()
 */
abstract class NonProvidingProvider<T> implements Provider<T> {

  @Override
  public boolean isProvided() {
    return true;
  }

  @Override
  public List<String> getSuggestions(String prefix, Namespace locals) {
    return Collections.emptyList();
  }
}

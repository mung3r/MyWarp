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

package me.taylorkelly.mywarp.bukkit.util.parametric.binding;

import com.sk89q.intake.parametric.ParameterException;
import com.sk89q.intake.parametric.argument.ArgumentStack;
import com.sk89q.intake.parametric.binding.BindingBehavior;
import com.sk89q.intake.parametric.binding.BindingHelper;
import com.sk89q.intake.parametric.binding.BindingMatch;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * A binding for {@link java.io.File}s.
 */
public class FileBinding extends BindingHelper {

  private final File base;

  /**
   * Creates an instance. Files will be resolved relatively to the given base File.
   *
   * @param base the base
   */
  public FileBinding(File base) {
    this.base = base;
  }

  /**
   * Gets a readable File matching the name given by the command.
   *
   * @param context the command's context
   * @return a matching Profile
   * @throws FileNotFoundException if the File does not exist or is not readable
   * @throws ParameterException    on a parameter error
   */
  @BindingMatch(type = File.class, behavior = BindingBehavior.CONSUMES, consumedCount = 1)
  public File getFile(ArgumentStack context) throws FileNotFoundException, ParameterException {
    File ret = new File(base, context.next());
    if (!ret.exists() || !ret.canRead()) {
      throw new FileNotFoundException(ret.getAbsolutePath());
    }
    return ret;
  }

}

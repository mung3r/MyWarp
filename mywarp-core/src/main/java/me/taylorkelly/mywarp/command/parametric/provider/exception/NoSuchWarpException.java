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

package me.taylorkelly.mywarp.command.parametric.provider.exception;

import com.google.common.collect.ImmutableList;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;

/**
 * Thrown when a given input does not match an existing {@link Warp}.
 */
public class NoSuchWarpException extends NonMatchingInputException {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final ImmutableList<Warp> matches;

  /**
   * Creates an instance.
   *
   * @param input   the input
   * @param matches the possible matches of the input
   */
  public NoSuchWarpException(String input, ImmutableList<Warp> matches) {
    super(input);
    this.matches = matches;
  }

  @Override
  public String getLocalizedMessage() {
    StrBuilder builder = new StrBuilder();
    builder.append(msg.getString("exception.no-such-warp", getInput()));

    if (!matches.isEmpty()) {
      builder.appendNewLine();
      builder.append(msg.getString("exception.no-such-warp.suggestion", matches.get(1).getName()));
    }
    return builder.toString();
  }
}

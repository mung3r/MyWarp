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

package me.taylorkelly.mywarp.command.provider.exception;

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.util.MatchList;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;

/**
 * Thrown when a given input does not match an existing {@link Warp}.
 */
public class NoSuchWarpException extends NonMatchingInputException {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final MatchList matches;

  /**
   * Creates an instance.
   *
   * @param input   the input
   * @param matches the possible matches of the input
   */
  public NoSuchWarpException(String input, MatchList matches) {
    super(input);
    this.matches = matches;
  }

  /**
   * Gets the matches.
   *
   * @return the matches
   */
  public MatchList getMatches() {
    return matches;
  }

  @Override
  public String getLocalizedMessage() {
    Optional<Warp> match = matches.getMatch(new Warp.PopularityComparator());

    StrBuilder builder = new StrBuilder();
    builder.append(msg.getString("exception.no-such-warp", getInput()));

    if (match.isPresent()) {
      builder.appendNewLine();
      builder.append(msg.getString("exception.no-such-warp.suggestion", match.get().getName()));
    }
    return builder.toString();
  }
}

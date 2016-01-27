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

package me.taylorkelly.mywarp.bukkit.util.parametric;

import com.google.common.base.Optional;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.parametric.handler.ExceptionConverterHelper;
import com.sk89q.intake.parametric.handler.ExceptionMatch;

import me.taylorkelly.mywarp.bukkit.util.parametric.binding.PlayerBinding.IllegalCommandSenderException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.PlayerBinding.NoSuchPlayerException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.ProfileBinding.NoSuchProfileException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.NoSuchWarpException;
import me.taylorkelly.mywarp.util.CommandUtils;
import me.taylorkelly.mywarp.util.NoSuchWorldException;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

/**
 * Converts specific Exceptions into human readable {@link CommandException}s.
 */
public class ExceptionConverter extends ExceptionConverterHelper {

  private static final DynamicMessages MESSAGES = new DynamicMessages(CommandUtils.RESOURCE_BUNDLE_NAME);

  /**
   * Converts a {@link NoSuchPlayerException} to a human readable {@link CommandException}.
   *
   * @param ex the NoSuchPlayerException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(NoSuchPlayerException ex) throws CommandException {
    throw new CommandException(MESSAGES.getString("exception.no-such-player", ex.getQuery()), ex);
  }

  /**
   * Converts a {@link NoSuchProfileException} to a human readable {@link CommandException}.
   *
   * @param ex the NoSuchProfileException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(NoSuchProfileException ex) throws CommandException {
    throw new CommandException(MESSAGES.getString("exception.no-such-profile", ex.getQuery()), ex);
  }

  /**
   * Converts a {@link NoSuchWarpException} to a human readable {@link CommandException}.
   *
   * @param ex the NoSuchWarpException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(NoSuchWarpException ex) throws CommandException {
    Optional<Warp> match = ex.getMatches().getMatch(new Warp.PopularityComparator());

    StrBuilder builder = new StrBuilder();
    builder.append(MESSAGES.getString("exception.no-such-warp", ex.getQuery()));

    if (match.isPresent()) {
      builder.appendNewLine();
      builder.append(ChatColor.GRAY);
      builder.append(MESSAGES.getString("exception.no-such-warp.suggestion", match.get().getName()));
    }

    throw new CommandException(builder.toString(), ex);
  }

  /**
   * Converts a {@link ExceedsInitiatorLimitException} to a human readable {@link CommandException}.
   *
   * @param ex the ExceedsInitiatorLimitException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(ExceedsInitiatorLimitException ex) throws CommandException {
    StrBuilder builder = new StrBuilder();

    switch (ex.getExceededLimit()) {
      case TOTAL:
        builder.append(MESSAGES.getString("exception.exceeds-initiator-limit.total", ex.getLimitMaximum()));
        break;
      case PRIVATE:
        builder.append(MESSAGES.getString("exception.exceeds-initiator-limit.private", ex.getLimitMaximum()));
        break;
      case PUBLIC:
        builder.append(MESSAGES.getString("exception.exceeds-initiator-limit.public", ex.getLimitMaximum()));
        break;
    }
    builder.appendNewLine();
    builder.append(MESSAGES.getString("exception.exceeds-initiator-limit.delete-warps"));

    throw new CommandException(builder.toString(), ex);
  }

  /**
   * Converts a {@link ExceedsLimitException} to a human readable {@link CommandException}.
   *
   * @param ex the LimitExceededException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(ExceedsLimitException ex) throws CommandException {
    Profile subject = ex.getSubject();
    throw new CommandException(
        MESSAGES.getString("exception.exceeds-limit", subject.getName().or(subject.getUniqueId().toString())), ex);
  }

  /**
   * Converts a {@link TimerRunningException} to a human readable {@link CommandException}.
   *
   * @param ex the TimerRunningException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(TimerRunningException ex) throws CommandException {
    throw new CommandException(
        MESSAGES.getString("exception.timer-running", ex.getDurationLeft().get(TimeUnit.SECONDS)), ex);
  }

  /**
   * Converts a {@link IllegalCommandSenderException} to a human readable {@link CommandException}.
   *
   * @param ex the IllegalCommandSenderException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(IllegalCommandSenderException ex) throws CommandException {
    throw new CommandException(MESSAGES.getString("exception.illegal-command-sender"), ex);
  }

  /**
   * Converts a {@link NoSuchWorldException} to a human readable {@link CommandException}.
   *
   * @param ex the NoSuchWorldException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(NoSuchWorldException ex) throws CommandException {
    throw new CommandException(MESSAGES.getString("exception.no-such-world", ex.getWorldRepresentation()), ex);
  }

  /**
   * Converts a {@link FileNotFoundException} to a human readable {@link CommandException}.
   *
   * @param ex the FileNotFoundException
   * @throws CommandException the wrapped exception
   */
  @ExceptionMatch
  public void convert(FileNotFoundException ex) throws CommandException {
    throw new CommandException(MESSAGES.getString("exception.file-not-found", ex.getMessage()), ex);
  }
}

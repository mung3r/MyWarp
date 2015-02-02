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

package me.taylorkelly.mywarp.bukkit.commands.printer;

import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Prints information about a certain Warp.
 */
public class InfoPrinter {

  private static final DynamicMessages
      MESSAGES =
      new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  private final Warp warp;

  /**
   * Creates an instance.
   *
   * @param warp the Warp whose information should be printed
   */
  public InfoPrinter(Warp warp) {
    this.warp = warp;
  }

  /**
   * Prints the information to the given receiver.
   *
   * @param receiver the Actor who is receiving this print
   */
  public void print(Actor receiver) {
    StrBuilder info = new StrBuilder();

    // heading
    info.append(ChatColor.GOLD);
    info.append(MESSAGES.getString("info.heading",
                                   ChatColor.getByChar(warp.getType().getColorCharacter()) + warp
                                       .getName() + ChatColor.GOLD));
    info.appendNewLine();

    // creator
    info.append(ChatColor.GRAY);

    String creatorName;
    Optional<String> nameOptional = warp.getCreator().getName();
    if (nameOptional.isPresent()) {
      creatorName = nameOptional.get();
    } else {
      creatorName = warp.getCreator().getUniqueId().toString();
    }
    info.append(MESSAGES.getString("info.created-by"));
    info.append(" ");
    info.append(ChatColor.WHITE);
    info.append(creatorName);
    if (receiver instanceof LocalPlayer && warp.isCreator((LocalPlayer) receiver)) {
      info.append(" ");
      info.append(MESSAGES.getString("info.created-by-you"));
    }
    info.appendNewLine();

    // location
    info.append(ChatColor.GRAY);
    info.append(MESSAGES.getString("info.location"));
    info.append(" ");
    info.append(ChatColor.WHITE);
    info.append(MESSAGES.getString("info.location.position", warp.getPosition().getFloorX(), warp
        .getPosition().getFloorY(), warp.getPosition().getFloorZ(), warp.getWorld().getName()));

    info.appendNewLine();

    // if the warp is modifiable, show information about invitations
    if (warp.isModifiable(receiver)) {

      // invited players
      info.append(ChatColor.GRAY);
      info.append(MESSAGES.getString("info.invited-players"));
      info.append(" ");
      info.append(ChatColor.WHITE);

      Set<Profile> invitedPlayers = warp.getInvitedPlayers();
      if (invitedPlayers.isEmpty()) {
        info.append("-");
      } else {
        List<String> invitedPlayerNames = new ArrayList<String>();
        for (Profile profile : invitedPlayers) {
          Optional<String> name = profile.getName();
          if (name.isPresent()) {
            invitedPlayerNames.add(name.get());
          }
        }
        Collections.sort(invitedPlayerNames);
        info.appendWithSeparators(invitedPlayerNames, ", ");
      }
      info.appendNewLine();

      // invited groups
      info.append(ChatColor.GRAY);
      info.append(MESSAGES.getString("info.invited-groups"));
      info.append(" ");
      info.append(ChatColor.WHITE);

      List<String> invitedGroups = Ordering.natural().sortedCopy(warp.getInvitedGroups());
      if (invitedGroups.isEmpty()) {
        info.append("-");
      } else {
        info.appendWithSeparators(invitedGroups, ", ");
      }
      info.appendNewLine();
    }

    // creation date
    info.append(ChatColor.GRAY);
    info.append(MESSAGES.getString("info.creation-date", warp.getCreationDate()));
    info.append(" ");
    info.append(ChatColor.WHITE);
    info.append(DateFormat.getDateInstance(DateFormat.DEFAULT, LocaleManager.getLocale()).format(
        warp.getCreationDate()));

    info.appendNewLine();

    // visits
    info.append(ChatColor.GRAY);
    info.append(MESSAGES.getString("info.visits"));
    info.append(" ");
    info.append(ChatColor.WHITE);
    info.append(
        MESSAGES.getString("info.visits.per-day", warp.getVisits(), warp.getVisitsPerDay()));
    receiver.sendMessage(info.toString());
  }

}

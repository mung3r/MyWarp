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

package me.taylorkelly.mywarp.command.printer;

import com.google.common.collect.Ordering;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.profile.Profile;
import me.taylorkelly.mywarp.util.Message;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.i18n.LocaleManager;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.authorization.AuthorizationResolver;

import java.text.DateFormat;
import java.util.List;
import java.util.Set;

/**
 * Prints information about a certain Warp.
 */
public class InfoPrinter {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final Warp warp;
  private final AuthorizationResolver authorizationResolver;
  private Game game;

  /**
   * Creates an instance.
   *
   * @param warp                  the Warp whose information should be printed
   * @param authorizationResolver the AuthorizationResolver used to resolve authorizations for the given warp
   * @param game                  the running game instance that holds the warp's world
   */
  public InfoPrinter(Warp warp, AuthorizationResolver authorizationResolver, Game game) {
    this.warp = warp;
    this.authorizationResolver = authorizationResolver;
    this.game = game;
  }

  /**
   * Gets information text.
   *
   * @param receiver the Actor who will receive the text
   * @return the text
   */
  public Message getText(Actor receiver) {
    Message.Builder info = Message.builder();
    // heading
    info.append(Message.Style.HEADLINE_1);

    info.append(msg.getString("info.heading"));
    info.append(" '");
    info.append(warp);
    info.append("':");
    info.appendNewLine();

    // creator
    info.append(Message.Style.KEY);

    info.append(msg.getString("info.created-by"));
    info.append(" ");
    info.append(Message.Style.VALUE);
    Profile creator = warp.getCreator();
    info.append(creator);
    if (receiver instanceof LocalPlayer && warp.isCreator((LocalPlayer) receiver)) {
      info.append(" ");
      info.append(msg.getString("info.created-by-you"));
    }
    info.appendNewLine();

    // location
    info.append(Message.Style.KEY);
    info.append(msg.getString("info.location"));
    info.append(" ");
    info.append(Message.Style.VALUE);
    info.append(msg.getString("info.location.position", warp.getPosition().getFloorX(), warp.getPosition().getFloorY(),
                              warp.getPosition().getFloorZ(), warp.getWorld(game).getName()));

    info.appendNewLine();

    // if the warp is modifiable, show information about invitations
    if (authorizationResolver.isModifiable(warp, receiver)) {

      // invited players
      info.append(Message.Style.KEY);
      info.append(msg.getString("info.invited-players"));
      info.append(" ");
      info.append(Message.Style.VALUE);

      Set<Profile> invitedPlayers = warp.getInvitedPlayers();
      if (invitedPlayers.isEmpty()) {
        info.append("-");
      } else {
        info.appendWithSeparators(invitedPlayers);
      }
      info.appendNewLine();

      // invited groups
      info.append(Message.Style.KEY);
      info.append(msg.getString("info.invited-groups"));
      info.append(" ");
      info.append(Message.Style.VALUE);

      List<String> invitedGroups = Ordering.natural().sortedCopy(warp.getInvitedGroups());
      if (invitedGroups.isEmpty()) {
        info.append("-");
      } else {
        info.appendWithSeparators(invitedGroups);
      }
      info.appendNewLine();
    }

    // creation date
    info.append(Message.Style.KEY);
    info.append(msg.getString("info.creation-date", warp.getCreationDate()));
    info.append(" ");
    info.append(Message.Style.VALUE);
    info.append(
        DateFormat.getDateInstance(DateFormat.DEFAULT, LocaleManager.getLocale()).format(warp.getCreationDate()));

    info.appendNewLine();

    // visits
    info.append(Message.Style.KEY);
    info.append(msg.getString("info.visits"));
    info.append(" ");
    info.append(Message.Style.VALUE);
    info.append(msg.getString("info.visits.per-day", warp.getVisits(), WarpUtils.visitsPerDay(warp)));
    return info.build();
  }

  /**
   * Prints the information to the given receiver.
   *
   * @param receiver the Actor who is receiving this print
   */
  public void print(Actor receiver) {
    receiver.sendMessage(getText(receiver));
  }

}

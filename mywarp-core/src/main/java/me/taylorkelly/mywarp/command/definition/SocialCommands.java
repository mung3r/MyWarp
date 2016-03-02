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

package me.taylorkelly.mywarp.command.definition;

import static me.taylorkelly.mywarp.command.annotation.Name.Condition.MODIFIABLE;

import com.google.common.base.Optional;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Switch;
import com.sk89q.intake.util.auth.AuthorizationException;

import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.ExceedsInitiatorLimitException;
import me.taylorkelly.mywarp.command.ExceedsLimitException;
import me.taylorkelly.mywarp.command.annotation.Billable;
import me.taylorkelly.mywarp.command.annotation.Name;
import me.taylorkelly.mywarp.command.provider.exception.NoSuchPlayerException;
import me.taylorkelly.mywarp.command.provider.exception.NoSuchProfileException;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.profile.Profile;
import me.taylorkelly.mywarp.platform.profile.ProfileCache;
import me.taylorkelly.mywarp.service.economy.FeeType;
import me.taylorkelly.mywarp.service.limit.LimitService;
import me.taylorkelly.mywarp.util.Message;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

/**
 * Bundles commands that involve social interaction with other players.
 */
public class SocialCommands {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final Game game;
  private final ProfileCache profileCache;
  private final Optional<LimitService> limitService;

  /**
   * Creates an instance.
   *
   * @param game         the Game instance used by commands
   * @param profileCache the ProfileCache used by commands
   * @param limitService the LimitService used by commands
   */
  public SocialCommands(Game game, ProfileCache profileCache, Optional<LimitService> limitService) {
    this.game = game;
    this.profileCache = profileCache;
    this.limitService = limitService;
  }

  @Command(aliases = {"give"}, desc = "give.description", help = "give.help")
  @Require("mywarp.cmd.give")
  @Billable(FeeType.GIVE)
  public void give(Actor actor, @Switch('d') boolean giveDirectly, @Switch('f') boolean ignoreLimits, Profile receiver,
                   @Name(MODIFIABLE) Warp warp) throws CommandException, AuthorizationException, NoSuchPlayerException {
    if (warp.isCreator(receiver)) {
      throw new CommandException("give.is-owner");
    }
    Optional<LocalPlayer> receiverPlayer = game.getPlayer(receiver.getUniqueId());

    if (!ignoreLimits && limitService.isPresent()) {
      if (!receiverPlayer.isPresent()) {
        throw new NoSuchPlayerException(receiver);
      }
      LimitService.EvaluationResult
          result =
          limitService.get().evaluateLimit(receiverPlayer.get(), warp.getWorld(game), warp.getType().getLimit(), true);
      if (result.exceedsLimit()) {
        throw new ExceedsLimitException(receiver);
      }
    } else if (!actor.hasPermission("mywarp.cmd.give.force")) {
      throw new AuthorizationException();
    }

    if (giveDirectly) {
      if (!actor.hasPermission("mywarp.cmd.give.direct")) {
        throw new AuthorizationException();
      }

      warp.setCreator(receiver);

      actor.sendMessage(msg.getString("give.given-successful", warp.getName(),
                                      receiver.getName().or(receiver.getUniqueId().toString())));

      if (receiverPlayer.isPresent()) {
        receiverPlayer.get().sendMessage(msg.getString("give.givee-owner", actor.getName(), warp.getName()));
      }
      return;
    }

    if (!receiverPlayer.isPresent()) {
      throw new NoSuchPlayerException(receiver);
    }
    receiverPlayer.get().initiateAcceptanceConversation(actor, warp);
    actor.sendMessage(msg.getString("give.asked-successful", receiverPlayer.get().getName(), warp.getName()));

  }

  @Command(aliases = {"private"}, desc = "private.description", help = "private.help")
  @Require("mywarp.cmd.private")
  @Billable(FeeType.PRIVATE)
  public void privatize(Actor actor, @Switch('f') boolean ignoreLimits, @Name(MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchPlayerException {
    if (warp.isType(Warp.Type.PRIVATE)) {
      throw new CommandException(msg.getString("private.already-private", warp.getName()));
    }
    if (!ignoreLimits && limitService.isPresent()) {
      Profile creator = warp.getCreator();
      Optional<LocalPlayer> creatorPlayer = game.getPlayer(creator.getUniqueId());
      if (!creatorPlayer.isPresent()) {
        throw new NoSuchPlayerException(creator);
      }

      LimitService.EvaluationResult
          result =
          limitService.get()
              .evaluateLimit(creatorPlayer.get(), warp.getWorld(game), Warp.Type.PRIVATE.getLimit(), false);

      if (result.exceedsLimit()) {
        if (actor instanceof LocalPlayer && creatorPlayer.get().equals(actor)) {
          throw new ExceedsInitiatorLimitException(result.getExceededLimit(), result.getLimitMaximum());
        } else {
          throw new ExceedsLimitException(creator);
        }
      }

    } else if (!actor.hasPermission("mywarp.cmd.private.force")) {
      throw new AuthorizationException();
    }

    warp.setType(Warp.Type.PRIVATE);
    actor.sendMessage(msg.getString("private.privatized", warp.getName()));
  }


  @Command(aliases = {"public"}, desc = "public.description", help = "public.help")
  @Require("mywarp.cmd.public")
  @Billable(FeeType.PUBLIC)
  public void publicize(Actor actor, @Switch('f') boolean ignoreLimits, @Name(MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchPlayerException {
    if (warp.isType(Warp.Type.PUBLIC)) {
      throw new CommandException(msg.getString("public.already-public", warp.getName()));
    }
    if (!ignoreLimits && limitService.isPresent()) {
      Profile creator = warp.getCreator();
      Optional<LocalPlayer> creatorPlayer = game.getPlayer(creator.getUniqueId());
      if (!creatorPlayer.isPresent()) {
        throw new NoSuchPlayerException(creator);
      }

      LimitService.EvaluationResult
          result =
          limitService.get()
              .evaluateLimit(creatorPlayer.get(), warp.getWorld(game), Warp.Type.PUBLIC.getLimit(), false);

      if (result.exceedsLimit()) {
        if (actor instanceof LocalPlayer && creatorPlayer.get().equals(actor)) {
          throw new ExceedsInitiatorLimitException(result.getExceededLimit(), result.getLimitMaximum());
        } else {
          throw new ExceedsLimitException(creator);
        }
      }

    } else if (!actor.hasPermission("mywarp.cmd.public.force")) {
      throw new AuthorizationException();
    }
    warp.setType(Warp.Type.PUBLIC);
    actor.sendMessage(msg.getString("public.publicized", warp.getName()));
  }

  @Command(aliases = {"invite"}, desc = "invite.description", help = "invite.help")
  @Require("mywarp.cmd.invite")
  @Billable(FeeType.INVITE)
  public void invite(Actor actor, @Switch('g') boolean groupInvite, String inviteeIdentifier,
                     @Name(MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchProfileException {
    if (groupInvite) {
      if (!actor.hasPermission("mywarp.cmd.invite.group")) {
        throw new AuthorizationException();
      }

      if (warp.isGroupInvited(inviteeIdentifier)) {
        throw new CommandException(msg.getString("invite.group.already-invited", inviteeIdentifier));
      }
      warp.inviteGroup(inviteeIdentifier);

      actor.sendMessage(msg.getString("invite.group.successful", inviteeIdentifier, warp.getName()));
      if (warp.getType() == Warp.Type.PUBLIC) {
        actor.sendMessage(
            Message.builder().append(Message.Style.INFO).append(msg.getString("invite.public", warp.getName()))
                .build());
      }
      return;
    }
    // invite player
    Optional<Profile> optionalInvitee = profileCache.getByName(inviteeIdentifier);
    if (!optionalInvitee.isPresent()) {
      throw new NoSuchProfileException(inviteeIdentifier);
    }

    Profile invitee = optionalInvitee.get();

    if (warp.isPlayerInvited(invitee)) {
      throw new CommandException(msg.getString("invite.player.already-invited", invitee.getName()));
    }
    if (warp.isCreator(invitee)) {
      throw new CommandException(msg.getString("invite.player.is-creator", invitee.getName()));
    }
    warp.invitePlayer(invitee);

    String displayName = invitee.getUniqueId().toString();
    if (invitee.getName().isPresent()) {
      displayName = invitee.getName().get();
    }

    actor.sendMessage(msg.getString("invite.player.successful", displayName, warp.getName()));
    if (warp.getType() == Warp.Type.PUBLIC) {
      actor.sendMessage(
          Message.builder().append(Message.Style.INFO).append(msg.getString("invite.public", warp.getName())).build());
    }

    Optional<LocalPlayer> invitedPlayer = game.getPlayer(invitee.getUniqueId());
    if (invitedPlayer.isPresent()) {
      invitedPlayer.get().sendMessage(msg.getString("invite.player.player-invited", actor.getName(), warp.getName()));
    }
  }

  @Command(aliases = {"uninvite"}, desc = "uninvite.description", help = "uninvite.help")
  @Require("mywarp.cmd.uninvite")
  @Billable(FeeType.UNINVITE)
  public void uninvite(Actor actor, @Switch('g') boolean groupInvite, String uninviteeIdentifier,
                       @Name(MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchProfileException {
    if (groupInvite) {
      if (!actor.hasPermission("mywarp.cmd.uninvite.group")) {
        throw new AuthorizationException();
      }

      if (!warp.isGroupInvited(uninviteeIdentifier)) {
        throw new CommandException(msg.getString("uninvite.group.not-invited", uninviteeIdentifier));
      }
      warp.uninviteGroup(uninviteeIdentifier);

      actor.sendMessage(msg.getString("uninvite.group.successful", uninviteeIdentifier, warp.getName()));
      if (warp.getType() == Warp.Type.PUBLIC) {
        actor.sendMessage(
            Message.builder().append(Message.Style.INFO).append(msg.getString("uninvite.public", warp.getName()))
                .build());
      }
      return;
    }
    // uninvite player
    Optional<Profile> optionalUninvitee = profileCache.getByName(uninviteeIdentifier);
    if (!optionalUninvitee.isPresent()) {
      throw new NoSuchProfileException(uninviteeIdentifier);
    }

    Profile uninvitee = optionalUninvitee.get();

    if (!warp.isPlayerInvited(uninvitee)) {
      throw new CommandException(msg.getString("uninvite.player.not-invited", uninvitee.getName()));
    }
    if (warp.isCreator(uninvitee)) {
      throw new CommandException(msg.getString("uninvite.player.is-creator", uninvitee.getName()));
    }
    warp.uninvitePlayer(uninvitee);

    String displayName = uninvitee.getUniqueId().toString();
    if (uninvitee.getName().isPresent()) {
      displayName = uninvitee.getName().get();
    }

    actor.sendMessage(msg.getString("uninvite.player.successful", displayName, warp.getName()));
    if (warp.getType() == Warp.Type.PUBLIC) {
      actor.sendMessage(
          Message.builder().append(Message.Style.INFO).append(msg.getString("uninvite.public", warp.getName()))
              .build());
    }

    Optional<LocalPlayer> invitedPlayer = game.getPlayer(uninvitee.getUniqueId());
    if (invitedPlayer.isPresent()) {
      invitedPlayer.get().sendMessage(msg.getString("uninvite.player.player-uninvited", warp.getName()));
    }
  }
}

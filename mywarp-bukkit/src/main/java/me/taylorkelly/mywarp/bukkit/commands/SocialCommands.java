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

package me.taylorkelly.mywarp.bukkit.commands;

import com.google.common.base.Optional;
import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Switch;
import com.sk89q.intake.util.auth.AuthorizationException;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.Game;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.bukkit.conversation.WarpAcceptancePromptFactory;
import me.taylorkelly.mywarp.bukkit.util.parametric.ExceedsInitiatorLimitException;
import me.taylorkelly.mywarp.bukkit.util.parametric.ExceedsLimitException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.PlayerBinding.NoSuchPlayerException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.ProfileBinding.NoSuchProfileException;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.Name;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.Name.Condition;
import me.taylorkelly.mywarp.bukkit.util.parametric.economy.Billable;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.util.CommandUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

/**
 * Bundles commands that involve social interaction with other players.
 */
public class SocialCommands {

  private static final DynamicMessages MESSAGES = new DynamicMessages(CommandUtils.RESOURCE_BUNDLE_NAME);

  private final Game game;
  private final LimitManager manager;
  private final ProfileService service;
  private final WarpAcceptancePromptFactory warpAcceptancePromptFactory;

  /**
   * Creates an instance.
   *
   * @param warpAcceptancePromptFactory the WarpAcceptancePromptFactory
   */
  public SocialCommands(Game game, LimitManager manager, ProfileService service,
                        WarpAcceptancePromptFactory warpAcceptancePromptFactory) {
    this.game = game;
    this.manager = manager;
    this.service = service;
    this.warpAcceptancePromptFactory = warpAcceptancePromptFactory;
  }

  /**
   * Changes the owner of a Warp.
   *
   * @param actor        the Actor
   * @param giveDirectly whether the Warp should be given directly, without asking the owner for acceptance
   * @param ignoreLimits whether the limits of the new owner should be ignored
   * @param receiver     the Profile of the player who should receive the Warp
   * @param warp         the Warp
   * @throws CommandException       if the warp is owned by the receiver
   * @throws AuthorizationException if the Actor does have enough permissions
   * @throws NoSuchPlayerException  if the receiver is offline and the flags to bypass this check where not given
   * @throws ExceedsLimitException  if the limits of the receiver would be exceeded
   */
  @Command(aliases = {"give"}, desc = "give.description", help = "give.help")
  @Require("mywarp.cmd.give")
  @Billable(FeeType.GIVE)
  public void give(Actor actor, @Switch('d') boolean giveDirectly, @Switch('f') boolean ignoreLimits, Profile receiver,
                   @Name(Condition.MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchPlayerException, ExceedsLimitException {
    if (warp.isCreator(receiver)) {
      throw new CommandException("give.is-owner");
    }
    Optional<LocalPlayer> receiverPlayer = game.getPlayer(receiver.getUniqueId());

    if (!ignoreLimits) {
      if (!receiverPlayer.isPresent()) {
        throw new NoSuchPlayerException(receiver);
      }
      LimitManager.EvaluationResult
          result =
          manager.evaluateLimit(receiverPlayer.get(), warp.getWorld(), warp.getType().getLimit(), true);
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

      actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("give.given-successful", warp.getName(),
                                                            receiver.getName().or(receiver.getUniqueId().toString())));

      if (receiverPlayer.isPresent()) {
        receiverPlayer.get()
            .sendMessage(ChatColor.AQUA + MESSAGES.getString("give.givee-owner", actor.getName(), warp.getName()));
      }
      return;
    }

    if (!receiverPlayer.isPresent()) {
      throw new NoSuchPlayerException(receiver);
    }
    warpAcceptancePromptFactory.create(actor, receiverPlayer.get(), warp);
    actor.sendMessage(
        ChatColor.AQUA + MESSAGES.getString("give.asked-successful", receiverPlayer.get().getName(), warp.getName()));

  }

  /**
   * Privatizes a Warp.
   *
   * @param actor        the Actor
   * @param ignoreLimits whether the limits of the owner should be ignored
   * @param warp         the warp
   * @throws CommandException               if the Warp is already private
   * @throws AuthorizationException         if {@code ignoreLimits} is {@code true}, but the given Actor does not have
   *                                        the required permission
   * @throws NoSuchPlayerException          if the warp's creator is offline and {@code ignoreLimits} is {@code false}
   * @throws ExceedsInitiatorLimitException if the limit of the Actor would be exceeded by privatizing the warp and
   *                                        {@code ignoreLimits} is {@code false}
   * @throws ExceedsLimitException          if a Limit would be exceeded by privatizing the warp and {@code
   *                                        ignoreLimits} is {@code false}
   */
  @Command(aliases = {"private"}, desc = "private.description", help = "private.help")
  @Require("mywarp.cmd.private")
  @Billable(FeeType.PRIVATE)
  public void privatize(Actor actor, @Switch('f') boolean ignoreLimits, @Name(Condition.MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchPlayerException, ExceedsInitiatorLimitException,
             ExceedsLimitException {
    if (warp.isType(Warp.Type.PRIVATE)) {
      throw new CommandException(MESSAGES.getString("private.already-private", warp.getName()));
    }
    if (!ignoreLimits) {
      Profile creator = warp.getCreator();
      Optional<LocalPlayer> creatorPlayer = game.getPlayer(creator.getUniqueId());
      if (!creatorPlayer.isPresent()) {
        throw new NoSuchPlayerException(creator);
      }

      LimitManager.EvaluationResult
          result =
          manager.evaluateLimit(creatorPlayer.get(), warp.getWorld(), Warp.Type.PRIVATE.getLimit(), false);

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
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("private.privatized", warp.getName()));
  }

  /**
   * Publicizes a Warp.
   *
   * @param actor the Actor
   * @param warp  the warp
   * @throws CommandException               if the Warp is already public
   * @throws AuthorizationException         if {@code ignoreLimits} is {@code true}, but the given Actor does not have
   *                                        the required permission
   * @throws NoSuchPlayerException          if the warp's creator is offline and {@code ignoreLimits} is {@code false}
   * @throws ExceedsInitiatorLimitException if the limit of the Actor would be exceeded by publicizing the warp and
   *                                        {@code ignoreLimits} is {@code false}
   * @throws ExceedsLimitException          if a Limit would be exceeded by publicizing the warp and {@code
   *                                        ignoreLimits} is {@code false}
   */
  @Command(aliases = {"public"}, desc = "public.description", help = "public.help")
  @Require("mywarp.cmd.public")
  @Billable(FeeType.PUBLIC)
  public void publicize(Actor actor, @Switch('f') boolean ignoreLimits, @Name(Condition.MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchPlayerException, ExceedsInitiatorLimitException,
             ExceedsLimitException {
    if (warp.isType(Warp.Type.PUBLIC)) {
      throw new CommandException(MESSAGES.getString("public.already-public", warp.getName()));
    }
    if (!ignoreLimits) {
      Profile creator = warp.getCreator();
      Optional<LocalPlayer> creatorPlayer = game.getPlayer(creator.getUniqueId());
      if (!creatorPlayer.isPresent()) {
        throw new NoSuchPlayerException(creator);
      }

      LimitManager.EvaluationResult
          result =
          manager.evaluateLimit(creatorPlayer.get(), warp.getWorld(), Warp.Type.PUBLIC.getLimit(), false);

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
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("public.publicized", warp.getName()));
  }

  /**
   * Invites players or groups to a Warp.
   *
   * @param actor             the Actor
   * @param groupInvite       whether groups should be invited
   * @param inviteeIdentifier the identifier of the invitee
   * @param warp              the Warp
   * @throws CommandException       if the invitation fails
   * @throws AuthorizationException if the Actor does not have the required permissions
   * @throws NoSuchProfileException if a player should be invited, but no Profile could be found for the given {@code
   *                                inviteeIdentifier}
   */
  @Command(aliases = {"invite"}, desc = "invite.description", help = "invite.help")
  @Require("mywarp.cmd.invite")
  @Billable(FeeType.INVITE)
  public void invite(Actor actor, @Switch('g') boolean groupInvite, String inviteeIdentifier,
                     @Name(Condition.MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchProfileException {
    if (groupInvite) {
      if (!actor.hasPermission("mywarp.cmd.invite.group")) {
        throw new AuthorizationException();
      }

      if (warp.isGroupInvited(inviteeIdentifier)) {
        throw new CommandException(MESSAGES.getString("invite.group.already-invited", inviteeIdentifier));
      }
      warp.inviteGroup(inviteeIdentifier);

      actor.sendMessage(
          ChatColor.AQUA + MESSAGES.getString("invite.group.successful", inviteeIdentifier, warp.getName()));
      if (warp.getType() == Warp.Type.PUBLIC) {
        actor.sendMessage(ChatColor.GRAY + MESSAGES.getString("invite.public", warp.getName()));
      }
      return;
    }
    // invite player
    Optional<Profile> optionalInvitee = service.getByName(inviteeIdentifier);
    if (!optionalInvitee.isPresent()) {
      throw new NoSuchProfileException(inviteeIdentifier);
    }

    Profile invitee = optionalInvitee.get();

    if (warp.isPlayerInvited(invitee)) {
      throw new CommandException(MESSAGES.getString("invite.player.already-invited", invitee.getName()));
    }
    if (warp.isCreator(invitee)) {
      throw new CommandException(MESSAGES.getString("invite.player.is-creator", invitee.getName()));
    }
    warp.invitePlayer(invitee);

    String displayName = invitee.getUniqueId().toString();
    if (invitee.getName().isPresent()) {
      displayName = invitee.getName().get();
    }

    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("invite.player.successful", displayName, warp.getName()));
    if (warp.getType() == Warp.Type.PUBLIC) {
      actor.sendMessage(ChatColor.GRAY + MESSAGES.getString("invite.public", warp.getName()));
    }

    Optional<LocalPlayer> invitedPlayer = game.getPlayer(invitee.getUniqueId());
    if (invitedPlayer.isPresent()) {
      invitedPlayer.get().sendMessage(
          ChatColor.AQUA + MESSAGES.getString("invite.player.player-invited", actor.getName(), warp.getName()));
    }
  }

  /**
   * Uninvites players or groups from a Warp.
   *
   * @param actor               the Actor
   * @param groupInvite         whether groups should be uninvited
   * @param uninviteeIdentifier the identifier of the uninvitee
   * @param warp                the Warp
   * @throws CommandException       if the uninvitation fails
   * @throws AuthorizationException if the Actor does not have the required permissions
   * @throws NoSuchProfileException if a player should be uninvited, but no Profile could be found for the given {@code
   *                                uninviteeIdentifier}
   */
  @Command(aliases = {"uninvite"}, desc = "uninvite.description", help = "uninvite.help")
  @Require("mywarp.cmd.uninvite")
  @Billable(FeeType.UNINVITE)
  public void uninvite(Actor actor, @Switch('g') boolean groupInvite, String uninviteeIdentifier,
                       @Name(Condition.MODIFIABLE) Warp warp)
      throws CommandException, AuthorizationException, NoSuchProfileException {
    if (groupInvite) {
      if (!actor.hasPermission("mywarp.cmd.uninvite.group")) {
        throw new AuthorizationException();
      }

      if (!warp.isGroupInvited(uninviteeIdentifier)) {
        throw new CommandException(MESSAGES.getString("uninvite.group.not-invited", uninviteeIdentifier));
      }
      warp.uninviteGroup(uninviteeIdentifier);

      actor.sendMessage(
          ChatColor.AQUA + MESSAGES.getString("uninvite.group.successful", uninviteeIdentifier, warp.getName()));
      if (warp.getType() == Warp.Type.PUBLIC) {
        actor.sendMessage(ChatColor.GRAY + MESSAGES.getString("uninvite.public", warp.getName()));
      }
      return;
    }
    // uninvite player
    Optional<Profile> optionalUninvitee = service.getByName(uninviteeIdentifier);
    if (!optionalUninvitee.isPresent()) {
      throw new NoSuchProfileException(uninviteeIdentifier);
    }

    Profile uninvitee = optionalUninvitee.get();

    if (!warp.isPlayerInvited(uninvitee)) {
      throw new CommandException(MESSAGES.getString("uninvite.player.not-invited", uninvitee.getName()));
    }
    if (warp.isCreator(uninvitee)) {
      throw new CommandException(MESSAGES.getString("uninvite.player.is-creator", uninvitee.getName()));
    }
    warp.uninvitePlayer(uninvitee);

    String displayName = uninvitee.getUniqueId().toString();
    if (uninvitee.getName().isPresent()) {
      displayName = uninvitee.getName().get();
    }

    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("uninvite.player.successful", displayName, warp.getName()));
    if (warp.getType() == Warp.Type.PUBLIC) {
      actor.sendMessage(ChatColor.GRAY + MESSAGES.getString("uninvite.public", warp.getName()));
    }

    Optional<LocalPlayer> invitedPlayer = game.getPlayer(uninvitee.getUniqueId());
    if (invitedPlayer.isPresent()) {
      invitedPlayer.get()
          .sendMessage(ChatColor.AQUA + MESSAGES.getString("uninvite.player.player-uninvited", warp.getName()));
    }
  }
}

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
import me.taylorkelly.mywarp.bukkit.util.LimitExceededException;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding.NoSuchPlayerException;
import me.taylorkelly.mywarp.bukkit.util.ProfileBinding.NoSuchProfileException;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition.Type;
import me.taylorkelly.mywarp.bukkit.util.economy.Billable;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.util.profile.ProfileService;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

/**
 * Bundles commands that involve social interaction with other players.
 */
public class SocialCommands {

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

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
   * @param receiver     the Profile of the player who should receive the Warp
   * @param warp         the Warp
   * @param giveDirectly whether the Warp should be given directly, without asking the owner for acceptance
   * @param ignoreLimits whether the limits of the new owner should be ignored
   * @throws CommandException       if the owner could not be changed
   * @throws AuthorizationException if the Actor does have enough permissions
   * @throws NoSuchPlayerException  if the receiver is offline and the flags to bypass this check where not given
   */
  @Command(aliases = {"give"}, desc = "give.description", help = "give.help")
  @Require("mywarp.warp.soc.give")
  @Billable(FeeType.GIVE)
  public void give(Actor actor, Profile receiver, @Condition(Type.MODIFIABLE) Warp warp,
                   @Switch('d') boolean giveDirectly, @Switch('f') boolean ignoreLimits)
      throws CommandException, AuthorizationException, NoSuchPlayerException {
    if (giveDirectly && !actor.hasPermission("mywarp.warp.soc.give.direct")) { // NON-NLS
      throw new AuthorizationException();
    }
    if (ignoreLimits && !actor.hasPermission("mywarp.warp.soc.give.force")) { // NON-NLS
      throw new AuthorizationException();
    }

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
        throw new CommandException(MESSAGES.getString("give.receiver-limits"));
      }
    }

    if (giveDirectly) {
      warp.setCreator(receiver);
      actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("give.given-successful"));

      if (receiverPlayer.isPresent()) {
        receiverPlayer.get().sendMessage(ChatColor.AQUA + MESSAGES.getString("give.givee-owner"));
      }
      return;
    }

    if (!receiverPlayer.isPresent()) {
      throw new NoSuchPlayerException(receiver);
    }
    warpAcceptancePromptFactory.create(actor, receiverPlayer.get(), warp);
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("give.asked-successful"));

  }

  /**
   * Privatizes a Warp.
   *
   * @param actor the Actor
   * @param warp  the warp
   * @throws CommandException       if the Warp could not be privatized
   * @throws LimitExceededException if a Limit would be exceeded by creating the warp
   * @throws NoSuchPlayerException  if the warp's creator is offline
   */
  @Command(aliases = {"private"}, desc = "private.description", help = "private.help")
  @Require("mywarp.warp.soc.private")
  @Billable(FeeType.PRIVATE)
  public void privatize(Actor actor, @Condition(Type.MODIFIABLE) Warp warp)
      throws CommandException, LimitExceededException, NoSuchPlayerException {
    if (warp.isType(Warp.Type.PRIVATE)) {
      throw new CommandException("private.already-private");
    }
    Profile creator = warp.getCreator();
    Optional<LocalPlayer> creatorPlayer = game.getPlayer(creator.getUniqueId());
    if (!creatorPlayer.isPresent()) {
      throw new NoSuchPlayerException(creator);
    }
    LimitManager.EvaluationResult
        result =
        manager.evaluateLimit(creatorPlayer.get(), warp.getWorld(), Warp.Type.PRIVATE.getLimit(), false);
    if (result.exceedsLimit()) {
      throw new LimitExceededException(result.getExceededLimit().get(), result.getLimitMaximum().get());
    }
    warp.setType(Warp.Type.PRIVATE);
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("private.privatized", warp.getName()));
  }

  /**
   * Publicizes a Warp.
   *
   * @param actor the Actor
   * @param warp  the warp
   * @throws CommandException       if the Warp could not be publicized
   * @throws LimitExceededException if a Limit would be exceeded by creating the warp
   * @throws NoSuchPlayerException  if the warp's creator is offline
   */
  @Command(aliases = {"public"}, desc = "public.description", help = "public.help")
  @Require("mywarp.warp.soc.public")
  @Billable(FeeType.PUBLIC)
  public void publicize(Actor actor, @Condition(Type.MODIFIABLE) Warp warp)
      throws CommandException, LimitExceededException, NoSuchPlayerException {
    if (warp.isType(Warp.Type.PUBLIC)) {
      throw new CommandException(MESSAGES.getString("public.already-public"));
    }
    Profile creator = warp.getCreator();
    Optional<LocalPlayer> creatorPlayer = game.getPlayer(creator.getUniqueId());
    if (!creatorPlayer.isPresent()) {
      throw new NoSuchPlayerException(creator);
    }
    LimitManager.EvaluationResult
        result =
        manager.evaluateLimit(creatorPlayer.get(), warp.getWorld(), Warp.Type.PUBLIC.getLimit(), false);
    if (result.exceedsLimit()) {
      throw new LimitExceededException(result.getExceededLimit().get(), result.getLimitMaximum().get());
    }
    warp.setType(Warp.Type.PUBLIC);
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("public.publicized", warp.getName()));
  }

  /**
   * Invites players or groups to a Warp.
   *
   * @param actor             the Actor
   * @param inviteeIdentifier the identifier of the invitee
   * @param warp              the Warp
   * @param groupInvite       whether groups should be invited
   * @throws CommandException       if the invitation fails
   * @throws AuthorizationException if the Actor does have enough permissions
   * @throws NoSuchProfileException if a player should be invited, but no Profile could be found for the given {@code
   *                                inviteeIdentifier}
   */
  @Command(aliases = {"invite"}, desc = "invite.description", help = "invite.help")
  @Require("mywarp.warp.soc.invite")
  @Billable(FeeType.INVITE)
  public void invite(Actor actor, String inviteeIdentifier, @Condition(Condition.Type.MODIFIABLE) Warp warp,
                     @Switch('g') boolean groupInvite)
      throws CommandException, AuthorizationException, NoSuchProfileException {
    if (groupInvite) {
      if (!actor.hasPermission("mywarp.warp.soc.invite.group")) { // NON-NLS
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
    Optional<Profile> optionalInvitee = service.get(inviteeIdentifier);
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

    Optional<LocalPlayer> uninvitedPlayer = game.getPlayer(invitee.getUniqueId());
    if (uninvitedPlayer.isPresent()) {
      uninvitedPlayer.get()
          .sendMessage(ChatColor.AQUA + MESSAGES.getString("invite.player.player-invited", warp.getName()));
    }
  }

  /**
   * Uninvites players or groups from a Warp.
   *
   * @param actor               the Actor
   * @param uninviteeIdentifier the identifier of the uninvitee
   * @param warp                the Warp
   * @param groupInvite         whether groups should be uninvited
   * @throws CommandException       if the uninvitation fails
   * @throws AuthorizationException if the Actor does have enough permissions
   * @throws NoSuchProfileException if a player should be uninvited, but no Profile could be found for the given {@code
   *                                uninviteeIdentifier}
   */
  @Command(aliases = {"uninvite"}, desc = "uninvite.description", help = "uninvite.help")
  @Require("mywarp.warp.soc.uninvite")
  @Billable(FeeType.UNINVITE)
  public void uninvite(Actor actor, String uninviteeIdentifier, @Condition(Condition.Type.MODIFIABLE) Warp warp,
                       @Switch('g') boolean groupInvite)
      throws CommandException, AuthorizationException, NoSuchProfileException {
    if (groupInvite) {
      if (!actor.hasPermission("mywarp.warp.soc.uninvite.group")) { // NON-NLS
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
    Optional<Profile> optionalUninvitee = service.get(uninviteeIdentifier);
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

    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("uninvited.player.successful", displayName, warp.getName()));
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

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

import com.sk89q.intake.Command;
import com.sk89q.intake.CommandException;
import com.sk89q.intake.Require;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.parametric.ExceedsInitiatorLimitException;
import me.taylorkelly.mywarp.command.parametric.binding.PlayerBinding.Sender;
import me.taylorkelly.mywarp.command.parametric.binding.WarpBinding.Name;
import me.taylorkelly.mywarp.command.parametric.economy.Billable;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.limits.LimitService;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpBuilder;

/**
 * Bundles commands that manage Warps.
 */
public class ManagementCommands {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;

  /**
   * Creates an instance.
   *
   * @param myWarp the MyWarp instance
   */
  public ManagementCommands(MyWarp myWarp) {
    this.myWarp = myWarp;
  }

  /**
   * Creates a private Warp with the given name.
   *
   * @param player the LocalPlayer
   * @param name   the name
   * @throws CommandException               if the Warp could not be created
   * @throws ExceedsInitiatorLimitException if a Limit would be exceeded by creating the warp
   */
  @Command(aliases = {"pcreate", "pset"}, desc = "create.private.description", help = "create.private.help")
  @Require("mywarp.cmd.create-private")
  @Billable(FeeType.CREATE_PRIVATE)
  public void pcreate(@Sender LocalPlayer player, String name) throws CommandException, ExceedsInitiatorLimitException {
    addWarp(player, player.getWorld(), player.getPosition(), player.getRotation(), Warp.Type.PRIVATE, name);
    player.sendMessage(msg.getString("create.private.created-successful", name));
  }

  /**
   * Creates a public Warp with the given name.
   *
   * @param player the LocalPlayer
   * @param name   the name
   * @throws CommandException               if the Warp could not be created
   * @throws ExceedsInitiatorLimitException if a Limit would be exceeded by creating the warp
   */
  @Command(aliases = {"create", "set"}, desc = "create.public.description", help = "create.public.help")
  @Require("mywarp.cmd.create-public")
  @Billable(FeeType.CREATE)
  public void create(@Sender LocalPlayer player, String name) throws CommandException, ExceedsInitiatorLimitException {
    addWarp(player, player.getWorld(), player.getPosition(), player.getRotation(), Warp.Type.PUBLIC, name);
    player.sendMessage(msg.getString("create.public.created-successful", name));
  }

  /**
   * Creates a Warp and adds it to the used WarpManager or fails fast.
   *
   * @param creator  the LocalPlayer creating the Warp
   * @param world    the world where the warp is placed
   * @param position the position of the warp
   * @param rotation the rotation of the warp
   * @param type     the warp's Warp.Type
   * @param name     the warp's name
   * @throws CommandException               if the Warp cannot be created
   * @throws ExceedsInitiatorLimitException if a Limit would be exceeded by creating the warp
   */
  private void addWarp(LocalPlayer creator, LocalWorld world, Vector3 position, EulerDirection rotation, Warp.Type type,
                       String name) throws CommandException, ExceedsInitiatorLimitException {
    if (myWarp.getWarpManager().contains(name)) {
      throw new CommandException(msg.getString("create.warp-exists", name));
    }
    if (name.length() > WarpUtils.MAX_NAME_LENGTH) {
      throw new CommandException(msg.getString("create.name-too-long", WarpUtils.MAX_NAME_LENGTH));
    }
    if (myWarp.getCommandHandler().isSubCommand(name)) {
      throw new CommandException(msg.getString("create.name-is-cmd", name));
    }

    LimitService.EvaluationResult
        result =
        myWarp.getLimitService().evaluateLimit(creator, world, type.getLimit(), true);
    if (result.exceedsLimit()) {
      throw new ExceedsInitiatorLimitException(result.getExceededLimit(), result.getLimitMaximum());
    }

    myWarp.getWarpManager().add(
        new WarpBuilder(myWarp, name, creator.getProfile(), world.getUniqueId(), position, rotation).setType(type)
            .build());
  }

  /**
   * Deletes a Warp.
   *
   * @param actor the Actor
   * @param warp  the Warp
   */
  @Command(aliases = {"delete", "remove"}, desc = "delete.description", help = "delete.help")
  @Require("mywarp.cmd.delete")
  @Billable(FeeType.DELETE)
  public void delete(Actor actor, @Name(Name.Condition.MODIFIABLE) Warp warp) {
    myWarp.getWarpManager().remove(warp);
    actor.sendMessage(msg.getString("delete.deleted-successful", warp.getName()));
  }

  /**
   * Update the location of a Warp.
   *
   * @param player the LocalPlayer
   * @param warp   the Warp
   */
  @Command(aliases = {"update"}, desc = "update.description", help = "update.help")
  @Require("mywarp.cmd.update")
  @Billable(FeeType.UPDATE)
  public void update(@Sender LocalPlayer player, @Name(Name.Condition.MODIFIABLE) Warp warp) {
    warp.setLocation(player.getWorld(), player.getPosition(), player.getRotation());
    player.sendMessage(msg.getString("update.update-successful", warp.getName()));
  }

  /**
   * Edits the welcome-message of a Warp.
   *
   * @param player the BukkitPlayer
   * @param warp   the Warp
   */
  @Command(aliases = {"welcome"}, desc = "welcome.description", help = "welcome.help")
  @Require("mywarp.cmd.welcome")
  @Billable(FeeType.WELCOME)
  public void welcome(@Sender LocalPlayer player, @Name(Name.Condition.MODIFIABLE) Warp warp) {
    player.initiateWelcomeChangeConversation(warp);
  }

}

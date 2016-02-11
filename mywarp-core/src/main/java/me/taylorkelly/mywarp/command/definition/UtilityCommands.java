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

import static me.taylorkelly.mywarp.command.parametric.binding.WarpBinding.Name.Condition.USABLE;
import static me.taylorkelly.mywarp.command.parametric.binding.WarpBinding.Name.Condition.VIEWABLE;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.command.paginator.StringPaginator;
import me.taylorkelly.mywarp.command.parametric.binding.PlayerBinding.Sender;
import me.taylorkelly.mywarp.command.parametric.binding.WarpBinding.Name;
import me.taylorkelly.mywarp.command.parametric.economy.Billable;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.Set;

/**
 * Bundles utility commands.
 */
public class UtilityCommands {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;

  /**
   * Creates an instance.
   *
   * @param myWarp the MyWarp instance
   */
  public UtilityCommands(MyWarp myWarp) {
    this.myWarp = myWarp;
  }

  /**
   * Displays the help.
   *
   * @param actor the Actor
   * @param page  the page
   */
  @Command(aliases = {"help"}, desc = "help.description", help = "help.help")
  @Require("mywarp.cmd.help")
  @Billable(FeeType.HELP)
  public void help(Actor actor, @Optional("1") int page) {
    Set<String> usableCommands = myWarp.getCommandHandler().getUsableCommands(actor);

    StringPaginator.of(msg.getString("help.heading"), usableCommands).withNote(msg.getString("help.note")).paginate()
        .display(actor, page);
  }

  /**
   * Points the compass of a player to a Warp.
   *
   * @param player the LocalPlayer
   * @param warp   the Warp
   */
  @Command(aliases = {"point"}, desc = "point.description", help = "point.help")
  @Require("mywarp.cmd.point")
  @Billable(FeeType.POINT)
  public void point(@Sender LocalPlayer player, @Optional @Name(USABLE) Warp warp) {
    if (warp != null) {
      player.setCompassTarget(warp.getWorld(), warp.getPosition());
      player.sendMessage(msg.getString("point.set", warp.getName()));
    } else {
      player.resetCompass();
      player.sendMessage(msg.getString("point.reset"));
    }
  }

  /**
   * Teleports an other player to a Warp.
   *
   * @param actor      the Actor
   * @param teleportee the LocalPlayer to teleport
   * @param warp       the Warp
   */
  @Command(aliases = {"player"}, desc = "warp-player.description", help = "warp-player.help")
  @Require("mywarp.cmd.player")
  @Billable(FeeType.WARP_PLAYER)
  public void player(Actor actor, LocalPlayer teleportee, @Name(VIEWABLE) Warp warp) {
    if (warp.teleport(teleportee).isPositionModified()) {
      actor.sendMessage(msg.getString("warp-player.teleport-successful", teleportee.getName(), warp.getName()));
    } else {
      actor.sendError(msg.getString("warp-player.teleport-failed", teleportee.getName(), warp.getName()));
    }
  }

  /**
   * Reloads MyWarp.
   *
   * @param actor the Actor
   */
  @Command(aliases = {"reload"}, desc = "reload.description", help = "reload.help")
  @Require("mywarp.cmd.reload")
  public void reload(Actor actor) {
    myWarp.reload();
    actor.sendMessage(msg.getString("reload.reload-message"));
  }

}

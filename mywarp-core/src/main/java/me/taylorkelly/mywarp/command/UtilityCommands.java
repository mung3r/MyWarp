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

package me.taylorkelly.mywarp.command;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.command.parametric.annotation.Billable;
import me.taylorkelly.mywarp.command.parametric.annotation.Sender;
import me.taylorkelly.mywarp.command.parametric.annotation.Usable;
import me.taylorkelly.mywarp.command.parametric.annotation.Viewable;
import me.taylorkelly.mywarp.command.util.CommandUtil;
import me.taylorkelly.mywarp.command.util.NoSuchWorldException;
import me.taylorkelly.mywarp.command.util.paginator.StringPaginator;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.Game;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.service.economy.FeeType;
import me.taylorkelly.mywarp.service.teleport.TeleportService;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.Set;

/**
 * Bundles utility commands.
 */
public final class UtilityCommands {

  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;
  private final CommandHandler commandHandler;
  private final TeleportService teleportService;
  private final Game game;

  /**
   * Creates an instance.
   *
   * @param myWarp          the MyWarp instance used in commands
   * @param commandHandler  the CommandHandler instance used in commands
   * @param teleportService the TeleportService to be used as base in commands
   * @param game            the Game instance used in commands
   */
  UtilityCommands(MyWarp myWarp, CommandHandler commandHandler, TeleportService teleportService, Game game) {
    this.myWarp = myWarp;
    this.commandHandler = commandHandler;
    this.teleportService = teleportService;
    this.game = game;
  }

  @Command(aliases = {"help"}, desc = "help.description", help = "help.help")
  @Require("mywarp.cmd.help")
  @Billable(FeeType.HELP)
  public void help(Actor actor, @Optional("1") int page) {
    Set<String> usableCommands = commandHandler.getUsableCommands(actor);

    StringPaginator.of(msg.getString("help.heading"), usableCommands).withNote(msg.getString("help.note")).paginate()
            .display(actor, page);
  }

  @Command(aliases = {"point"}, desc = "point.description", help = "point.help")
  @Require("mywarp.cmd.point")
  @Billable(FeeType.POINT)
  public void point(@Sender LocalPlayer player, @Optional @Usable Warp warp) throws NoSuchWorldException {
    if (warp != null) {
      player.setCompassTarget(CommandUtil.toWorld(warp, game), warp.getPosition());
      player.sendMessage(msg.getString("point.set", warp.getName()));
    } else {
      player.resetCompass();
      player.sendMessage(msg.getString("point.reset"));
    }
  }

  @Command(aliases = {"player"}, desc = "warp-player.description", help = "warp-player.help")
  @Require("mywarp.cmd.player")
  @Billable(FeeType.WARP_PLAYER)
  public void player(Actor actor, LocalPlayer teleportee, @Viewable Warp warp) {
    if (teleportService.teleport(teleportee, warp).isPositionModified()) {
      actor.sendMessage(msg.getString("warp-player.teleport-successful", teleportee.getName(), warp.getName()));
    } else {
      actor.sendError(msg.getString("warp-player.teleport-failed", teleportee.getName(), warp.getName()));
    }
  }

  @Command(aliases = {"reload"}, desc = "reload.description", help = "reload.help")
  @Require("mywarp.cmd.reload")
  public void reload(Actor actor) {
    myWarp.reload();
    actor.sendMessage(msg.getString("reload.reload-message"));
  }

}

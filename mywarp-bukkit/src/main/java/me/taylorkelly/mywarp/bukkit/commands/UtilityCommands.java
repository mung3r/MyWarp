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

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding.Sender;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition.Type;
import me.taylorkelly.mywarp.bukkit.util.economy.Billable;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

/**
 * Bundles utility commands.
 */
public class UtilityCommands {

  private static final DynamicMessages
      MESSAGES =
      new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  /**
   * Displays the help.
   *
   * @param actor the Actor
   * @param page  the page
   */
  @Command(aliases = {"help"}, desc = "help.description", help = "help.help")
  @Require("mywarp.warp.basic.help")
  @Billable(FeeType.HELP)
  public void help(Actor actor, @Optional("1") int page) {
    // XXX implement when Intake has the necessary options!
    // CommandMapping commandMapping =
    // MyWarp.getInstance().getDispatcher().get("warp");

    actor.sendError("Help is currently not implemented, sorry.");
  }

  /**
   * Points the compass of a player to a Warp.
   *
   * @param player the LocalPlayer
   * @param warp   the Warp
   */
  @Command(aliases = {"point"}, desc = "point.description", help = "point.help")
  @Require("mywarp.warp.basic.compass")
  @Billable(FeeType.POINT)
  public void point(@Sender LocalPlayer player, @Optional @Condition(Type.USABLE) Warp warp) {
    if (warp != null) {
      warp.asCompassTarget(player);
      player.sendMessage(ChatColor.AQUA + MESSAGES.getString("point.set", player, warp.getName()));
    } else {
      player.resetCompass();
      player.sendMessage(ChatColor.AQUA + MESSAGES.getString("point.reset", player));
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
  @Require("mywarp.admin.warpto")
  @Billable(FeeType.WARP_PLAYER)
  public void player(Actor actor, LocalPlayer teleportee, @Condition(Type.USABLE) Warp warp) {
    switch (warp.teleport(teleportee)) {
      case NONE:
        actor.sendError(MESSAGES.getString("warp-player.teleport-failed", teleportee.getName(),
                                           warp.getName()));
        break;
      case ORIGINAL_LOC:
      case SAFE_LOC:
        actor.sendMessage(ChatColor.AQUA
                          + MESSAGES
            .getString("warp-player.teleport-successful", teleportee.getName(),
                       warp.getName()));
        break;

    }
  }

  /**
   * Reloads MyWarp.
   *
   * @param actor the Actor
   */
  @Command(aliases = {"reload"}, desc = "reload.description", help = "reload.help")
  @Require("mywarp.admin.reload")
  public void reload(Actor actor) {
    MyWarp.getInstance().reload();
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("reload.reload-message"));
  }

}

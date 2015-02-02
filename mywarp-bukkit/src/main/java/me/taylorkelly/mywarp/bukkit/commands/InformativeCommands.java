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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Ordering;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Optional;
import com.sk89q.intake.parametric.annotation.Switch;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.commands.printer.AssetsPrinter;
import me.taylorkelly.mywarp.bukkit.commands.printer.InfoPrinter;
import me.taylorkelly.mywarp.bukkit.util.FormattingUtils;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding.IllegalCommandSenderException;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Condition.Type;
import me.taylorkelly.mywarp.bukkit.util.economy.Billable;
import me.taylorkelly.mywarp.bukkit.util.paginator.StringPaginator;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.List;

/**
 * Bundles commands that provide information about existing Warps.
 */
public class InformativeCommands {

  private static final DynamicMessages
      MESSAGES =
      new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  /**
   * Displays a player's assets.
   *
   * @param actor   the Actor
   * @param creator the LocalPlayer
   * @throws IllegalCommandSenderException if no {@code creator} is given and the given Actor is not
   *                                       a player
   */
  @Command(aliases = {"assets", "limits"}, desc = "assets.description", help = "assets.help")
  @Require("mywarp.warp.basic.assets")
  @Billable(FeeType.ASSETS)
  public void assets(Actor actor, @Optional LocalPlayer creator)
      throws IllegalCommandSenderException {
    if (creator == null) {
      if (actor instanceof LocalPlayer) {
        creator = (LocalPlayer) actor;
      } else {
        throw new IllegalCommandSenderException(actor);
      }
    }
    new AssetsPrinter(creator, MyWarp.getInstance().getLimitManager()).print(actor);

  }

  /**
   * Lists viewable Warps.
   *
   * @param actor   the Actor
   * @param page    the page to display
   * @param creator the optional creator
   * @param name    the optional name
   * @param world   the optional world
   */
  @Command(aliases = {"list", "alist"}, desc = "list.description", help = "list.help")
  @Require("mywarp.warp.basic.list")
  @Billable(FeeType.LIST)
  public void list(final Actor actor, @Optional("1") int page, @Switch('c') final String creator,
                   @Switch('n') final String name, @Switch('w') final String world) {

    Predicate<Warp> predicate = new Predicate<Warp>() {

      @Override
      public boolean apply(Warp input) {
        if (name != null && !StringUtils.containsIgnoreCase(input.getName(), name)) {
          return false;
        }

        if (creator != null) {
          com.google.common.base.Optional<String> creatorName = input.getCreator().getName();
          if (creatorName.isPresent()
              && !StringUtils.containsIgnoreCase(creatorName.get(), creator)) {
            return false;
          }
        }

        if (world != null && !StringUtils.containsIgnoreCase(input.getWorld().getName(), world)) {
          return false;
        }
        return true;
      }

    };

    List<Warp> warps = Ordering.natural().sortedCopy(
        MyWarp.getInstance().getWarpManager()
            .filter(Predicates.<Warp>and(WarpUtils.isViewable(actor), predicate)));

    Function<Warp, String> mapping = new Function<Warp, String>() {

      @Override
      public String apply(Warp input) {
        // 'name' by player
        StringBuilder first = new StringBuilder();
        first.append(ChatColor.WHITE);
        first.append("'");
        first.append(ChatColor.getByChar(input.getType().getColorCharacter()));
        first.append(input.getName());
        first.append(ChatColor.WHITE);
        first.append("' ");
        first.append(MESSAGES.getString("list.by"));
        first.append(" ");
        first.append(ChatColor.ITALIC);

        if (actor instanceof LocalPlayer && input.isCreator((LocalPlayer) actor)) {
          first.append(MESSAGES.getString("list.you"));
        } else {
          com.google.common.base.Optional<String> creatorName = input.getCreator().getName();
          if (creatorName.isPresent()) {
            first.append(creatorName.get());
          } else {
            first.append(input.getCreator().getUniqueId());
          }
        }
        // @(x, y, z)
        StringBuilder last = new StringBuilder();
        last.append(ChatColor.RESET);
        last.append("@(");
        last.append(input.getPosition().getFloorX());
        last.append(", ");
        last.append(input.getPosition().getFloorY());
        last.append(", ");
        last.append(input.getPosition().getFloorZ());
        last.append(")");
        return FormattingUtils.twoColumnAlign(first.toString(), last.toString());
      }

    };

    StringPaginator.of(MESSAGES.getString("list.heading"), warps).withMapping(mapping).paginate()
        .display(actor, page);
  }

  /**
   * Displays informations about a Warp.
   *
   * @param actor the Actor
   * @param warp  the Warp
   */
  @Command(aliases = {"info", "stats"}, desc = "info.description", help = "info.help")
  @Require("mywarp.warp.basic.info")
  @Billable(FeeType.INFO)
  public void info(Actor actor, @Condition(Type.VIEWABLE) Warp warp) {
    new InfoPrinter(warp).print(actor);
  }
}

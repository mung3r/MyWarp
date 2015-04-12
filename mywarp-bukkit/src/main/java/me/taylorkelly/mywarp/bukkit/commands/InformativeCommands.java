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
import com.sk89q.intake.parametric.annotation.Range;
import com.sk89q.intake.parametric.annotation.Switch;
import com.sk89q.intake.util.auth.AuthorizationException;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.bukkit.commands.printer.AssetsPrinter;
import me.taylorkelly.mywarp.bukkit.commands.printer.InfoPrinter;
import me.taylorkelly.mywarp.bukkit.util.FormattingUtils;
import me.taylorkelly.mywarp.bukkit.util.PlayerBinding.IllegalCommandSenderException;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Name;
import me.taylorkelly.mywarp.bukkit.util.WarpBinding.Name.Condition;
import me.taylorkelly.mywarp.bukkit.util.economy.Billable;
import me.taylorkelly.mywarp.bukkit.util.paginator.StringPaginator;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.WarpUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpManager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bundles commands that provide information about existing Warps.
 */
public class InformativeCommands {

  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  private final LimitManager limitManager;
  private final Settings settings;
  private final WarpManager warpManager;

  /**
   * Creates an instance.
   *
   * @param limitManager thr LimitManager the commands should operate on
   * @param settings     the Settings
   * @param warpManager  the WarpManager the commands should operate on
   */
  public InformativeCommands(LimitManager limitManager, Settings settings, WarpManager warpManager) {
    this.limitManager = limitManager;
    this.settings = settings;
    this.warpManager = warpManager;
  }

  /**
   * Displays a player's assets.
   *
   * @param actor   the Actor
   * @param creator the LocalPlayer
   * @throws IllegalCommandSenderException if no {@code creator} is given and the given Actor is not a player
   */
  @Command(aliases = {"assets", "limits"}, desc = "assets.description", help = "assets.help")
  @Require("mywarp.cmd.assets.self")
  @Billable(FeeType.ASSETS)
  public void assets(Actor actor, @Optional LocalPlayer creator)
      throws IllegalCommandSenderException, AuthorizationException {
    if (creator == null) {
      if (actor instanceof LocalPlayer) {
        creator = (LocalPlayer) actor;
      } else {
        throw new IllegalCommandSenderException(actor);
      }
    } else if (!actor.hasPermission("mywarp.cmd.assets")) {
      throw new AuthorizationException();
    }
    new AssetsPrinter(creator, limitManager, settings).print(actor);

  }

  /**
   * Lists viewable Warps.
   *
   * @param actor   the Actor
   * @param page    the page to display
   * @param creator the optional creator
   * @param name    the optional name
   * @param radius  the optional radius
   * @param world   the optional world
   * @throws IllegalCommandSenderException if the {@code r} flag is used by an Actor that is not an Entity
   */
  @Command(aliases = {"list", "alist"}, desc = "list.description", help = "list.help")
  @Require("mywarp.cmd.list")
  @Billable(FeeType.LIST)
  public void list(final Actor actor, @Optional("1") int page, @Switch('c') final String creator,
                   @Switch('n') final String name,
                   @Switch('r') @Range(min = 1, max = Integer.MAX_VALUE) final Integer radius,
                   @Switch('w') final String world) throws IllegalCommandSenderException {

    // build the listing predicate
    List<Predicate<Warp>> predicates = new ArrayList<Predicate<Warp>>();
    predicates.add(WarpUtils.isViewable(actor));

    if (creator != null) {
      predicates.add(new Predicate<Warp>() {
        @Override
        public boolean apply(Warp input) {
          com.google.common.base.Optional<String> creatorName = input.getCreator().getName();
          return creatorName.isPresent() && StringUtils.containsIgnoreCase(creatorName.get(), creator);
        }
      });
    }

    if (name != null) {
      predicates.add(new Predicate<Warp>() {
        @Override
        public boolean apply(Warp input) {
          return StringUtils.containsIgnoreCase(input.getName(), name);
        }
      });
    }

    if (radius != null) {
      if (!(actor instanceof LocalEntity)) {
        throw new IllegalCommandSenderException(actor);
      }

      LocalEntity entity = (LocalEntity) actor;

      final UUID worldId = entity.getWorld().getUniqueId();

      final int squaredRadius = radius * radius;
      final Vector3 position = entity.getPosition();
      predicates.add(new Predicate<Warp>() {
        @Override
        public boolean apply(Warp input) {
          return input.getWorldIdentifier().equals(worldId)
                 && input.getPosition().distanceSquared(position) <= squaredRadius;
        }
      });
    }

    if (world != null) {
      predicates.add(new Predicate<Warp>() {
        @Override
        public boolean apply(Warp input) {
          return StringUtils.containsIgnoreCase(input.getWorld().getName(), world);
        }
      });
    }

    //query the warps
    List<Warp> warps = Ordering.natural().sortedCopy(warpManager.filter(Predicates.<Warp>and(predicates)));

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
          Profile creator = input.getCreator();
          first.append(creator.getName().or(creator.getUniqueId().toString()));
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

    // display
    StringPaginator.of(MESSAGES.getString("list.heading"), warps).withMapping(mapping).paginate().display(actor, page);
  }

  /**
   * Displays information about a Warp.
   *
   * @param actor the Actor
   * @param warp  the Warp
   */
  @Command(aliases = {"info", "stats"}, desc = "info.description", help = "info.help")
  @Require("mywarp.cmd.info")
  @Billable(FeeType.INFO)
  public void info(Actor actor, @Name(Condition.VIEWABLE) Warp warp) {
    new InfoPrinter(warp).print(actor);
  }
}

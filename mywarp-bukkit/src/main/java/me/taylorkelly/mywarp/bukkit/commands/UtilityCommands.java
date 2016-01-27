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

package me.taylorkelly.mywarp.bukkit.commands;

import com.sk89q.intake.Command;
import com.sk89q.intake.CommandCallable;
import com.sk89q.intake.CommandMapping;
import com.sk89q.intake.Require;
import com.sk89q.intake.context.CommandLocals;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.parametric.annotation.Optional;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.bukkit.MyWarpPlugin;
import me.taylorkelly.mywarp.bukkit.util.paginator.StringPaginator;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.PlayerBinding.Sender;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.Name;
import me.taylorkelly.mywarp.bukkit.util.parametric.binding.WarpBinding.Name.Condition;
import me.taylorkelly.mywarp.bukkit.util.parametric.economy.Billable;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.CommandUtils;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.apache.commons.lang.text.StrBuilder;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bundles utility commands.
 */
public class UtilityCommands {

  private static final DynamicMessages MESSAGES = new DynamicMessages(CommandUtils.RESOURCE_BUNDLE_NAME);
  private final MyWarp myWarp;
  private final MyWarpPlugin plugin;

  /**
   * Creates an instance.
   *
   * @param myWarp the MyWarp instance
   */
  public UtilityCommands(MyWarp myWarp, MyWarpPlugin plugin) {
    this.myWarp = myWarp;
    this.plugin = plugin;
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
    Set<String> usableCommands = new TreeSet<String>();
    CommandLocals locals = new CommandLocals();
    locals.put(Actor.class, actor);

    flattenCommands(usableCommands, locals, "", plugin.getDispatcher());

    StringPaginator.of(MESSAGES.getString("help.heading"), usableCommands)
        .withNote(ChatColor.GRAY + MESSAGES.getString("help.note") + ChatColor.WHITE).paginate().display(actor, page);
  }

  /**
   * Adds a all commands from the given Dispatcher to the given Collection, transforming them into Strings that include
   * the full command string as the user would enter it. Commands that are not usable under the given CommandLocals are
   * excluded and the given prefix is added before all commands. <p>This algorithm actually calls every Command, it is
   * <b> not</b> lazy.</p>
   *
   * @param entries    the Collection the Commands are added to
   * @param locals     the CommandLocals
   * @param prefix     the prefix
   * @param dispatcher the Dispatcher to add
   */
  private void flattenCommands(Collection<String> entries, CommandLocals locals, String prefix, Dispatcher dispatcher) {
    for (CommandMapping rootCommand : dispatcher.getCommands()) {
      flattenCommands(entries, locals, prefix, rootCommand);
    }
  }

  /**
   * Adds a all commands from the given CommandMapping to the given Collection, transforming them into Strings that
   * include the full command string as the user would enter it. Commands that are not usable under the given
   * CommandLocals are excluded and the given prefix is added before all commands. <p>This algorithm actually calls
   * every Command, it is <b> not</b> lazy.</p>
   *
   * @param entries the Collection the Commands are added to
   * @param locals  the CommandLocals
   * @param prefix  the prefix
   * @param current the CommandMapping to add
   */
  private void flattenCommands(Collection<String> entries, CommandLocals locals, String prefix,
                               CommandMapping current) {
    CommandCallable currentCallable = current.getCallable();
    if (!currentCallable.testPermission(locals)) {
      return;
    }
    StrBuilder builder = new StrBuilder().append(prefix).append(prefix.isEmpty() ? '/' : ' ');

    //subcommands
    if (currentCallable instanceof Dispatcher) {
      builder.append(current.getPrimaryAlias());
      flattenCommands(entries, locals, builder.toString(), (Dispatcher) currentCallable);
    } else {
      // the end
      builder.appendWithSeparators(current.getAllAliases(), "|");
      builder.append(' ');
      builder.append(current.getDescription().getUsage());
      entries.add(builder.toString());
    }
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
  public void point(@Sender LocalPlayer player, @Optional @Name(Condition.USABLE) Warp warp) {
    if (warp != null) {
      player.setCompassTarget(warp.getWorld(), warp.getPosition());
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
  @Require("mywarp.cmd.player")
  @Billable(FeeType.WARP_PLAYER)
  public void player(Actor actor, LocalPlayer teleportee, @Name(Condition.VIEWABLE) Warp warp) {
    if (warp.teleport(teleportee).isPositionModified()) {
      actor.sendMessage(
          ChatColor.AQUA + MESSAGES.getString("warp-player.teleport-successful", teleportee.getName(), warp.getName()));
    } else {
      actor.sendError(MESSAGES.getString("warp-player.teleport-failed", teleportee.getName(), warp.getName()));
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
    actor.sendMessage(ChatColor.AQUA + MESSAGES.getString("reload.reload-message"));
  }

}

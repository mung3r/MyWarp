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

package me.taylorkelly.mywarp.bukkit.commands.printer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.bukkit.commands.UsageCommands;
import me.taylorkelly.mywarp.bukkit.util.CommandUtils;
import me.taylorkelly.mywarp.bukkit.util.FormattingUtils;
import me.taylorkelly.mywarp.limits.Limit;
import me.taylorkelly.mywarp.limits.LimitManager;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

/**
 * Prints a certain player's assets, showing active limits and Warps sorted to the corresponding limit.
 */
public class AssetsPrinter {

  private static final List<Limit.Type> DISPLAYABLE_TYPES = Arrays.asList(Limit.Type.PRIVATE, Limit.Type.PUBLIC);
  private static final DynamicMessages MESSAGES = new DynamicMessages(UsageCommands.RESOURCE_BUNDLE_NAME);

  private final LocalPlayer creator;
  private final LimitManager limitManager;
  private final Settings settings;

  /**
   * Creates an instance.
   *
   * @param creator      the player whose assets should be displayed
   * @param limitManager the limitManager that manages the limits that should be displayed
   * @param settings     the Settings
   */
  public AssetsPrinter(LocalPlayer creator, LimitManager limitManager, Settings settings) {
    this.creator = creator;
    this.limitManager = limitManager;
    this.settings = settings;
  }

  /**
   * Prints the assets to the given receiver.
   *
   * @param receiver the Actor who is receiving this print
   */
  public void print(Actor receiver) {
    // display the heading
    String heading = " " + MESSAGES.getString("assets.heading", creator.getName()) + " ";
    receiver.sendMessage(ChatColor.GOLD + FormattingUtils.center(heading, '-'));

    // display the limits
    Multimap<Limit, Warp> index = limitManager.getWarpsPerLimit(creator);
    for (Entry<Limit, Collection<Warp>> entry : index.asMap().entrySet()) {
      printLimit(receiver, entry.getKey(), entry.getValue());
    }
  }

  /**
   * Prints the given Limit to the receiver. The given Warps will be matched to the individual Limit.Types and displayed
   * accordingly.
   *
   * @param receiver the Actor who is receiving this print
   * @param limit    the limit
   * @param warps    the Warps that are affected by the given Limit
   */
  private void printLimit(Actor receiver, Limit limit, Collection<Warp> warps) {
    ImmutableMultimap.Builder<Limit.Type, Warp> builder = ImmutableMultimap.builder();

    // sort warps to types
    for (Warp warp : warps) {
      for (Limit.Type type : Limit.Type.values()) {
        if (type.getCondition().apply(warp)) {
          builder.put(type, warp);
        }
      }
    }

    // display...
    ImmutableMultimap<Limit.Type, Warp> index = builder.build();

    // ...the total limit
    receiver.sendMessage(ChatColor.GRAY + MESSAGES
        .getString("assets.total", CommandUtils.joinWorlds(limit.getAffectedWorlds()),
                   warpLimitCount(index.get(Limit.Type.TOTAL).size(), limit.getLimit(Limit.Type.TOTAL))));

    // ... all other limits
    List<String> limitStrings = new ArrayList<String>();
    for (Limit.Type type : DISPLAYABLE_TYPES) {
      Collection<Warp> privateWarps = index.get(type);

      limitStrings.add(ChatColor.GOLD + MESSAGES.getString("assets." + type.lowerCaseName(), // NON-NLS
                                                           warpLimitCount(privateWarps.size(), limit.getLimit(type)))
                       + " " + ChatColor.WHITE + ChatColor.ITALIC + CommandUtils.joinWarps(privateWarps));
    }

    receiver.sendMessage(FormattingUtils.toList(limitStrings));
  }

  /**
   * Creates a String with the count-suffix from the given warp-count and the given limit-maximum.
   *
   * @param warpCount     the warp-count
   * @param limitMaxiumum the limit-maximum
   * @return the count-suffix
   */
  private String warpLimitCount(int warpCount, int limitMaxiumum) {
    StringBuilder count = new StringBuilder();
    count.append(warpCount);
    if (settings.isLimitsEnabled()) {
      count.append('/').append(limitMaxiumum);
    }
    return count.toString();
  }
}

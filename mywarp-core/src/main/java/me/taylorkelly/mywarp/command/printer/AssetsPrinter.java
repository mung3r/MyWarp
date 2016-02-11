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

package me.taylorkelly.mywarp.command.printer;

import com.google.common.collect.ImmutableMultimap;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.command.CommandHandler;
import me.taylorkelly.mywarp.limits.Limit;
import me.taylorkelly.mywarp.limits.LimitService;
import me.taylorkelly.mywarp.util.Message;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.warp.Warp;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prints a certain player's assets, showing active limits and Warps sorted to the corresponding limit.
 */
public class AssetsPrinter {

  private static final List<Limit.Type> DISPLAYABLE_TYPES = Arrays.asList(Limit.Type.PRIVATE, Limit.Type.PUBLIC);
  private static final DynamicMessages msg = new DynamicMessages(CommandHandler.RESOURCE_BUNDLE_NAME);

  private final LocalPlayer creator;
  private final LimitService limitService;
  private final Settings settings;

  /**
   * Creates an instance.
   *
   * @param creator      the player whose assets should be displayed
   * @param limitService the limitService that manages the limits that should be displayed
   * @param settings     the Settings
   */
  public AssetsPrinter(LocalPlayer creator, LimitService limitService, Settings settings) {
    this.creator = creator;
    this.limitService = limitService;
    this.settings = settings;
  }

  /**
   * Prints the assets to the given receiver.
   *
   * @param receiver the Actor who is receiving this print
   */
  public void print(Actor receiver) {
    // display the heading
    String heading = " " + msg.getString("assets.heading", creator.getName()) + " ";
    receiver.sendMessage(Message.builder().append(Message.Style.HEADLINE_1).append(heading).build());

    // display the limits
    Map<Limit, List<Warp>> index = limitService.getWarpsPerLimit(creator);
    for (Entry<Limit, List<Warp>> entry : index.entrySet()) {
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
    Message.Builder totalMsg = Message.builder();
    totalMsg.append(Message.Style.HEADLINE_2);

    totalMsg.append(msg.getString("assets.total"));
    totalMsg.append(" ");
    totalMsg.appendWithSeparators(limit.getAffectedWorlds());
    totalMsg.append(" ");
    warpLimitCount(totalMsg, index.get(Limit.Type.TOTAL).size(), limit.getLimit(Limit.Type.TOTAL));
    totalMsg.append(":");

    receiver.sendMessage(totalMsg.build());

    // ... all other limits
    for (Limit.Type type : DISPLAYABLE_TYPES) {
      Collection<Warp> typeWarps = index.get(type);

      Message.Builder limitMsg = Message.builder();
      limitMsg.append(Message.Style.KEY);
      limitMsg.append(msg.getString("assets." + type.lowerCaseName()));
      limitMsg.append(" ");
      warpLimitCount(limitMsg, typeWarps.size(), limit.getLimit(type));
      limitMsg.append(": ");
      limitMsg.append(Message.Style.VALUE);
      limitMsg.appendWithSeparators(typeWarps);

      receiver.sendMessage(limitMsg.build());
    }
  }

  /**
   * Creates the count-suffix from the given warp-count and the given limit-maximum to the given builder.
   *
   * @param builder       the builder
   * @param warpCount     the warp-count
   * @param limitMaxiumum the limit-maximum
   * @return builder with appended contents
   */
  private Message.Builder warpLimitCount(Message.Builder builder, int warpCount, int limitMaxiumum) {
    builder.append("(");
    builder.append(warpCount);
    if (settings.isLimitsEnabled()) {
      builder.append('/').append(limitMaxiumum);
    }
    builder.append(")");
    return builder;
  }
}

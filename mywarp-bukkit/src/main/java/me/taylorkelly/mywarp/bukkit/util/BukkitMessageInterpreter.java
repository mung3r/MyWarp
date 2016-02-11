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

package me.taylorkelly.mywarp.bukkit.util;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.util.Message;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.ChatColor;

/**
 * Interprets {@link Message} instances by returning human readable string representations that are usable for Bukkit's
 * message system.
 */
public class BukkitMessageInterpreter {

  private Message.Style lastStyle = Message.Style.DEFAULT;
  private StringBuilder builder;

  private BukkitMessageInterpreter(StringBuilder builder) {
    this.builder = builder;
  }

  /**
   * Interprets the given {@code message} and returns a human readable string with the appropriate styles.
   *
   * @param message the message to interpret
   * @return a string representation
   */
  public static String interpret(Message message) {
    return new BukkitMessageInterpreter(new StringBuilder()).interpret(message.getElements()).toString();

  }

  /**
   * Interprets the given objects and returns a human readable string.
   *
   * @param elements the objects to interpret
   * @return a StringBuilder containing the string representations
   */
  private StringBuilder interpret(Iterable<Object> elements) {
    for (Object element : elements) {
      interpret(element);
    }
    return builder;
  }

  /**
   * Interprets the given object and returns a human readable string.
   *
   * @param element the object to interpret
   * @return a StringBuilder containing the string representation
   */
  private StringBuilder interpret(Object element) {
    if (element instanceof Message.Style) {
      Message.Style style = (Message.Style) element;

      lastStyle = style;
      return builder.append(resolveStyle(style));
    } // Warp

    if (element instanceof Warp) {
      Warp warp = (Warp) element;
      switch (warp.getType()) {
        case PRIVATE:
          builder.append(ChatColor.RED);
          break;
        case PUBLIC:
        default:
          builder.append(ChatColor.GREEN);
      }

      builder.append(warp.getName());
      return builder.append(resolveStyle(lastStyle));
    }

    // LocalWorld
    if (element instanceof LocalWorld) {
      return builder.append(((LocalWorld) element).getName());
    }

    // Actor
    if (element instanceof Actor) {
      builder.append(ChatColor.ITALIC);
      builder.append(((Actor) element).getName());

      return builder.append(resolveStyle(lastStyle));
    }

    // Profile
    if (element instanceof Profile) {
      Profile profile = (Profile) element;

      builder.append(ChatColor.ITALIC);
      builder.append(profile.getName().or(profile.getUniqueId().toString()));

      return builder.append(resolveStyle(lastStyle));
    }

    //default
    return builder.append(element);
  }

  /**
   * Resolves the given style into Bukkit {@link ChatColor}s.
   *
   * @param style the style
   * @return a string with the appropriate ChatColor representations
   */
  private static String resolveStyle(Message.Style style) {
    // Bukkit's ChatColors directly represent Minecraft's formatting codes and their behavior: colors always reset
    // formatting to normal! To use a formatting with a color, the formatting must be given after the color.
    switch (style) {
      case ERROR:
        return ChatColor.RED.toString();
      case INFO:
        return ChatColor.GRAY.toString();
      case HEADLINE_1:
        return ChatColor.GOLD.toString() + ChatColor.BOLD.toString();
      case HEADLINE_2:
        return ChatColor.WHITE.toString();
      case KEY:
        return ChatColor.GRAY.toString();
      case VALUE:
        return ChatColor.WHITE.toString();
      case DEFAULT:
      default:
        return ChatColor.AQUA.toString();
    }
  }

}

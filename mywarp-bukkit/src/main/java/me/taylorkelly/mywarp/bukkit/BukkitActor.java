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

package me.taylorkelly.mywarp.bukkit;

import me.taylorkelly.mywarp.Actor;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Locale;

/**
 * References a Bukkit {@link CommandSender}.
 */
public class BukkitActor implements Actor {

  private final CommandSender sender;
  private final Locale locale;

  /**
   * Initializes this BukkitActor.
   *
   * @param sender the Bukkit CommandSender
   * @param locale the Locale of the CommandSender
   */
  public BukkitActor(CommandSender sender, Locale locale) {
    this.sender = sender;
    this.locale = locale;
  }

  /**
   * Gets the underlying {@code CommandSender}.
   *
   * @return the CommandSender
   */
  public CommandSender getCommandSender() {
    return sender;
  }

  @Override
  public void sendMessage(String msg) {
    getCommandSender().sendMessage(msg);
  }

  @Override
  public void sendError(String msg) {
    sendMessage(ChatColor.RED + msg);
  }

  @Override
  public boolean hasPermission(String node) {
    return sender.hasPermission(node);
  }

  @Override
  public String getName() {
    return sender.getName();
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

}

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

package me.taylorkelly.mywarp.bukkit;

import me.taylorkelly.mywarp.bukkit.util.BukkitMessageInterpreter;
import me.taylorkelly.mywarp.platform.AbstractActor;
import me.taylorkelly.mywarp.platform.Settings;
import me.taylorkelly.mywarp.util.Message;

import org.bukkit.command.CommandSender;

import java.util.Locale;

/**
 * References a Bukkit {@link CommandSender}.
 */
public class BukkitActor extends AbstractActor {

  protected final Settings settings;

  private final CommandSender sender;

  /**
   * Creates an instance referencing the given {@code sender}.
   *
   * @param sender   the Bukkit CommandSender
   * @param settings the configured settings
   */
  BukkitActor(CommandSender sender, Settings settings) {
    this.sender = sender;
    this.settings = settings;
  }

  /**
   * Gets the underlying {@code CommandSender}.
   *
   * @return the CommandSender
   */
  public CommandSender getWrapped() {
    return sender;
  }

  @Override
  public String getName() {
    return sender.getName();
  }

  @Override
  public Locale getLocale() {
    return settings.getLocalizationDefaultLocale();
  }

  @Override
  public void sendMessage(Message msg) {
    sender.sendMessage(BukkitMessageInterpreter.interpret(msg));
  }

  @Override
  public boolean hasPermission(String node) {
    return sender.hasPermission(node);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BukkitActor that = (BukkitActor) o;

    return sender.equals(that.sender);

  }

  @Override
  public int hashCode() {
    return sender.hashCode();
  }

  @Override
  public String toString() {
    return "BukkitActor{" + "sender=" + sender + '}';
  }
}

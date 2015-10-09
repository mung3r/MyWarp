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

package me.taylorkelly.mywarp;

import java.util.Locale;

/**
 * Someone who can interact with MyWarp. <p>Typically an implementation is provided by the platform running MyWarp.</p>
 */
public interface Actor {

  /**
   * Gets the name of this Actor.
   *
   * @return the name
   */
  String getName();

  /**
   * Returns whether this Actor has the given permission.
   *
   * @param node the permission-node
   * @return {@code true} if the Actor has the permission
   */
  boolean hasPermission(String node);

  /**
   * Sends a message to this Actor.
   *
   * @param msg the message
   */
  void sendMessage(String msg);

  /**
   * Sends an error message to this Actor.
   *
   * @param msg the error-message
   */
  void sendError(String msg);

  /**
   * Gets the current Locale of this Actor.
   *
   * @return the Actor's Locale
   */
  Locale getLocale();

}

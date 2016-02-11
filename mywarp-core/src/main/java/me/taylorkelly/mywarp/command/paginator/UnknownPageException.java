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

package me.taylorkelly.mywarp.command.paginator;

/**
 * Indicates that the requested page does not exist.
 */
public class UnknownPageException extends Exception {

  private static final long serialVersionUID = 2658425917194218460L;

  private final int highestPage;

  /**
   * Constructs this exception.
   *
   * @param highestPage the number of the highest existing page
   */
  public UnknownPageException(int highestPage) {
    this.highestPage = highestPage;
  }

  /**
   * Constructs this exception with the given message.
   *
   * @param message     the message
   * @param highestPage the number of the highest existing page
   */
  public UnknownPageException(String message, int highestPage) {
    super(message);
    this.highestPage = highestPage;
  }

  /**
   * Constructs this exception with the given message and the given cause.
   *
   * @param message     the message
   * @param cause       the cause of this exception
   * @param highestPage the number of the highest existing page
   */
  public UnknownPageException(String message, Throwable cause, int highestPage) {
    super(message, cause);
    this.highestPage = highestPage;
  }

  /**
   * Constructs this exception with the given cause.
   *
   * @param cause       the cause
   * @param highestPage the number of the highest existing page
   */
  public UnknownPageException(Throwable cause, int highestPage) {
    super(cause);
    this.highestPage = highestPage;
  }

  /**
   * Gets the number of the highest existing page.
   *
   * @return the highestPage
   */
  public int getHighestPage() {
    return highestPage;
  }
}

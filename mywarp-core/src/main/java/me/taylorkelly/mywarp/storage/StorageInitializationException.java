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

package me.taylorkelly.mywarp.storage;

/**
 * Thrown when a {@link WarpStorage} cannot be initialized.
 */
public class StorageInitializationException extends Exception {

  private static final long serialVersionUID = -2033822282111044971L;

  /**
   * Constructs this exception.
   */
  public StorageInitializationException() {
    super();
  }

  /**
   * Constructs this exception with the given message.
   *
   * @param message the message
   */
  public StorageInitializationException(String message) {
    super(message);
  }

  /**
   * Constructs this exception with the given message and the given cause.
   *
   * @param message the message
   * @param cause   the cause of this exception
   */
  public StorageInitializationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs this exception with the given cause.
   *
   * @param cause the cause
   */
  public StorageInitializationException(Throwable cause) {
    super(cause);
  }
}

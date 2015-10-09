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

package me.taylorkelly.mywarp.util;

/**
 * Thrown when an attempt is made to access a world that does not exist on the server at the moment, the attempt was
 * made.
 */
public class NoSuchWorldException extends RuntimeException {

  private static final long serialVersionUID = 6540699388762912185L;

  private final String worldRepresentation;

  /**
   * Creates an instance.
   *
   * @param worldRepresentation the world's representation
   */
  public NoSuchWorldException(String worldRepresentation) {
    this.worldRepresentation = worldRepresentation;
  }

  /**
   * Gets a readable representation of the world which does not exist. This can be a world's name or a world's unique
   * identifier.
   *
   * @return the world's representation
   */
  public String getWorldRepresentation() {
    return worldRepresentation;
  }

}

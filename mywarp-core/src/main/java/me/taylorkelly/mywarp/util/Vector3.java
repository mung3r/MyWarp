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
 * Represent an immutable 3 component vector using doubles. Vectors returned by modifying methods are new instances.
 */
public class Vector3 {

  private final double x;
  private final double y;
  private final double z;

  /**
   * Creates an instance that uses the given components.
   *
   * @param x the x component.
   * @param y the y component.
   * @param z the z component.
   */
  public Vector3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Returns the x component.
   *
   * @return The x component
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the y component.
   *
   * @return The y component
   */
  public double getY() {
    return y;
  }

  /**
   * Returns the z component.
   *
   * @return The z component
   */
  public double getZ() {
    return z;
  }

  /**
   * Returns the floor of the x component as a long integer.
   *
   * @return The floor of the x component as a long integer
   */
  public long getFloorX() {
    return (long) Math.floor(x);
  }

  /**
   * Returns the floor of the y component as a long integer.
   *
   * @return The floor of the y component as a long integer
   */
  public long getFloorY() {
    return (long) Math.floor(y);
  }

  /**
   * Returns the floor of the z component as a long integer.
   *
   * @return The floor of the z component as a long integer
   */
  public long getFloorZ() {
    return (long) Math.floor(z);
  }

  /**
   * Gets the square of the distance between this vector and another Vector3.
   *
   * @param v The other vector
   * @return The square of the distance between the two
   */
  public double distanceSquared(Vector3 v) {
    return Math.pow(v.x - x, 2) + Math.pow(v.y - y, 2) + Math.pow(v.z - z, 2);
  }

  /**
   * Gets the distance between this vector and another Vector3.
   *
   * @param v The other vector
   * @return The distance between the two
   */
  public double distance(Vector3 v) {
    return Math.sqrt(distanceSquared(v));
  }

  /**
   * Adds the vector components to this vector, returning the results as new vector.
   *
   * @param x The x component
   * @param y The y component
   * @param z The z component
   * @return The results of the operation as a new vector
   */
  public Vector3 add(double x, double y, double z) {
    return new Vector3(this.x + x, this.y + y, this.z + z);
  }

  /**
   * Subtracts the vector components from this vector, returning the results as new vector.
   *
   * @param x The x component
   * @param y The y component
   * @param z The z component
   * @return The results of the operation as a new vector
   */
  public Vector3 sub(double x, double y, double z) {
    return new Vector3(this.x - x, this.y - y, this.z - z);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(z);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Vector3 other = (Vector3) obj;
    if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
      return false;
    }
    if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
      return false;
    }
    if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Vector3 [x=" + x + ", y=" + y + ", z=" + z + "]";
  }
}

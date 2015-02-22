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
 * Represents an immutable euler direction, made up of pitch, yaw and roll components.
 */
public class EulerDirection {

  private final float pitch;
  private final float yaw;
  private final float roll;

  /**
   * Creates an instance.
   *
   * @param pitch the pitch component
   * @param yaw   the yaw component
   * @param roll  the roll component
   */
  public EulerDirection(float pitch, float yaw, float roll) {
    this.pitch = pitch;
    this.yaw = yaw;
    this.roll = roll;
  }

  /**
   * Returns the pitch component.
   *
   * @return the pitch component
   */
  public float getPitch() {
    return pitch;
  }

  /**
   * Returns the yaw component.
   *
   * @return the yaw component
   */
  public float getYaw() {
    return yaw;
  }

  /**
   * Returns the roll component.
   *
   * @return the roll component
   */
  public float getRoll() {
    return roll;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(pitch);
    result = prime * result + Float.floatToIntBits(roll);
    result = prime * result + Float.floatToIntBits(yaw);
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
    EulerDirection other = (EulerDirection) obj;
    if (Float.floatToIntBits(pitch) != Float.floatToIntBits(other.pitch)) {
      return false;
    }
    if (Float.floatToIntBits(roll) != Float.floatToIntBits(other.roll)) {
      return false;
    }
    if (Float.floatToIntBits(yaw) != Float.floatToIntBits(other.yaw)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "EulerDirection [pitch=" + pitch + ", yaw=" + yaw + ", roll=" + roll + "]";


  }

}

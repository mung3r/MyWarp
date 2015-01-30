/*
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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
package me.taylorkelly.mywarp.util.profile;

import java.util.UUID;

import com.google.common.base.Optional;

/**
 * A pairing of a player's UUID and username.
 */
public interface Profile {

    /**
     * Gets the player's unique ID.
     * 
     * @return the unique ID
     */
    UUID getUniqueId();

    /**
     * Gets the player's name.
     * 
     * @return the name
     */
    Optional<String> getName();

    /**
     * Returns whether this profile belongs to the same player than the given
     * unique ID.
     * 
     * @param uniqueId
     *            the player's unique ID
     * @return true if this profile belongs the the same player
     */
    boolean isProfileOf(UUID uniqueId);
}

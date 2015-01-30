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
 * Represents a Service that creates {@link Profile}s from unique IDs or
 * player-names.
 */
public interface ProfileService {

    /**
     * Gets the Profile of the given unique ID. If the service is unable to find
     * a name matching the unique ID, the returned Profile will not have a name
     * value and calls to {@link Profile#getName()} may fail.
     * <p>
     * Implementations must be thread-safe!
     * </p>
     * 
     * @param uniqueId
     *            the unique ID
     * @return the corresponding Profile
     */
    Profile get(UUID uniqueId);

    /**
     * Gets an Optional containing the Profile of the given name.
     * 
     * @param name
     *            the name
     * @return an Optional containing the Profile
     */
    Optional<Profile> get(String name);

}

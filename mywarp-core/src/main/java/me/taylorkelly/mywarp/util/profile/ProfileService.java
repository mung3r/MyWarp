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

package me.taylorkelly.mywarp.util.profile;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.UUID;

/**
 * Creates or resolves {@link Profile}s from unique identifiers or player-names. Implementations are expected to be
 * thread safe. <p>Typically an implementation is provided by the platform running MyWarp.</p>
 */
public interface ProfileService {

  /**
   * Gets the Profile of the given unique identifier. If the service is unable to find a name matching the identifier,
   * the returned Profile might not have a name value and calls to {@link Profile#getName()} may fail.
   *
   * @param uniqueId the unique identifier
   * @return the corresponding Profile
   */
  Profile getByUniqueId(UUID uniqueId);

  /**
   * Gets an Optional containing the Profile of the given name, if such a Profile exists. <p>Since Minecraft usernames
   * are case-insensitive, {@link Profile#getName()} may return a name with a different case than the requested one. The
   * returned one is than guaranteed to have the correct case. </p> <p>Calling this method might result in a blocking
   * call to a remote server to get the Profiles.</p>
   *
   * @param name the name
   * @return an Optional containing the Profile
   */
  Optional<Profile> getByName(String name);


  /**
   * Gets the the Profiles for all given names, if such a Profile exists. If none of the given names has a Profile, an
   * empty List will be returned. <p>Since Minecraft usernames are case-insensitive, {@link Profile#getName()} may
   * return a name with a different case than the requested one. The returned one is than guaranteed to have the correct
   * case. </p> <p>Calling this method might result in a blocking call to a remote server to get the Profiles.</p>
   *
   * @param names an Iterable of names
   * @return a List with all existing Profiles
   */
  ImmutableList<Profile> getByName(Iterable<String> names);

}

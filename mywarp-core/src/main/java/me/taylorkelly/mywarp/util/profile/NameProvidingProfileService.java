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

package me.taylorkelly.mywarp.util.profile;

import com.google.common.base.Optional;

import java.util.UUID;

/**
 * A ProfileService that has an additional method to get the current name of a uniqueId. By combining this with an
 * {@link me.taylorkelly.mywarp.util.profile.NameProvidingProfileService.LazyProfile}, implementations can create lazy
 * profile services that query the name only when needed. <p>Typically an implementation is provided by the platform
 * running MyWarp.</p>
 */
public interface NameProvidingProfileService extends ProfileService {

  /**
   * Gets an Optional containing the name that belongs to the given unique ID.
   *
   * @param uniqueId the unique ID
   * @return an Optional containing the corresponding name
   */
  Optional<String> getName(UUID uniqueId);

  /**
   * A Profile that uses an {@link NameProvidingProfileService} to get the name whenever necessary.
   */
  class LazyProfile implements Profile {

    private final NameProvidingProfileService service;
    private final UUID uniqueId;

    /**
     * Creates an instance of the given unique ID.
     *
     * @param service  the NameProvidingProfileService
     * @param uniqueId the unique ID
     */
    public LazyProfile(NameProvidingProfileService service, UUID uniqueId) {
      this.service = service;
      this.uniqueId = uniqueId;
    }

    @Override
    public UUID getUniqueId() {
      return uniqueId;
    }

    @Override
    public Optional<String> getName() {
      return service.getName(uniqueId);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
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
      LazyProfile other = (LazyProfile) obj;
      if (uniqueId == null) {
        if (other.uniqueId != null) {
          return false;
        }
      } else if (!uniqueId.equals(other.uniqueId)) {
        return false;
      }
      return true;
    }

  }
}

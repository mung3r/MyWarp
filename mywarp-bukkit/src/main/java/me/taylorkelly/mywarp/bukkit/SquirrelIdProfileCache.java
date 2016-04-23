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

package me.taylorkelly.mywarp.bukkit;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.sk89q.squirrelid.cache.HashMapCache;
import com.sk89q.squirrelid.cache.SQLiteCache;
import com.sk89q.squirrelid.resolver.BukkitPlayerService;
import com.sk89q.squirrelid.resolver.CacheForwardingService;
import com.sk89q.squirrelid.resolver.CombinedProfileService;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;

import me.taylorkelly.mywarp.bukkit.util.AbstractListener;
import me.taylorkelly.mywarp.platform.profile.Profile;
import me.taylorkelly.mywarp.platform.profile.ProfileCache;
import me.taylorkelly.mywarp.util.MyWarpLogger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * A ProfileCache implementation that uses the SquirrelID library to lookup UUIDs.
 */
public class SquirrelIdProfileCache extends AbstractListener implements ProfileCache {

  private static final Logger log = MyWarpLogger.getLogger(SquirrelIdProfileCache.class);

  private final CacheForwardingService resolver;
  private com.sk89q.squirrelid.cache.ProfileCache cache;

  /**
   * Creates an instance, using the given file to store the SQLite cache.
   *
   * @param cacheFile the cache file
   */
  public SquirrelIdProfileCache(File cacheFile) {
    try {
      cache = new SQLiteCache(cacheFile);
    } catch (IOException e) {
      log.warn("Failed to access SQLite profile cache. Player names will be resolved from memory.", e);
      cache = new HashMapCache();
    }
    resolver =
        new CacheForwardingService(
            new CombinedProfileService(BukkitPlayerService.getInstance(), HttpRepositoryService.forMinecraft()), cache);
  }

  @Override
  public Profile getByUniqueId(UUID uniqueId) {
    return new LazyProfile(this, uniqueId);
  }

  @Override
  public Optional<Profile> getByName(String name) {
    try {
      com.sk89q.squirrelid.Profile profile = resolver.findByName(name);
      if (profile != null) {
        return Optional.of(wrap(profile));
      }
    } catch (IOException e) {
      log.error(String.format("Failed to find UUID for '%s'.", name), e);
    } catch (InterruptedException e) {
      log.error(String.format("Failed to find UUID for '%s' as the process was interrupted.", name), e);
    }
    return Optional.absent();
  }

  @Override
  public ImmutableList<Profile> getByName(Iterable<String> names) {
    final ImmutableList.Builder<Profile> builder = ImmutableList.builder();

    try {
      resolver.findAllByName(names, new Predicate<com.sk89q.squirrelid.Profile>() {
        @Override
        public boolean apply(com.sk89q.squirrelid.Profile input) {
          builder.add(wrap(input));
          return true;
        }
      });
    } catch (IOException e) {
      log.error("Failed to lookup UUIDs.", e);
    } catch (InterruptedException e) {
      log.error("Failed to lookup UUIDs as the process was interrupted.", e);
    }

    return builder.build();
  }

  /**
   * Gets an Optional containing the name that belongs to the given unique ID.
   *
   * @param uniqueId the unique ID
   * @return an Optional containing the corresponding name
   */
  public Optional<String> getName(UUID uniqueId) {
    com.sk89q.squirrelid.Profile profile = cache.getIfPresent(uniqueId);

    if (profile != null) {
      return Optional.of(profile.getName());
    }
    return Optional.absent();
  }

  /**
   * Adapts between a {@link com.sk89q.squirrelid.Profile} and a {@link Profile}.
   *
   * @param profile the {@link com.sk89q.squirrelid.Profile}
   * @return the Profile
   */
  private Profile wrap(com.sk89q.squirrelid.Profile profile) {
    return new LazyProfile(this, profile.getUniqueId());
  }

  /**
   * Called asynchronous when a player logs in.
   *
   * @param event the AsyncPlayerPreLoginEvent
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
    if (!event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
      return;
    }
    //SquirrelID's cache is thread-safe
    cache.put(new com.sk89q.squirrelid.Profile(event.getUniqueId(), event.getName()));
  }

  /**
   * A Profile that uses an {@link SquirrelIdProfileCache} to get the name whenever necessary.
   */
  private class LazyProfile implements Profile {

    private final SquirrelIdProfileCache service;
    private final UUID uniqueId;

    /**
     * Creates an instance of the given unique ID.
     *
     * @param service  the NameProvidingProfileService
     * @param uniqueId the unique ID
     */
    LazyProfile(SquirrelIdProfileCache service, UUID uniqueId) {
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

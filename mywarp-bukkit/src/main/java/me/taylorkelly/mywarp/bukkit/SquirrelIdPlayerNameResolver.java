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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.sk89q.squirrelid.Profile;
import com.sk89q.squirrelid.cache.HashMapCache;
import com.sk89q.squirrelid.cache.SQLiteCache;
import com.sk89q.squirrelid.resolver.BukkitPlayerService;
import com.sk89q.squirrelid.resolver.CacheForwardingService;
import com.sk89q.squirrelid.resolver.CombinedProfileService;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;

import me.taylorkelly.mywarp.bukkit.util.AbstractListener;
import me.taylorkelly.mywarp.platform.PlayerNameResolver;
import me.taylorkelly.mywarp.util.MyWarpLogger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * A PlayerNameResolver implementation that uses the SquirrelID library to lookup UUIDs.
 */
class SquirrelIdPlayerNameResolver extends AbstractListener implements PlayerNameResolver {

  private static final Logger log = MyWarpLogger.getLogger(SquirrelIdPlayerNameResolver.class);

  private final CacheForwardingService resolver;
  private com.sk89q.squirrelid.cache.ProfileCache cache;

  /**
   * Creates an instance, using the given file to store the SQLite cache.
   *
   * @param cacheFile the cache file
   */
  SquirrelIdPlayerNameResolver(File cacheFile) {
    try {
      cache = new SQLiteCache(cacheFile);
    } catch (IOException e) {
      log.warn("Failed to access SQLite profile cache. Player names will be resolved from memory.", e);
      cache = new HashMapCache();
    }
    resolver =
            new CacheForwardingService(
                    new CombinedProfileService(BukkitPlayerService.getInstance(), HttpRepositoryService.forMinecraft()),
                    cache);
  }

  @Override
  public Optional<String> getByUniqueId(UUID uniqueId) {
    Profile profile = cache.getIfPresent(uniqueId);

    if (profile != null) {
      return Optional.of(profile.getName());
    }
    return Optional.absent();
  }

  @Override
  public ImmutableMap<UUID, String> getByUniqueId(Iterable<UUID> uniqueIds) {
    ImmutableMap.Builder<UUID, String> builder = ImmutableMap.builder();
    ImmutableMap<UUID, Profile> allPresent = cache.getAllPresent(uniqueIds);

    for (UUID uniqueId : uniqueIds) {
      Profile profile = allPresent.get(uniqueId);
      if (profile != null) {
        builder.put(uniqueId, profile.getName());
      }
    }
    return builder.build();
  }

  @Override
  public Optional<UUID> getByName(String name) {
    try {
      Profile profile = resolver.findByName(name);
      if (profile != null) {
        return Optional.of(profile.getUniqueId());
      }
    } catch (IOException e) {
      log.error(String.format("Failed to find UUID for '%s'.", name), e);
    } catch (InterruptedException e) {
      log.error(String.format("Failed to find UUID for '%s' as the process was interrupted.", name), e);
    }
    return Optional.absent();
  }

  @Override
  public ImmutableMap<String, UUID> getByName(Iterable<String> names) {
    final ImmutableMap.Builder<String, UUID> builder = ImmutableSortedMap.orderedBy(String.CASE_INSENSITIVE_ORDER);

    try {
      resolver.findAllByName(names, new Predicate<Profile>() {
        @Override
        public boolean apply(Profile input) {
          builder.put(input.getName(), input.getUniqueId());
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
    cache.put(new Profile(event.getUniqueId(), event.getName()));
  }
}

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

package me.taylorkelly.mywarp.bukkit.profile;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.taylorkelly.mywarp.bukkit.AbstractListener;
import me.taylorkelly.mywarp.util.profile.NameProvidingProfileService;
import me.taylorkelly.mywarp.util.profile.Profile;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import com.google.common.base.Optional;
import com.sk89q.squirrelid.cache.HashMapCache;
import com.sk89q.squirrelid.cache.ProfileCache;
import com.sk89q.squirrelid.resolver.BukkitPlayerService;
import com.sk89q.squirrelid.resolver.CacheForwardingService;
import com.sk89q.squirrelid.resolver.CombinedProfileService;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;

/**
 * A ProfileService implementation that uses the SquirrelID library to lookup
 * UUIDs.
 */
public class SquirrelIdProfileService extends AbstractListener implements NameProvidingProfileService {

    private static final Logger LOG = Logger.getLogger(SquirrelIdProfileService.class.getName());

    private ProfileCache cache = new HashMapCache(); // REVIEW use SQLite cache?
    private CacheForwardingService resolver = new CacheForwardingService(new CombinedProfileService(
            BukkitPlayerService.getInstance(), HttpRepositoryService.forMinecraft()), cache);

    @Override
    public Profile get(UUID uniqueId) {
        return new LazyProfile(this, uniqueId);
    }

    @Override
    public Optional<Profile> get(String name) {
        try {
            com.sk89q.squirrelid.Profile profile = resolver.findByName(name);
            if (profile != null) {
                return Optional.of(wrap(profile));
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Failed to find UUID for '" + name + "'.", e); // NON-NLS
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Failed to find UUID for '" + name + "' as the process was interuptted.", e); // NON-NLS
                                                                                                                // NON-NLS
        }
        return Optional.absent();
    }

    @Override
    public Optional<String> getName(UUID uniqueId) {
        com.sk89q.squirrelid.Profile profile = cache.getIfPresent(uniqueId);

        if (profile != null) {
            return Optional.of(profile.getName());
        }
        return Optional.absent();
    }

    /**
     * Adapts between a {@link com.sk89q.squirrelid.Profile} and a
     * {@link Profile}.
     * 
     * @param profile
     *            the com.sk89q.squirrelid.Profile
     * @return the Profile
     */
    private Profile wrap(com.sk89q.squirrelid.Profile profile) {
        return new LazyProfile(this, profile.getUniqueId());
    }

    /**
     * Called when a player logs in.
     * 
     * @param event
     *            the PlayerLoginEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        cache.put(new com.sk89q.squirrelid.Profile(player.getUniqueId(), player.getName()));
    }

}

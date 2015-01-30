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
package me.taylorkelly.mywarp.bukkit.permissions;

import java.util.logging.Logger;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Resolves the group of a player by calling a configured {@link GroupResolver}.
 */
public class GroupResolverManager implements GroupResolver {

    private static final Logger LOG = Logger.getLogger(GroupResolver.class.getName());
    private final GroupResolver resolver = setupResolver();

    /**
     * Setups a GroupResolver by searching for supported plugins on the server.
     * Vault will always take preference over other supported plugins for
     * obvious reasons.
     * 
     * This method will never return {@code null}, but a SuperpermsResolver
     * instead.
     * 
     * @return the created GroupResolver
     */
    private GroupResolver setupResolver() {
        // check for Vault first!
        try {
            RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = Bukkit
                    .getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                LOG.info("Using Vault for group support."); // NON-NLS
                return new VaultResolver(permissionProvider.getProvider());
            }
        } catch (NoClassDefFoundError e) {
            // the class is not in the classpath (perhaps Vault is
            // not installed), so we continue.
        }

        Plugin checkPlugin;

        checkPlugin = Bukkit.getPluginManager().getPlugin("bPermissions"); // NON-NLS
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            // we support bPermissions2 only
            if (checkPlugin.getDescription().getVersion().charAt(0) == '2') {
                LOG.info("Using bPermissions v" + checkPlugin.getDescription().getVersion() // NON-NLS
                        + " for group support."); // NON-NLS
                return new BPermissions2Resolver();
            }
        }

        checkPlugin = Bukkit.getPluginManager().getPlugin("GroupManager"); // NON-NLS
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            LOG.info("Using GroupManager v" + checkPlugin.getDescription().getVersion() // NON-NLS
                    + " for group support."); // NON-NLS
            return new GroupManagerResolver((GroupManager) checkPlugin);
        }

        checkPlugin = Bukkit.getPluginManager().getPlugin("PermissionsEx"); // NON-NLS
        if (checkPlugin != null && checkPlugin.isEnabled()) {
            LOG.info("Using PermissionsEx v" + checkPlugin.getDescription().getVersion() // NON-NLS
                    + " for group support."); // NON-NLS
            return new PermissionsExResolver();
        }

        LOG.info("No supported permissions plugin found, using Superperms fallback for group support."); // NON-NLS
        return new SuperpermsResolver();
    }

    @Override
    public boolean hasGroup(Player player, String groupId) {
        return resolver.hasGroup(player, groupId);
    }
}

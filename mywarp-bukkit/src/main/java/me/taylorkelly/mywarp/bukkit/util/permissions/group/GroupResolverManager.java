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

package me.taylorkelly.mywarp.bukkit.util.permissions.group;

import net.milkbowl.vault.permission.Permission;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

/**
 * Resolves the group of a player by calling a configured {@link GroupResolver}.
 */
public class GroupResolverManager implements GroupResolver {

  private static final Logger log = Logger.getLogger(GroupResolver.class.getName());
  private final GroupResolver resolver = setupResolver();

  /**
   * Setups a GroupResolver by searching for supported plugins on the server. Vault will always take preference over
   * other supported plugins for obvious reasons. <p> This method will never return {@code null}, but a {@link
   * SuperPermsResolver} instead. </p>
   *
   * @return the created GroupResolver
   */
  private GroupResolver setupResolver() {
    // check for Vault first!
    try {
      RegisteredServiceProvider<Permission>
          permissionProvider =
          Bukkit.getServicesManager().getRegistration(Permission.class);
      if (permissionProvider != null) {
        log.info("Using Vault for group support.");
        return new VaultResolver(permissionProvider.getProvider());
      }
    } catch (NoClassDefFoundError e) {
      // the class is not in the classpath (perhaps Vault is not installed), so we continue.
    }

    Plugin checkPlugin;

    checkPlugin = Bukkit.getPluginManager().getPlugin("bPermissions");
    if (checkPlugin != null && checkPlugin.isEnabled() && checkPlugin.getDescription().getVersion().charAt(0) == '2') {
      // we support bPermissions2 only
      log.info("Using bPermissions v" + checkPlugin.getDescription().getVersion() + " for group support.");
      return new BPermissions2Resolver();
    }

    checkPlugin = Bukkit.getPluginManager().getPlugin("GroupManager");
    if (checkPlugin != null && checkPlugin.isEnabled()) {
      log.info("Using GroupManager v" + checkPlugin.getDescription().getVersion() + " for group support.");
      return new GroupManagerResolver((GroupManager) checkPlugin);
    }

    // PermissionsEx automatically applies these
    log.info("Using Superperms fallback ('group.[GROUPNAME]) for group support.");
    return new SuperPermsResolver();
  }

  @Override
  public boolean hasGroup(Player player, String groupId) {
    return resolver.hasGroup(player, groupId);
  }
}

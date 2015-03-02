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

package me.taylorkelly.mywarp.bukkit;

import me.taylorkelly.mywarp.AbstractPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.Settings;
import me.taylorkelly.mywarp.bukkit.permissions.GroupResolver;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.util.profile.ProfileService;

import org.apache.commons.lang.LocaleUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A reference to a Player in Bukkit.
 */
public class BukkitPlayer extends AbstractPlayer {

  private static final Map<String, Locale> LOCALE_CACHE = new HashMap<String, Locale>();
  private static final Logger log = Logger.getLogger(BukkitPlayer.class.getName());

  private final Player player;
  private final BukkitAdapter adapter;
  private final GroupResolver groupResolver;
  private final ProfileService profileService;
  private final Settings settings;

  /**
   * Creates an instance that references the given Player.
   *
   * @param player         the player
   * @param adapter        the BukkitAdapter
   * @param groupResolver  the groupResolver
   * @param profileService the profileService
   * @param settings       the Settings
   */
  public BukkitPlayer(Player player, BukkitAdapter adapter, GroupResolver groupResolver, ProfileService profileService,
                      Settings settings) {
    this.player = player;
    this.adapter = adapter;
    this.groupResolver = groupResolver;
    this.profileService = profileService;
    this.settings = settings;
  }

  /**
   * Gets the Player referenced by this BukkitPlayer.
   *
   * @return the loaded Player
   */
  public Player getLoadedPlayer() {
    return player;
  }

  @Override
  public void sendMessage(String msg) {
    player.sendMessage(msg);
  }

  @Override
  public void sendError(String msg) {
    player.sendMessage(ChatColor.RED + msg);
  }

  @Override
  public boolean hasPermission(String node) {
    return player.hasPermission(node);
  }

  @Override
  public String getName() {
    return player.getName();
  }

  @Override
  public boolean hasGroup(String groupId) {
    return groupResolver.hasGroup(player, groupId);
  }

  @Override
  public Profile getProfile() {
    return profileService.getByUniqueId(player.getUniqueId());
  }

  @Override
  public Locale getLocale() {
    Locale locale = settings.getLocalizationDefaultLocale();
    if (settings.isLocalizationPerPlayer()) {
      try {
        String minecraftLocale = getLanguage(player);
        if (LOCALE_CACHE.containsKey(minecraftLocale)) {
          locale = LOCALE_CACHE.get(minecraftLocale);
        } else {
          locale = LocaleUtils.toLocale(minecraftLocale);
          LOCALE_CACHE.put(minecraftLocale, locale);
        }
      } catch (Exception e) {
        log.log(Level.WARNING, String.format("Failed to get locale from %1$s, defaulting to %2$s.", getName(), locale));
      }
    }
    return locale;
  }

  /**
   * Attempts to get the locale used by the given player (client-side). <p> This method relies on reflection to load the
   * minecraft-player-object through the craftbukkit-implementation and access its {@code locale} field. It may break on
   * Minecraft or CraftBukkit updates. </p>
   *
   * @param player the player
   * @return the used locale as string
   * @throws IllegalAccessException    if the underlying reflection fails
   * @throws NoSuchFieldException      if the underlying reflection fails
   * @throws InvocationTargetException if the underlying reflection fails
   * @throws NoSuchMethodException     if the underlying reflection fails
   */
  private String getLanguage(Player player)
      throws IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
    Object minecraftHandle = player.getClass().getMethod("getHandle").invoke(player);
    Field localeField = minecraftHandle.getClass().getDeclaredField("locale");
    localeField.setAccessible(true);
    return (String) localeField.get(minecraftHandle);
  }

  @Override
  public void resetCompass() {
    player.setCompassTarget(player.getWorld().getSpawnLocation());
  }

  @Override
  public UUID getUniqueId() {
    return player.getUniqueId();
  }

  @Override
  public double getHealth() {
    return player.getHealth();
  }

  @Override
  public void setCompassTarget(LocalWorld world, Vector3 position) {
    Location bukkitLoc = new Location(adapter.adapt(world), position.getX(), position.getY(), position.getZ());
    player.setCompassTarget(bukkitLoc);
  }

  @Override
  public LocalWorld getWorld() {
    return adapter.adapt(player.getWorld());
  }

  @Override
  public Vector3 getPosition() {
    Location bukkitLoc = player.getLocation();
    return new Vector3(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ());
  }

  @Override
  public EulerDirection getRotation() {
    Location bukkitLoc = player.getLocation();
    return new EulerDirection(bukkitLoc.getPitch(), bukkitLoc.getYaw(), 0);
  }

  @Override
  public void teleport(LocalWorld world, Vector3 position, EulerDirection rotation) {
    Location
        bukkitLoc =
        new Location(adapter.adapt(world), position.getX(), position.getY(), position.getZ(), rotation.getYaw(),
                     rotation.getPitch());
    teleportRecursive(player, bukkitLoc, true);
  }

  /**
   * Teleports the given {@link org.bukkit.entity.Entity} to the given {@link org.bukkit.Location}.
   *
   * @param teleportee          the Entity to teleport
   * @param bukkitLoc           the Location where the Entity is teleported
   * @param teleportTamedHorses whether ridden, tamed horses should be teleported too
   */
  private void teleportRecursive(Entity teleportee, Location bukkitLoc, boolean teleportTamedHorses) {
    Entity vehicle = null;

    // handle vehicles
    if (teleportTamedHorses) {
      if (teleportee.getVehicle() instanceof Horse && ((Horse) teleportee.getVehicle()).isTamed()) {
        vehicle = teleportee.getVehicle();
      }
    }
    teleportee.leaveVehicle();

    // load the chunk if needed
    int blockX = bukkitLoc.getBlockX();
    int blockZ = bukkitLoc.getBlockZ();
    if (!bukkitLoc.getWorld().isChunkLoaded(blockX, blockZ)) {
      bukkitLoc.getWorld().refreshChunk(blockX, blockZ);
    }

    // play the smoke effect
    for (int i = 0; i < 4; i++) {
      bukkitLoc.getWorld().playEffect(bukkitLoc, Effect.SMOKE, 4);
    }

    // teleport the entity
    teleportee.teleport(bukkitLoc);

    // teleport the vehicle
    if (vehicle != null) {
      teleportRecursive(vehicle, bukkitLoc, teleportTamedHorses);
      vehicle.setPassenger(teleportee);
    }
  }
}

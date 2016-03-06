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

import me.taylorkelly.mywarp.bukkit.util.ReflectiveLocaleResolver;
import me.taylorkelly.mywarp.bukkit.util.conversation.AcceptancePromptFactory;
import me.taylorkelly.mywarp.bukkit.util.conversation.WelcomeEditorFactory;
import me.taylorkelly.mywarp.bukkit.util.permission.group.GroupResolver;
import me.taylorkelly.mywarp.platform.Actor;
import me.taylorkelly.mywarp.platform.LocalPlayer;
import me.taylorkelly.mywarp.platform.LocalWorld;
import me.taylorkelly.mywarp.platform.Settings;
import me.taylorkelly.mywarp.platform.profile.Profile;
import me.taylorkelly.mywarp.platform.profile.ProfileCache;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.warp.Warp;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.UUID;

/**
 * A reference to a Player in Bukkit.
 */
public class BukkitPlayer extends BukkitActor implements LocalPlayer {

  private static final Logger log = MyWarpLogger.getLogger(BukkitPlayer.class);

  private final AcceptancePromptFactory acceptancePromptFactory;
  private final WelcomeEditorFactory welcomeEditorFactory;
  private final GroupResolver groupResolver;
  private final ProfileCache profileCache;

  /**
   * Creates an instance that references the given {@code player}.
   *
   * @param player                  the player
   * @param acceptancePromptFactory the factory to create warp acceptance conversations
   * @param welcomeEditorFactory    the factory to create welcome message editor conversations
   * @param groupResolver           the group resolver
   * @param profileCache            the configured profile cache
   * @param settings                the configured settings
   */
  BukkitPlayer(Player player, AcceptancePromptFactory acceptancePromptFactory,
               WelcomeEditorFactory welcomeEditorFactory, GroupResolver groupResolver, ProfileCache profileCache,
               Settings settings) {
    super(player, settings);
    this.acceptancePromptFactory = acceptancePromptFactory;
    this.welcomeEditorFactory = welcomeEditorFactory;
    this.groupResolver = groupResolver;
    this.profileCache = profileCache;
  }

  @Override
  public Player getWrapped() {
    return (Player) super.getWrapped();
  }

  @Override
  public UUID getUniqueId() {
    return getWrapped().getUniqueId();
  }

  @Override
  public Profile getProfile() {
    return profileCache.getByUniqueId(getWrapped().getUniqueId());
  }

  @Override
  public Locale getLocale() {
    Locale locale = super.getLocale();
    if (settings.isLocalizationPerPlayer()) {
      try {
        locale = ReflectiveLocaleResolver.INSTANCE.resolve(getWrapped());
      } catch (ReflectiveLocaleResolver.UnresolvableLocaleException e) {
        log.warn(String.format("Failed to resolve the Locale for %s, defaulting to %s.", getName(), locale), e);
      }
    }
    return locale;
  }

  @Override
  public void initiateAcceptanceConversation(Actor initiator, Warp warp) {
    acceptancePromptFactory.create(this, warp, initiator);
  }

  @Override
  public void initiateWelcomeChangeConversation(Warp warp) {
    welcomeEditorFactory.create(this, warp);
  }

  @Override
  public boolean hasGroup(String groupId) {
    return groupResolver.hasGroup(getWrapped(), groupId);
  }

  @Override
  public double getHealth() {
    return getWrapped().getHealth();
  }

  @Override
  public void setCompassTarget(LocalWorld world, Vector3 position) {
    Location bukkitLoc = new Location(BukkitAdapter.adapt(world), position.getX(), position.getY(), position.getZ());
    getWrapped().setCompassTarget(bukkitLoc);
  }

  @Override
  public void resetCompass() {
    getWrapped().setCompassTarget(getWrapped().getWorld().getSpawnLocation());
  }

  @Override
  public LocalWorld getWorld() {
    return BukkitAdapter.adapt(getWrapped().getWorld());
  }

  @Override
  public Vector3 getPosition() {
    Location bukkitLoc = getWrapped().getLocation();
    return new Vector3(bukkitLoc.getX(), bukkitLoc.getY(), bukkitLoc.getZ());
  }

  @Override
  public EulerDirection getRotation() {
    Location bukkitLoc = getWrapped().getLocation();
    return new EulerDirection(bukkitLoc.getPitch(), bukkitLoc.getYaw(), 0);
  }

  @Override
  public void teleport(LocalWorld world, Vector3 position, EulerDirection rotation, boolean teleportTamedHorse) {
    Location
        bukkitLoc =
        new Location(BukkitAdapter.adapt(world), position.getX(), position.getY(), position.getZ(), rotation.getYaw(),
                     rotation.getPitch());
    teleportRecursive(getWrapped(), bukkitLoc, teleportTamedHorse);
  }

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
      teleportRecursive(vehicle, bukkitLoc, true);
      vehicle.setPassenger(teleportee);
    }
  }
}

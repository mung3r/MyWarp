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

import me.taylorkelly.mywarp.AbstractActor;
import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.bukkit.util.BukkitMessageInterpreter;
import me.taylorkelly.mywarp.bukkit.util.ReflectiveLocaleResolver;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Message;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
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
public class BukkitPlayer extends AbstractActor implements LocalPlayer {

  private static final Logger log = MyWarpLogger.getLogger(BukkitPlayer.class);

  private final Player player;
  private final MyWarpPlugin plugin;

  /**
   * Creates an instance that references the given Player.
   *
   * @param player the player
   * @param plugin the plugin instance
   */
  public BukkitPlayer(Player player, MyWarpPlugin plugin) {
    this.player = player;
    this.plugin = plugin;
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
  public void initiateAcceptanceConversation(Actor initiator, Warp warp) {
    plugin.getAcceptancePromptFactory().create(this, warp, initiator);
  }

  @Override
  public void initiateWelcomeChangeConversation(Warp warp) {
    plugin.getWelcomeEditorFactory().create(this, warp);
  }

  @Override
  public void sendMessage(Message msg) {
    player.sendMessage(BukkitMessageInterpreter.interpret(msg));
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
    return plugin.getGroupResolver().hasGroup(player, groupId);
  }

  @Override
  public Profile getProfile() {
    return plugin.getProfileService().getByUniqueId(player.getUniqueId());
  }

  @Override
  public Locale getLocale() {
    Locale locale = plugin.getSettings().getLocalizationDefaultLocale();
    if (plugin.getSettings().isLocalizationPerPlayer()) {
      try {
        locale = ReflectiveLocaleResolver.INSTANCE.resolve(player);
      } catch (ReflectiveLocaleResolver.UnresolvableLocaleException e) {
        log.warn(String.format("Failed to resolve the Locale for %s, defaulting to %s.", player.getName(), locale), e);
      }
    }
    return locale;
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
    Location
        bukkitLoc =
        new Location(plugin.getAdapter().adapt(world), position.getX(), position.getY(), position.getZ());
    player.setCompassTarget(bukkitLoc);
  }

  @Override
  public LocalWorld getWorld() {
    return plugin.getAdapter().adapt(player.getWorld());
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
        new Location(plugin.getAdapter().adapt(world), position.getX(), position.getY(), position.getZ(),
                     rotation.getYaw(), rotation.getPitch());
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BukkitPlayer that = (BukkitPlayer) o;

    return player.equals(that.player);

  }

  @Override
  public int hashCode() {
    return player.hashCode();
  }
}

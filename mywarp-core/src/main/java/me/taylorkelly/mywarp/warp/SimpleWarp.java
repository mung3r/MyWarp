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

package me.taylorkelly.mywarp.warp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.safety.TeleportService.TeleportStatus;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.NoSuchWorldException;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A simple implementation of a Warp. Use a {@link WarpBuilder} to create instances.
 */
class SimpleWarp implements Warp {

  private static final double GRAVITY_CONSTANT = 0.8;
  private static final DynamicMessages MESSAGES = new DynamicMessages(Warp.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;
  private final String name;
  private final Date creationDate;
  private final Set<Profile> invitedPlayers;
  private final Set<String> invitedGroups;
  private volatile Profile creator;
  private volatile Warp.Type type;
  private volatile UUID worldIdentifier;
  private volatile Vector3 position;
  private volatile EulerDirection rotation;
  private volatile int visits;
  private volatile String welcomeMessage;

  /**
   * Creates a instance with the given values.
   * <p>This method should never be called manually. Use a {@link WarpBuilder} instead.</p>
   *
   * @param myWarp          the running MyWarp instance
   * @param name            the warp's name
   * @param creationDate    the warp's creation date
   * @param invitedPlayers  a Set of player profiles invited to this warp
   * @param invitedGroups   a set of group identifiers invited to this warp
   * @param creator         the profile of the warp's creator
   * @param type            the warp's type
   * @param worldIdentifier the identifier of the world that holds the warp
   * @param position        the warp's position
   * @param rotation        the warp's rotation
   * @param visits          the number of times the warp has been visited
   * @param welcomeMessage  the warp's welcome message
   * @throws NullPointerException     if one of the given values is {@code null}
   * @throws IllegalArgumentException if {@code invitedPlayers} or {@code invitedGroups} contains {@code null}
   */
  SimpleWarp(MyWarp myWarp, String name, Date creationDate, Set<Profile> invitedPlayers, Set<String> invitedGroups,
             Profile creator, Type type, UUID worldIdentifier, Vector3 position, EulerDirection rotation, int visits,
             String welcomeMessage) {
    this.myWarp = checkNotNull(myWarp);
    this.name = checkNotNull(name);
    this.creationDate = checkNotNull(creationDate);
    checkArgument(!checkNotNull(invitedPlayers).contains(null), "'invitedPlayers' must not contain null.");
    this.invitedPlayers = invitedPlayers;
    checkArgument(!checkNotNull(invitedGroups).contains(null), "'invitedGroups' must not contain null.");
    this.invitedGroups = invitedGroups;
    this.creator = checkNotNull(creator);
    this.type = checkNotNull(type);
    this.worldIdentifier = checkNotNull(worldIdentifier);
    this.position = checkNotNull(position);
    this.rotation = checkNotNull(rotation);
    this.visits = checkNotNull(visits);
    this.welcomeMessage = checkNotNull(welcomeMessage);
  }

  @Override
  public void asCompassTarget(LocalPlayer player) {
    player.setCompassTarget(getWorld(), position);
  }

  @Override
  public boolean isViewable(Actor actor) {
    if (actor.hasPermission("mywarp.override.view")) {
      return true;
    }
    if (actor instanceof LocalEntity) {
      return isUsable((LocalEntity) actor);
    }
    return false;
  }

  @Override
  public boolean isUsable(LocalEntity entity) {
    if (entity instanceof LocalPlayer) {
      LocalPlayer player = (LocalPlayer) entity;
      if (myWarp.getSettings().isControlWorldAccess()) {
        if (!player.canAccessWorld(getWorld())) {
          return false;

        }
      }
      if (player.hasPermission("mywarp.override.use")) {
        return true;
      }
      if (isCreator(player)) {
        return true;
      }
      if (isPlayerInvited(player)) {
        return true;
      }
      for (String groupId : invitedGroups) {
        if (player.hasGroup(groupId)) {
          return true;
        }
      }
    }
    return isType(Warp.Type.PUBLIC);
  }

  @Override
  public boolean isModifiable(Actor actor) {
    if (actor.hasPermission("mywarp.override.modify")) {
      return true;
    }
    if (actor instanceof LocalPlayer && isCreator((LocalPlayer) actor)) {
      return true;
    }
    return false;
  }

  @Override
  public TeleportStatus teleport(LocalEntity entity) {
    TeleportStatus status = myWarp.getTeleportService().safeTeleport(entity, getWorld(), position, rotation);

    switch (status) {
      case ORIGINAL_LOC:
      case SAFE_LOC:
        visits++;
        break;
      case NONE:
        break;
    }
    return status;
  }

  @Override
  public TeleportStatus teleport(LocalPlayer player) {
    TeleportStatus status = teleport((LocalEntity) player);

    switch (status) {
      case ORIGINAL_LOC:
        if (!welcomeMessage.isEmpty()) {
          // TODO color in aqua
          player.sendMessage(getParsedWelcomeMessage(player));
        }
        break;
      case SAFE_LOC:
        player.sendError(MESSAGES.getString("unsafe-loc.closest-location", getName()));
        break;
      case NONE:
        player.sendError(MESSAGES.getString("unsafe-loc.no-teleport", getName()));
        break;
    }
    return status;
  }

  @Override
  public TeleportStatus teleport(LocalPlayer player, FeeProvider.FeeType fee) {
    TeleportStatus status = teleport(player);
    if (myWarp.getSettings().isEconomyEnabled()) {
      switch (status) {
        case NONE:
          break;
        case ORIGINAL_LOC:
        case SAFE_LOC:
          myWarp.getEconomyManager().informativeWithdraw(player, fee);
      }
    }
    return status;
  }

  @Override
  public boolean isCreator(LocalPlayer player) {
    return isCreator(player.getProfile());
  }

  @Override
  public boolean isCreator(Profile profile) {
    return creator.equals(profile);
  }

  @Override
  public boolean isType(Warp.Type type) {
    return this.type == type;
  }

  @Override
  public boolean isPlayerInvited(LocalPlayer player) {
    return isPlayerInvited(player.getProfile());
  }

  @Override
  public boolean isPlayerInvited(Profile profile) {
    return invitedPlayers.contains(profile);
  }

  @Override
  public boolean isGroupInvited(String groupId) {
    return invitedGroups.contains(groupId);
  }

  @Override
  public void inviteGroup(String groupId) {
    invitedGroups.add(groupId);
  }

  @Override
  public void invitePlayer(Profile player) {
    invitedPlayers.add(player);
  }

  @Override
  public void uninviteGroup(String groupId) {
    invitedGroups.remove(groupId);

  }

  @Override
  public void uninvitePlayer(Profile player) {
    invitedPlayers.remove(player);
  }

  @Override
  public String replacePlaceholders(String str, LocalPlayer forWhom) {
    str = StringUtils.replace(str, "%player%", forWhom.getName());
    return replacePlaceholders(str);
  }

  @Override
  public String replacePlaceholders(String str) {
    str = StringUtils.replace(str, "%creator%", creator.getName().or(creator.getUniqueId().toString()));

    str = StringUtils.replace(str, "%warp%", name);
    str = StringUtils.replace(str, "%visits%", Integer.toString(visits));

    str =
        StringUtils.replace(str, "%loc%",
                            "(" + position.getFloorX() + ", " + position.getFloorY() + ", " + position.getFloorZ()
                            + ")");
    str = StringUtils.replace(str, "%getWorld()%", getWorld().getName());

    return str;
  }

  @Override
  public int compareTo(Warp that) {
    return this.name.compareTo(that.getName());
  }

  @Override
  public Profile getCreator() {
    return creator;
  }

  @Override
  public void setCreator(Profile creator) {
    this.creator = creator;
  }

  @Override
  public Set<String> getInvitedGroups() {
    return Collections.unmodifiableSet(invitedGroups);
  }

  @Override
  public Set<Profile> getInvitedPlayers() {
    return Collections.unmodifiableSet(invitedPlayers);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Warp.Type getType() {
    return type;
  }

  @Override
  public void setType(Warp.Type type) {
    this.type = type;
  }

  @Override
  public Date getCreationDate() {
    // date is mutable, so we return a copy
    return new Date(creationDate.getTime());
  }

  @Override
  public int getVisits() {
    return visits;
  }

  @Override
  public String getWelcomeMessage() {
    return welcomeMessage;
  }

  @Override
  public void setWelcomeMessage(String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
  }

  @Override
  public String getParsedWelcomeMessage(LocalPlayer forWhom) {
    return replacePlaceholders(welcomeMessage, forWhom);
  }

  @Override
  public void setLocation(LocalWorld world, Vector3 position, EulerDirection rotation) {
    this.worldIdentifier = world.getUniqueId();
    this.position = position;
    this.rotation = rotation;
  }

  @Override
  public double getPopularityScore() {
    // a basic implementation of the hacker news ranking algorithm detailed
    // at http://amix.dk/blog/post/19574: Older warps receive lower scores
    // due to the influence of the gravity constant.
    double daysExisting = (System.currentTimeMillis() - creationDate.getTime()) / (1000 * 60 * 60 * 24L);
    return visits / Math.pow(daysExisting, GRAVITY_CONSTANT);
  }

  @Override
  public double getVisitsPerDay() {
    // this method might not be 100% exact (considering leap seconds), but
    // within the current Java API there are no alternatives
    long daysSinceCreation = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - creationDate.getTime());
    if (daysSinceCreation <= 0) {
      return visits;
    }
    return visits / daysSinceCreation;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
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
    SimpleWarp other = (SimpleWarp) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public LocalWorld getWorld() {
    Optional<LocalWorld> world = myWarp.getGame().getWorld(worldIdentifier);
    if (!world.isPresent()) {
      throw new NoSuchWorldException(worldIdentifier.toString());
    }
    return world.get();
  }

  @Override
  public Vector3 getPosition() {
    return position;
  }

  @Override
  public EulerDirection getRotation() {
    return rotation;
  }

  @Override
  public UUID getWorldIdentifier() {
    return worldIdentifier;
  }
}

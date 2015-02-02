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

import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp.Type;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A Builder for warps.
 */
public class WarpBuilder {

  private static final DynamicMessages MESSAGES = new DynamicMessages(Warp.RESOURCE_BUNDLE_NAME);

  private final String name;
  private final Profile creator;
  private final Type type;
  private final UUID worldIdentifier;
  private final Vector3 position;
  private final EulerDirection rotation;
  private final Set<Profile> invitedPlayers = new HashSet<Profile>();
  private final Set<String> invitedGroups = new HashSet<String>();
  private Date creationDate = new Date();
  private int visits;
  // REVIEW use LocaleManager.getLocale() ?
  private String welcomeMessage = MESSAGES.getString("default-welcome-message", MyWarp.getInstance() // NON-NLS
      .getSettings().getLocalizationDefaultLocale());

  /**
   * Creates a Builder that will build a Warp with the given values.
   *
   * @param name     the name of the warp
   * @param creator  the Profile of the player who created the warp
   * @param type     the type
   * @param world    the world where the warp is placed
   * @param position the position of the warp
   * @param rotation the rotation of the warp
   */
  public WarpBuilder(String name, Profile creator, Type type, LocalWorld world, Vector3 position,
                     EulerDirection rotation) {
    this(name, creator, type, world.getUniqueId(), position, rotation);
  }

  /**
   * Creates a Builder that will build a Warp with the given values.
   *
   * @param name            the name of the warp
   * @param creator         the Profile of the player who created the warp
   * @param type            the type
   * @param worldIdentifier the name of the world where the warp is placed
   * @param position        the position of the warp
   * @param rotation        the rotation of the warp
   */
  public WarpBuilder(String name, Profile creator, Type type, UUID worldIdentifier, Vector3 position,
                     EulerDirection rotation) {
    this.name = name;
    this.creator = creator;
    this.type = type;
    this.worldIdentifier = worldIdentifier;
    this.position = position;
    this.rotation = rotation;
  }

  /**
   * Sets the creation-date of the warp.
   *
   * @param creationDate the creation-date
   * @return this Builder for chaining
   */
  public WarpBuilder withCreationDate(Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  /**
   * Sets the visits-count of the warp.
   *
   * @param visits the visits-count
   * @return this Builder for chaining
   */
  public WarpBuilder withVisits(int visits) {
    this.visits = visits;
    return this;
  }

  /**
   * Sets the welcome-message of the warp.
   *
   * @param welcomeMessage the welcome-message
   * @return this Builder for chaining
   */
  public WarpBuilder withWelcomeMessage(String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
    return this;
  }

  /**
   * Adds the given player-profile to the set of invited players for this warp.
   *
   * @param player the profile of the player
   * @return this Builder for chaining
   */
  public WarpBuilder addInvitedPlayer(Profile player) {
    this.invitedPlayers.add(player);
    return this;
  }

  /**
   * Adds the given group-ID to the set of invited groups for this warp.
   *
   * @param groupId the ID of the group
   * @return this Builder for chaining
   */
  public WarpBuilder addInvitedGroup(String groupId) {
    this.invitedGroups.add(groupId);
    return this;
  }

  /**
   * Builds the Warp.
   *
   * @return the builded warp
   */
  public Warp build() {
    return new SimpleWarp(this);
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  protected String getName() {
    return name;
  }

  /**
   * Gets the creator.
   *
   * @return the creator
   */
  protected Profile getCreator() {
    return creator;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  protected Type getType() {
    return type;
  }

  /**
   * Gets the unique identifier of the world.
   *
   * @return the world's unique identifier
   */
  protected UUID getWorldIdentifier() {
    return worldIdentifier;
  }

  /**
   * Gets the position.
   *
   * @return the position
   */
  protected Vector3 getPosition() {
    return position;
  }

  /**
   * Gets the rotation.
   *
   * @return the rotation
   */
  protected EulerDirection getRotation() {
    return rotation;
  }

  /**
   * Gets the creationDate.
   *
   * @return the creationDate
   */
  protected Date getCreationDate() {
    return creationDate;
  }

  /**
   * Gets the visits.
   *
   * @return the visits
   */
  protected int getVisits() {
    return visits;
  }

  /**
   * Gets the welcomeMessage.
   *
   * @return the welcomeMessage
   */
  protected String getWelcomeMessage() {
    return welcomeMessage;
  }

  /**
   * Gets the invitedPlayers.
   *
   * @return the invitedPlayers
   */
  protected Set<Profile> getInvitedPlayers() {
    return invitedPlayers;
  }

  /**
   * Gets the invitedGroups.
   *
   * @return the invitedGroups
   */
  protected Set<String> getInvitedGroups() {
    return invitedGroups;
  }
}

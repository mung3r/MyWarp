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

import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Builds {@link Warp}s.
 */
public class WarpBuilder {

  private static final DynamicMessages MESSAGES = new DynamicMessages(Warp.RESOURCE_BUNDLE_NAME);

  private final MyWarp myWarp;
  private final String name;
  private final Set<Profile> invitedPlayers;
  private final Set<String> invitedGroups;
  private final Profile creator;
  private final UUID worldIdentifier;
  private final Vector3 position;
  private final EulerDirection rotation;

  private Date creationDate = new Date();
  private Warp.Type type = Warp.Type.PUBLIC;
  private int visits = 0;
  private String welcomeMessage = MESSAGES.getString("default-welcome-message");

  /**
   * Creates a {@code WarpBuilder} that builds {@code Warp}s using the given values.
   *
   * @param myWarp          the running MyWarp instance
   * @param name            the {@code Warp}'s name
   * @param creator         the {@code Warp}'s creator
   * @param worldIdentifier the identifier of the world the {@code Warp} is located in
   * @param position        the {@code Warp}'s position
   * @param rotation        the {@code Warp}'s rotation
   */
  public WarpBuilder(MyWarp myWarp, String name, Profile creator, UUID worldIdentifier, Vector3 position,
                      EulerDirection rotation) {
    this.invitedPlayers = new HashSet<Profile>();
    this.invitedGroups = new HashSet<String>();

    this.myWarp = myWarp;
    this.name = name;
    this.creator = creator;
    this.worldIdentifier = worldIdentifier;
    this.position = position;
    this.rotation = rotation;
  }

  /**
   * Sets the creation date of the {@code Warp}.
   *
   * @param creationDate the creation date
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  /**
   * Adds the player to the set of players invited to the {@code Warp}.
   *
   * @param player the player profiles
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder addInvitedPlayer(Profile player) {
    invitedPlayers.add(player);
    return this;
  }

  /**
   * Adds each player to the set of players invited to the {@code Warp}.
   *
   * @param players the Iterable of player profiles
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder addInvitedPlayers(Iterable<Profile> players) {
    Iterables.addAll(invitedPlayers, players);
    return this;
  }

  /**
   * Adds the group to the set of groups invited to the {@code Warp}.
   *
   * @param groupId the group identifier
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder addInvitedGroup(String groupId) {
    invitedGroups.add(groupId);
    return this;
  }

  /**
   * Adds each group to the set of groups invited to the {@code Warp}.
   *
   * @param groupIds the {@code Iterable} of group identifier
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder addInvitedGroups(Iterable<String> groupIds) {
    Iterables.addAll(invitedGroups, groupIds);
    return this;
  }

  /**
   * Sets the type of the {@code Warp}.
   *
   * @param type the type
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder setType(Warp.Type type) {
    this.type = type;
    return this;
  }

  /**
   * Sets the amount of visits of the {@code Warp}.
   *
   * @param visits the amount of visits
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder setVisits(int visits) {
    this.visits = visits;
    return this;
  }

  /**
   * Sets the welcome message of the {@code Warp}.
   *
   * @param welcomeMessage the welcome message
   * @return this {@code WarpBuilder}
   */
  public WarpBuilder setWelcomeMessage(String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
    return this;
  }

  /**
   * Builds the {@code Warp} based on the contents of the {@code WarpBuilder}.
   *
   * @return the newly-created {@code Warp}
   */
  public Warp build() {
    return new SimpleWarp(myWarp, name, creationDate, invitedPlayers, invitedGroups, creator, type, worldIdentifier,
                          position, rotation, visits, welcomeMessage);
  }
}

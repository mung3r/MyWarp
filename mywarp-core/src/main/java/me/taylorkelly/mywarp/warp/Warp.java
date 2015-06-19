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

import com.google.common.collect.ComparisonChain;

import me.taylorkelly.mywarp.Actor;
import me.taylorkelly.mywarp.LocalEntity;
import me.taylorkelly.mywarp.LocalPlayer;
import me.taylorkelly.mywarp.LocalWorld;
import me.taylorkelly.mywarp.economy.FeeProvider;
import me.taylorkelly.mywarp.limits.Limit;
import me.taylorkelly.mywarp.teleport.TeleportManager.TeleportStatus;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a Warp. Implementations must ensure that two Warps are equal if, and only if their names are equal. <p>
 * To create a Warp use the {@link WarpBuilder}.</p>
 */
public interface Warp extends Comparable<Warp> {

  String RESOURCE_BUNDLE_NAME = "me.taylorkelly.mywarp.lang.Warp";

  /**
   * Sets this Warp as the player's compass target.
   *
   * @param player the player
   */
  void asCompassTarget(LocalPlayer player);

  /**
   * Returns whether this Warp is viewable by the given Actor.
   *
   * @param actor the Actor
   * @return true if the Actor can view this Warp
   */
  boolean isViewable(Actor actor);

  /**
   * Returns whether this Warp is usable by the given entity.
   *
   * @param entity the entity
   * @return true if the given entity can use this Warp
   */
  boolean isUsable(LocalEntity entity);

  /**
   * Returns whether this Warp is modifiable by the given Actor.
   *
   * @param actor the Actor
   * @return true if the given Actor can modify this Warp
   */
  boolean isModifiable(Actor actor);

  /**
   * Teleports the given entity to this Warp.
   *
   * @param entity the entity
   * @return the status of teleport
   */
  TeleportStatus teleport(LocalEntity entity);

  /**
   * Teleports the given player to this Warp and sends the applicable message.
   *
   * @param player the player
   * @return the status of the teleport
   */
  TeleportStatus teleport(LocalPlayer player);

  /**
   * Teleports the given player to this Warp, sends the applicable message and withdraws the applicable fee.
   *
   * @param player the player
   * @param fee    the fee that identifies the amount
   * @return the status of this teleport
   */
  TeleportStatus teleport(LocalPlayer player, FeeProvider.FeeType fee);

  /**
   * Returns whether the given player is the creator of this Warp.
   *
   * @param player the player
   * @return true if the given player is the creator
   */
  boolean isCreator(LocalPlayer player);

  /**
   * Returns whether the given profile is equal to the profile of the player who created this Warp.
   *
   * @param profile the profile
   * @return true if the profiles are equal
   */
  boolean isCreator(Profile profile);

  /**
   * Returns whether the Warp has the same type as the given type.
   *
   * @param type the type
   * @return true if the given type is the same as this Warp's type
   */
  boolean isType(Warp.Type type);

  /**
   * Returns whether the given player is invited to this Warp.
   *
   * @param player the player
   * @return true if the given player is invited to this Warp
   */
  boolean isPlayerInvited(LocalPlayer player);

  /**
   * Returns whether the given Profile identifies a player who is invited to this Warp.
   *
   * @param profile the Profile
   * @return true if the identified Player is invited to this Warp
   */
  boolean isPlayerInvited(Profile profile);

  /**
   * Returns whether the permission-group identified by the given ID is invited to this Warp.
   *
   * @param groupId the ID of the group
   * @return true if group identified by the given ID is invited to this Warp
   */
  boolean isGroupInvited(String groupId);

  /**
   * Invites the permission-group identified by the given ID to this Warp. This method will also attempt to update this
   * Warp via the active data-connection.
   *
   * @param groupId the ID of the group who should be invited
   */
  void inviteGroup(String groupId);

  /**
   * Invites the player identified by the given Profile to this Warp. This method will also attempt to update this Warp
   * via the active data-connection.
   *
   * @param player the profile of the player who should be invited
   */
  void invitePlayer(Profile player);

  /**
   * Uninvites the permission-group identified by the given ID from this Warp. This method will also attempt to update
   * this Warp via the active data-connection.
   *
   * @param groupId the ID of the group who should be uninvited
   */
  void uninviteGroup(String groupId);

  /**
   * Uninvites the player identified by the given Profile from this Warp. This method will also attempt to update this
   * Warp via the active data-connection.
   *
   * @param player the Profile of the player who should be uninvited
   */
  void uninvitePlayer(Profile player);

  /**
   * Replaces all placeholder in the given string with the values applicable for this Warp and the given player.
   *
   * @param str     the string that contains the placeholder
   * @param forWhom the player for whom the placeholder should be replaced - can be null if there is none
   * @return the string with replaced placeholder
   */
  String replacePlaceholders(String str, LocalPlayer forWhom);

  /**
   * Replaces all placeholder in the given string with the values applicable for this Warp.
   *
   * @param str the string that contains the placeholder
   * @return the string with replaced placeholder
   */
  String replacePlaceholders(String str);

  /**
   * Gets the Profile of this Warp's creator.
   *
   * @return the Profile of the creator of this Warp
   */
  Profile getCreator();

  /**
   * Sets the creator of this Warp to the one identified by the given Profile.
   *
   * @param creator the profile of the new creator
   */
  void setCreator(Profile creator);

  /**
   * Gets an unmodifiable set containing the IDs of all permission-groups invited to this Warp.
   *
   * @return a set with all IDs of groups invited to this Warp
   */
  Set<String> getInvitedGroups();

  /**
   * Gets an unmodifiable set containing the Profiles of all players invited to this Warp.
   *
   * @return a set with all Profiles of players who are invited to this Warp
   */
  Set<Profile> getInvitedPlayers();

  /**
   * Gets this Warp's name.
   *
   * @return the name of this Warp
   */
  String getName();

  /**
   * Gets the world this warp is positioned in.
   *
   * @return the world
   */
  LocalWorld getWorld();

  /**
   * Gets the unique identifier of the world this warp is positioned in.
   *
   * @return the world's unique identifier
   */
  UUID getWorldIdentifier();

  /**
   * Gets this Warp's position.
   *
   * @return the position
   */
  Vector3 getPosition();

  /**
   * Gets this Warp's rotation.
   *
   * @return the rotation
   */
  EulerDirection getRotation();

  /**
   * Gets this Warp's type.
   *
   * @return the type of this Warp
   */
  Warp.Type getType();

  /**
   * Sets the type of this Warp to the given one.
   *
   * @param type the new type
   */
  void setType(Warp.Type type);

  /**
   * Gets this Warp's creation-date.
   *
   * @return the creation-date of this Warp
   */
  Date getCreationDate();

  /**
   * Gets this Warp's visits number.
   *
   * @return the number of times this Warp has been visited
   */
  int getVisits();

  /**
   * Gets this Warp's welcome message. Use {@link #getParsedWelcomeMessage(LocalPlayer)} to get the welcome message
   * without any variables.
   *
   * @return the raw, unparsed welcome message of this Warp
   */
  String getWelcomeMessage();

  /**
   * Sets the welcome-message of this Warp to the given one.
   *
   * @param welcomeMessage the new welcome-message
   */
  void setWelcomeMessage(String welcomeMessage);

  /**
   * Gets this Warp's welcome message with replaced variables. Use {@link #getWelcomeMessage()} to get the raw welcome
   * message.
   *
   * @param forWhom the player for who this welcome message should be parsed
   * @return the welcome message with parsed values
   */
  String getParsedWelcomeMessage(LocalPlayer forWhom);

  /**
   * Sets the location of this Warp. A location consists of three parts: <ol> <li>the world where the location is</li>
   * <li>the position in the world</li> <li>the rotation of the position</li> </ol>
   *
   * @param world    the world
   * @param position the position
   * @param rotation the rotation
   */
  void setLocation(LocalWorld world, Vector3 position, EulerDirection rotation);

  /**
   * Gets the average visits number per day, from the point this Warp was created until now.
   *
   * @return the average number of visits per day
   */
  double getVisitsPerDay();

  /**
   * Gets this Warp's popularity score. The score is influenced by the number of visits a Warp received since it was
   * created, while newer Warps receive a better score than older Warps.
   *
   * @return the popularity score of this Warp
   */
  double getPopularityScore();

  /**
   * The type of a Warp.
   */
  enum Type {
    /**
     * A private Warp.
     */
    PRIVATE('c'),
    /**
     * A public Warp.
     */
    PUBLIC('a');

    /**
     * This types colorCharacter-representation used when displaying Warp-names of this type.
     */
    private final char colorCharacter;

    /**
     * Initializes this type.
     *
     * @param colorCharacter the color character
     */
    Type(char colorCharacter) {
      this.colorCharacter = colorCharacter;
    }

    /**
     * Gets this type's color character.
     *
     * @return the colorCharacter
     */
    public char getColorCharacter() {
      return colorCharacter;
    }

    /**
     * Gets the limit that corresponds with this type.
     *
     * @return the limit
     */
    public Limit.Type getLimit() {
      // putting these information in the respective constructor will not
      // work as the classloader runs into problems when initializing both
      // enums!
      switch (this) {
        case PRIVATE:
          return Limit.Type.PRIVATE;
        case PUBLIC:
          return Limit.Type.PUBLIC;
        default:
          return Limit.Type.TOTAL;

      }
    }
  }

  /**
   * A custom comparator that orders Warps by popularity: popular Warps come first, unpopular last.
   */
  class PopularityComparator implements Comparator<Warp> {

    @Override
    public int compare(Warp w1, Warp w2) {
      // Warps with a higher popularity score are preferred over Warps
      // with lower score. If the score is equal, newer Warps are
      // preferred over older Warps. If both Warps were created at the
      // same millisecond, the alphabetically first is preferred.
      return ComparisonChain.start().compare(w2.getPopularityScore(), w1.getPopularityScore())
          .compare(w2.getCreationDate().getTime(), w1.getCreationDate().getTime()).compare(w1.getName(), w2.getName())
          .result();
    }
  }

}

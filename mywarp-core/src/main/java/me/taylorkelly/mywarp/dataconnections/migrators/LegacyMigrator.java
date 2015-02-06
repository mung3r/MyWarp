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

package me.taylorkelly.mywarp.dataconnections.migrators;

import static org.jooq.impl.DSL.fieldByName;
import static org.jooq.impl.DSL.tableByName;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpBuilder;

import org.jooq.DSLContext;
import org.jooq.Record13;
import org.jooq.Result;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An abstract migrator for legacy (pre 3.0) database layouts. Running the migration will convert player names to UUIDs
 * and world names to world UUIDs.
 */
public abstract class LegacyMigrator {

  private final Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();
  private final MyWarp myWarp;
  private final ImmutableMap<String, UUID> worldsSnapshot;

  /**
   * Creates an instance.
   *
   * @param myWarp         the MyWarp instance
   * @param worldsSnapshot a mapping of world names to uniqueIds
   */
  protected LegacyMigrator(MyWarp myWarp, ImmutableMap<String, UUID> worldsSnapshot) {
    this.myWarp = myWarp;
    this.worldsSnapshot = worldsSnapshot;
  }

  /**
   * Migrates warps from the given DSLContext looking inside the table of the given name.
   *
   * @param create    the DSLContext that provides database access
   * @param tableName the name of the table that contains the data
   * @return a collection of warps migrated from the given data source
   * @throws DataConnectionException if the player UUID conversion fails
   */
  public Collection<Warp> migrateLegacyWarps(DSLContext create, String tableName) throws DataConnectionException {
    Result<Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String,
        String>>
        results =
        create.select(fieldByName(String.class, "name"), fieldByName(String.class, "creator"),
                      fieldByName(Boolean.class, "publicAll"), fieldByName(Double.class, "x"),
                      fieldByName(Double.class, "y"), fieldByName(Double.class, "z"), fieldByName(Float.class, "yaw"),
                      fieldByName(Float.class, "pitch"), fieldByName(String.class, "world"),
                      fieldByName(Integer.class, "visits"), fieldByName(String.class, "welcomeMessage"),
                      fieldByName(String.class, "permissions"), fieldByName(String.class, "groupPermissions"))
            .from(tableByName(tableName)).fetch();

    Set<String> playerNames = new HashSet<String>(results.getValues("creator", String.class)); // NON-NLS
    for (String invitedPlayers : results.getValues("permissions", String.class)) { // NON-NLS
      Iterables.addAll(playerNames, splitter.split(invitedPlayers));
    }

    Map<String, Profile> cache = new HashMap<String, Profile>();
    for (String invitedPlayer : playerNames) {
      Optional<Profile> optionalInvited = myWarp.getProfileService().get(invitedPlayer);
      if (!optionalInvited.isPresent()) {
        // REVIEW log error?
        continue;
      }
      cache.put(invitedPlayer, optionalInvited.get());
    }

    Set<Warp> ret = new HashSet<Warp>(results.size());

    for (Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String,
        String> r : results) {
      Profile creator = cache.get(r.value2());
      if (creator == null) {
        // REVIEW log error?
        continue;
      }

      Warp.Type type = r.value3() ? Warp.Type.PUBLIC : Warp.Type.PRIVATE;

      UUID worldId = worldsSnapshot.get(r.value9());
      if (worldId == null) {
        // REVIEW log error?
        continue;
      }
      Vector3 position = new Vector3(r.value4(), r.value5(), r.value6());
      EulerDirection rotation = new EulerDirection(r.value7(), r.value8(), 0);

      WarpBuilder builder = new WarpBuilder(myWarp, r.value1(), creator, type, worldId, position, rotation);

      // optional values
      builder.withVisits(r.value10());
      builder.withWelcomeMessage(r.value11());

      for (String groupId : splitter.split(r.value13())) {
        builder.addInvitedGroup(groupId);
      }
      for (String playerName : splitter.split(r.value12())) {
        Profile invitee = cache.get(playerName);
        if (invitee == null) {
          // REVIEW log error?
          continue;
        }
        builder.addInvitedPlayer(invitee);
      }

      ret.add(builder.build());
    }
    return ret;
  }

}

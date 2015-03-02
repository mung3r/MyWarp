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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract migrator for legacy (pre 3.0) database layouts. Running the migration will convert player names to UUIDs
 * and world names to world UUIDs.
 */
public abstract class LegacyMigrator {

  private static final Logger log = Logger.getLogger(LegacyMigrator.class.getName());

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
    // @formatter:off
    Result<Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String,
        String>>
        results =
        create.select(fieldByName(String.class, "name"), //1
                      fieldByName(String.class, "creator"), //2
                      fieldByName(Boolean.class, "publicAll"), //3
                      fieldByName(Double.class, "x"), //4
                      fieldByName(Double.class, "y"), //5
                      fieldByName(Double.class, "z"), //6
                      fieldByName(Float.class, "yaw"), //7
                      fieldByName(Float.class, "pitch"), //8
                      fieldByName(String.class, "world"), //9
                      fieldByName(Integer.class, "visits"), //10
                      fieldByName(String.class, "welcomeMessage"), //11
                      fieldByName(String.class, "permissions"), //12
                      fieldByName(String.class, "groupPermissions")) //13
            .from(tableByName(tableName)).fetch();
    // @formatter:on
    log.info(String.format("%d entries found.", results.size()));

    Set<String> playerNames = new HashSet<String>(results.getValues("creator", String.class));
    for (String invitedPlayers : results.getValues("permissions", String.class)) {
      Iterables.addAll(playerNames, splitter.split(invitedPlayers));
    }
    log.info(String.format("Looking up unique IDs for %d unique players.", playerNames.size()));

    List<Profile> profiles = myWarp.getProfileService().getByName(playerNames);
    log.info(String.format("%d unique IDs found.", profiles.size()));

    // the legacy database may contain player-names with a wrong case, so the lookup must be case insensitive
    TreeMap<String, Profile> profileLookup = new TreeMap<String, Profile>(String.CASE_INSENSITIVE_ORDER);
    for (Profile profile : profiles) {
      Optional<String> name = profile.getName();
      if (!name.isPresent()) {
        // this should not happen since all names where cached when requesting the profiles
        continue;
      }
      if (profileLookup.containsKey(name.get())) {
        // this might happen if a that player does not have an unique ID
        continue;
      }
      profileLookup.put(name.get(), profile);
    }

    Set<Warp> ret = new HashSet<Warp>(results.size());

    for (Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String,
        String> r : results) {
      String warpName = r.value1();

      String creatorName = r.value2();
      Profile creator = profileLookup.get(creatorName);
      if (creator == null) {
        log.warning(String.format("For the creator of '%s' (%s) no unique ID could be found. The warp will be ignored.",
                                  warpName, creatorName));
        continue;
      }

      Warp.Type type = r.value3() ? Warp.Type.PUBLIC : Warp.Type.PRIVATE;

      Vector3 position = new Vector3(r.value4(), r.value5(), r.value6());
      EulerDirection rotation = new EulerDirection(r.value8(), r.value7(), 0);

      String worldName = r.value9();
      UUID worldId = worldsSnapshot.get(worldName);
      if (worldId == null) {
        log.warning(String.format("For the world of '%s' (%s) no unique ID could be found. The warp will be ignored.",
                                  warpName, worldName));
        continue;
      }

      WarpBuilder builder = new WarpBuilder(myWarp, warpName, creator, type, worldId, position, rotation);

      // optional values
      builder.withVisits(r.value10());
      builder.withWelcomeMessage(r.value11());

      for (String groupId : splitter.split(r.value13())) {
        builder.addInvitedGroup(groupId);
      }
      for (String playerName : splitter.split(r.value12())) {
        Profile invitee = profileLookup.get(playerName);
        if (invitee == null) {
          log.warning(String.format(
              "%s, who is invited to '%s' does not have a unique ID. The invitation will be ignored.", playerName,
              warpName));
          continue;
        }
        builder.addInvitedPlayer(invitee);
      }

      ret.add(builder.build());
      log.log(Level.FINE, String.format("Warp '%s' exported.", warpName));
    }

    log.info(String.format("%d warps exported from source.", ret.size()));
    return ret;
  }

}

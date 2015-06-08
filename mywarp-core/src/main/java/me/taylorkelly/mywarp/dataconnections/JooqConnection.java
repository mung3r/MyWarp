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

package me.taylorkelly.mywarp.dataconnections;

import static me.taylorkelly.mywarp.dataconnections.generated.Tables.GROUP;
import static me.taylorkelly.mywarp.dataconnections.generated.Tables.PLAYER;
import static me.taylorkelly.mywarp.dataconnections.generated.Tables.WARP;
import static me.taylorkelly.mywarp.dataconnections.generated.Tables.WARP_GROUP_MAP;
import static me.taylorkelly.mywarp.dataconnections.generated.Tables.WARP_PLAYER_MAP;
import static me.taylorkelly.mywarp.dataconnections.generated.Tables.WORLD;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.dataconnections.generated.tables.Player;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.GroupRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.PlayerRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.WarpGroupMapRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.WarpPlayerMapRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.WarpRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.WorldRecord;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.Warp.Type;
import me.taylorkelly.mywarp.warp.WarpBuilder;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record14;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A connection to an SQL database that relies on <a href="http://www.jooq.org/">JOOQ</a>.
 */
class JooqConnection implements DataConnection {

  private static final Logger log = MyWarpLogger.getLogger(JooqConnection.class);

  private final DSLContext create;
  private final MyWarp myWarp;

  /**
   * Creates an instance that uses the given {@code Configuration}.
   *
   * @param myWarp the MyWarp instance
   * @param config the Configuration
   */
  JooqConnection(MyWarp myWarp, Configuration config) {
    this.create = DSL.using(config);
    this.myWarp = myWarp;
  }

  @Override
  public void addWarp(final Warp warp) {
    WarpRecord record = create.newRecord(WARP);
    record.setName(warp.getName());

    PlayerRecord playerRecord = getOrCreatePlayer(warp.getCreator());
    record.setPlayerId(playerRecord.getPlayerId());
    record.setType(warp.getType());

    // position
    Vector3 position = warp.getPosition();
    record.setX(position.getX());
    record.setY(position.getY());
    record.setZ(position.getZ());

    // rotation
    EulerDirection rotation = warp.getRotation();
    record.setPitch(rotation.getPitch());
    record.setYaw(rotation.getYaw());

    // world
    WorldRecord worldRecord = getOrCreateWorld(warp.getWorldIdentifier());
    record.setWorldId(worldRecord.getWorldId());

    record.setCreationDate(warp.getCreationDate());
    record.setVisits(UInteger.valueOf(warp.getVisits()));
    record.setWelcomeMessage(warp.getWelcomeMessage());

    record.store();

    //invitations...

    //...groups
    List<WarpGroupMapRecord> warpGroupMapRecords = new ArrayList<WarpGroupMapRecord>();
    for (String groupName : warp.getInvitedGroups()) {
      GroupRecord groupRecord = getOrCreateGroup(groupName);

      WarpGroupMapRecord warpGroupMapRecord = create.newRecord(WARP_GROUP_MAP);
      warpGroupMapRecord.setWarpId(record.getWarpId());
      warpGroupMapRecord.setGroupId(groupRecord.getGroupId());

      warpGroupMapRecords.add(warpGroupMapRecord);
    }
    create.batchStore(warpGroupMapRecords).execute();

    //...players
    List<WarpPlayerMapRecord> warpPlayerMapRecords = new ArrayList<WarpPlayerMapRecord>();
    for (Profile profile : warp.getInvitedPlayers()) {
      PlayerRecord inivtedPlayerRecord = getOrCreatePlayer(profile);

      WarpPlayerMapRecord warpPlayerMapRecord = create.newRecord(WARP_PLAYER_MAP);
      warpPlayerMapRecord.setWarpId(record.getWarpId());
      warpPlayerMapRecord.setPlayerId(inivtedPlayerRecord.getPlayerId());

      warpPlayerMapRecords.add(warpPlayerMapRecord);
    }

    create.batchStore(warpPlayerMapRecords).execute();
  }

  /**
   * Gets the GroupRecord representing the given group identifier or creates it, if it does not yet exit.
   *
   * @param groupName the group identifier
   * @return the corresponding GroupRecord
   */
  private GroupRecord getOrCreateGroup(String groupName) {
    GroupRecord groupRecord = create.fetchOne(GROUP, GROUP.NAME.eq(groupName));
    if (groupRecord == null) {
      groupRecord = create.newRecord(GROUP);
      groupRecord.setName(groupName);
      groupRecord.store();
    }
    return groupRecord;
  }

  /**
   * Gets the PlayerRecord representing the given Profile or creates it, if it does not yet exit.
   *
   * @param profile the Profile
   * @return the corresponding PlayerRecord
   */
  private PlayerRecord getOrCreatePlayer(Profile profile) {
    PlayerRecord playerRecord = create.fetchOne(PLAYER, PLAYER.UUID.eq(profile.getUniqueId()));
    if (playerRecord == null) {
      playerRecord = create.newRecord(PLAYER);
      playerRecord.setUuid(profile.getUniqueId());
      playerRecord.store();
    }
    return playerRecord;
  }

  /**
   * Gets the WorldRecord representing the world of the given name or creates it, if it does not yet exit.
   *
   * @param worldIdentifier the unique identifier of the world
   * @return the corresponding WorldRecord
   */
  private WorldRecord getOrCreateWorld(UUID worldIdentifier) {
    WorldRecord worldRecord = create.fetchOne(WORLD, WORLD.UUID.eq(worldIdentifier));

    if (worldRecord == null) {
      worldRecord = create.newRecord(WORLD);
      worldRecord.setUuid(worldIdentifier);
      worldRecord.store();
    }
    return worldRecord;
  }

  @Override
  public void removeWarp(final Warp warp) {
    create.delete(WARP).where(WARP.NAME.eq(warp.getName())).execute();
  }

  @Override
  public List<Warp> getWarps() {
    // Alias for the player-table to represent the warp-creator
    Player creatorTable = PLAYER.as("c");

    // query the database and group results by name - each map-entry
    // contains all values for one single warp
    // @formatter:off
    Map<String, Result<Record14<String, UUID, Type, Double, Double, Double, Float, Float, UUID, Date,
        UInteger, String, UUID, String>>> groupedResults = create
            .select(WARP.NAME, creatorTable.UUID, WARP.TYPE, WARP.X, WARP.Y, WARP.Z, WARP.YAW,
                    WARP.PITCH, WORLD.UUID, WARP.CREATION_DATE, WARP.VISITS,
                    WARP.WELCOME_MESSAGE, PLAYER.UUID, GROUP.NAME)
            .from(WARP
                    .join(WORLD)
                        .on(WARP.WORLD_ID.eq(WORLD.WORLD_ID))
                    .join(creatorTable)
                        .on(WARP.PLAYER_ID.eq(creatorTable.PLAYER_ID))
                    .leftOuterJoin(WARP_PLAYER_MAP)
                        .on(WARP_PLAYER_MAP.WARP_ID.eq(WARP.WARP_ID))
                    .leftOuterJoin(PLAYER)
                        .on(WARP_PLAYER_MAP.PLAYER_ID.eq(PLAYER.PLAYER_ID)))
                    .leftOuterJoin(WARP_GROUP_MAP)
                        .on(WARP_GROUP_MAP.WARP_ID.eq(WARP.WARP_ID))
                    .leftOuterJoin(GROUP)
                        .on(WARP_GROUP_MAP.GROUP_ID.eq(GROUP.GROUP_ID))
            .fetch().intoGroups(WARP.NAME);
    // @formatter:on

    // create warp-instances from the results
    List<Warp> ret = new ArrayList<Warp>(groupedResults.size());
    for (Result<Record14<String, UUID, Type, Double, Double, Double, Float, Float, UUID, Date, UInteger, String,
        UUID, String>> r : groupedResults
        .values()) {
      Profile creator = myWarp.getProfileService().getByUniqueId(r.getValue(0, creatorTable.UUID));

      Vector3 position = new Vector3(r.getValue(0, WARP.X), r.getValue(0, WARP.Y), r.getValue(0, WARP.Z));
      EulerDirection rotation = new EulerDirection(r.getValue(0, WARP.PITCH), r.getValue(0, WARP.YAW), 0);

      WarpBuilder
          builder =
          new WarpBuilder(myWarp, r.getValue(0, WARP.NAME), creator, r.getValue(0, WORLD.UUID), position, rotation);

      // optional values
      builder.setType(r.getValue(0, WARP.TYPE));
      builder.setCreationDate(r.getValue(0, WARP.CREATION_DATE));
      builder.setVisits(r.getValue(0, WARP.VISITS).intValue());
      builder.setWelcomeMessage(r.getValue(0, WARP.WELCOME_MESSAGE));

      for (String groupName : r.getValues(GROUP.NAME)) {
        if (groupName != null) {
          builder.addInvitedGroup(groupName);
        }
      }

      for (UUID inviteeUniqueId : r.getValues(PLAYER.UUID)) {
        if (inviteeUniqueId != null) {
          Profile inviteeProfile = myWarp.getProfileService().getByUniqueId(inviteeUniqueId);
          builder.addInvitedPlayer(inviteeProfile);
        }
      }

      ret.add(builder.build());
    }

    return ret;
  }

  @Override
  public void inviteGroup(final Warp warp, final String groupId) {
    WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
    GroupRecord groupRecord = getOrCreateGroup(groupId);

    create.insertInto(WARP_GROUP_MAP).set(WARP_GROUP_MAP.WARP_ID, warpRecord.getWarpId())
        .set(WARP_GROUP_MAP.GROUP_ID, groupRecord.getGroupId()).execute();
  }

  @Override
  public void invitePlayer(final Warp warp, final Profile profile) {
    WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
    PlayerRecord playerRecord = getOrCreatePlayer(profile);

    create.insertInto(WARP_PLAYER_MAP).set(WARP_PLAYER_MAP.WARP_ID, warpRecord.getWarpId())
        .set(WARP_PLAYER_MAP.PLAYER_ID, playerRecord.getPlayerId()).execute();
  }

  @Override
  public void uninviteGroup(final Warp warp, final String groupId) {
    WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
    GroupRecord groupRecord = getOrCreateGroup(groupId);

    create.delete(WARP_GROUP_MAP).where(
        WARP_GROUP_MAP.WARP_ID.eq(warpRecord.getWarpId()).and(WARP_GROUP_MAP.GROUP_ID.eq(groupRecord.getGroupId())))
        .execute();
  }

  @Override
  public void uninvitePlayer(final Warp warp, final Profile profile) {
    WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
    PlayerRecord playerRecord = getOrCreatePlayer(profile);

    create.delete(WARP_PLAYER_MAP).where(WARP_PLAYER_MAP.WARP_ID.eq(warpRecord.getWarpId())
                                             .and(WARP_PLAYER_MAP.PLAYER_ID.eq(playerRecord.getPlayerId()))).execute();
  }

  @Override
  public void updateCreator(final Warp warp) {
    WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

    PlayerRecord playerRecord = getOrCreatePlayer(warp.getCreator());
    record.setPlayerId(playerRecord.getPlayerId());

    record.update();
  }

  @Override
  public void updateLocation(final Warp warp) {
    WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

    // position
    Vector3 position = warp.getPosition();
    record.setX(position.getX());
    record.setY(position.getY());
    record.setZ(position.getZ());

    // rotation
    EulerDirection rotation = warp.getRotation();
    record.setPitch(rotation.getPitch());
    record.setYaw(rotation.getYaw());

    // world
    WorldRecord worldRecord = getOrCreateWorld(warp.getWorldIdentifier());
    record.setWorldId(worldRecord.getWorldId());

    record.update();
  }

  @Override
  public void updateType(final Warp warp) {
    WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

    record.setType(warp.getType());
    record.update();
  }

  @Override
  public void updateVisits(final Warp warp) {
    WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

    record.setVisits(UInteger.valueOf(warp.getVisits()));
    record.update();
  }

  @Override
  public void updateWelcomeMessage(final Warp warp) {
    WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

    record.setWelcomeMessage(warp.getWelcomeMessage());
    record.update();
  }

}

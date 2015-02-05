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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.dataconnections.generated.tables.Player;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.GroupRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.PlayerRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.WarpRecord;
import me.taylorkelly.mywarp.dataconnections.generated.tables.records.WorldRecord;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.Warp.Type;
import me.taylorkelly.mywarp.warp.WarpBuilder;

import org.jooq.DSLContext;
import org.jooq.Record14;
import org.jooq.Result;
import org.jooq.types.UInteger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The connection to a SQL database via JOOQ.
 */
public class JooqConnection implements DataConnection {

  private static final Logger log = Logger.getLogger(JooqConnection.class.getName());

  private final Connection conn;
  private final DSLContext create;
  private final ListeningExecutorService executor;
  private final MyWarp myWarp;

  /**
   * Creates this JooqConnection using the given DSLContext with the given connection, using the given executor to run
   * all tasks.
   *
   * @param myWarp   the MyWarp instance
   * @param conn     the Connection to use
   * @param executor the executor that runs all tasks
   * @param create   the DSLContext to use
   */
  protected JooqConnection(MyWarp myWarp, Connection conn, ListeningExecutorService executor, DSLContext create) {
    this.create = create;
    this.conn = conn;
    this.executor = executor;
    this.myWarp = myWarp;
  }

  @Override
  public void close() {
    executor.shutdown();

    try {
      if (conn != null && !conn.isClosed()) {
        conn.close();
      }
    } catch (SQLException e) {
      log.log(Level.WARNING, "Failed to close SQL connection.", e); // NON-NLS
    }

  }

  @Override
  public void addWarp(final Warp warp) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
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
      }

    });

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
      playerRecord.insert();
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
    executor.submit(new Runnable() {

      @Override
      public void run() {
        create.delete(WARP).where(WARP.NAME.eq(warp.getName())).execute();
      }

    });
  }

  @Override
  public ListenableFuture<Collection<Warp>> getWarps() {
    return executor.submit(new Callable<Collection<Warp>>() {


      @Override
      public Collection<Warp> call() {
        // Alias for the player-table to represent the warp-creator
        Player creatorTable = PLAYER.as("c"); //NON-NLS

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
        Collection<Warp> ret = new ArrayList<Warp>(groupedResults.size());
        for (Result<Record14<String, UUID, Type, Double, Double, Double, Float, Float, UUID, Date, UInteger, String,
            UUID, String>> r : groupedResults
            .values()) {
          Profile creator = myWarp.getProfileService().get(r.getValue(0, creatorTable.UUID));

          Vector3 position = new Vector3(r.getValue(0, WARP.X), r.getValue(0, WARP.Y), r.getValue(0, WARP.Z));
          EulerDirection rotation = new EulerDirection(r.getValue(0, WARP.YAW), r.getValue(0, WARP.PITCH), 0);

          WarpBuilder
              builder =
              new WarpBuilder(myWarp, r.getValue(0, WARP.NAME), creator, r.getValue(0, WARP.TYPE),
                              r.getValue(0, WORLD.UUID), position, rotation);

          // optional values
          builder.withCreationDate(r.getValue(0, WARP.CREATION_DATE));
          builder.withVisits(r.getValue(0, WARP.VISITS).intValue());
          builder.withWelcomeMessage(r.getValue(0, WARP.WELCOME_MESSAGE));

          for (String groupName : r.getValues(GROUP.NAME)) {
            if (groupName != null) {
              builder.addInvitedGroup(groupName);
            }
          }

          for (UUID inviteeUniqueId : r.getValues(PLAYER.UUID)) {
            if (inviteeUniqueId != null) {
              Profile inviteeProfile = myWarp.getProfileService().get(inviteeUniqueId);
              builder.addInvitedPlayer(inviteeProfile);
            }
          }

          ret.add(builder.build());
        }

        return ret;
      }

    });

  }

  @Override
  public void inviteGroup(final Warp warp, final String groupId) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
        GroupRecord groupRecord = getOrCreateGroup(groupId);

        create.insertInto(WARP_GROUP_MAP).set(WARP_GROUP_MAP.WARP_ID, warpRecord.getWarpId())
            .set(WARP_GROUP_MAP.GROUP_ID, groupRecord.getGroupId()).execute();
      }

    });

  }

  @Override
  public void invitePlayer(final Warp warp, final Profile profile) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
        PlayerRecord playerRecord = getOrCreatePlayer(profile);

        create.insertInto(WARP_PLAYER_MAP).set(WARP_PLAYER_MAP.WARP_ID, warpRecord.getWarpId())
            .set(WARP_PLAYER_MAP.PLAYER_ID, playerRecord.getPlayerId()).execute();
      }

    });

  }

  @Override
  public void uninviteGroup(final Warp warp, final String groupId) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
        GroupRecord groupRecord = getOrCreateGroup(groupId);

        create.delete(WARP_GROUP_MAP).where(
            WARP_GROUP_MAP.WARP_ID.eq(warpRecord.getWarpId()).and(WARP_GROUP_MAP.GROUP_ID.eq(groupRecord.getGroupId())))
            .execute();
      }

    });

  }

  @Override
  public void uninvitePlayer(final Warp warp, final Profile profile) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
        PlayerRecord playerRecord = getOrCreatePlayer(profile);

        create.delete(WARP_PLAYER_MAP).where(WARP_PLAYER_MAP.WARP_ID.eq(warpRecord.getWarpId())
                                                 .and(WARP_PLAYER_MAP.PLAYER_ID.eq(playerRecord.getPlayerId())))
            .execute();
      }

    });

  }

  @Override
  public void updateCreator(final Warp warp) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

        PlayerRecord playerRecord = getOrCreatePlayer(warp.getCreator());
        record.setPlayerId(playerRecord.getPlayerId());

        record.update();
      }

    });

  }

  @Override
  public void updateLocation(final Warp warp) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
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

    });

  }

  @Override
  public void updateType(final Warp warp) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

        record.setType(warp.getType());
        record.update();
      }

    });

  }

  @Override
  public void updateVisits(final Warp warp) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

        record.setVisits(UInteger.valueOf(warp.getVisits()));
        record.update();
      }

    });

  }

  @Override
  public void updateWelcomeMessage(final Warp warp) {
    executor.submit(new Runnable() {

      @Override
      public void run() {
        WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

        record.setWelcomeMessage(warp.getWelcomeMessage());
        record.update();
      }

    });

  }

}

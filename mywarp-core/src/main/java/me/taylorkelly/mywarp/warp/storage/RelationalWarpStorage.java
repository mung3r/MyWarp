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

package me.taylorkelly.mywarp.warp.storage;

import static me.taylorkelly.mywarp.warp.storage.generated.Tables.GROUP;
import static me.taylorkelly.mywarp.warp.storage.generated.Tables.PLAYER;
import static me.taylorkelly.mywarp.warp.storage.generated.Tables.WARP;
import static me.taylorkelly.mywarp.warp.storage.generated.Tables.WARP_GROUP_MAP;
import static me.taylorkelly.mywarp.warp.storage.generated.Tables.WARP_PLAYER_MAP;
import static me.taylorkelly.mywarp.warp.storage.generated.Tables.WORLD;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.val;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3d;

import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.Warp.Type;
import me.taylorkelly.mywarp.warp.WarpBuilder;
import me.taylorkelly.mywarp.warp.storage.generated.tables.Player;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.InsertSetMoreStep;
import org.jooq.Record;
import org.jooq.Record14;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TransactionalRunnable;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * A storage implementation that stores warps in a relational database.
 *
 * <p>This implementation is guaranteed to work with SQLite, MySQL, MariaDB and H2, but might also work on other
 * relational database management systems.</p>
 */
@SuppressWarnings("checkstyle:indentation")
class RelationalWarpStorage implements WarpStorage {

  private final Configuration configuration;

  /**
   * Creates an instance that uses the given {@code Configuration}.
   *
   * @param configuration the Configuration
   */
  RelationalWarpStorage(Configuration configuration) {
    this.configuration = configuration;
  }

  private DSLContext create(Configuration configuration) {
    return DSL.using(configuration);
  }

  @Override
  public void addWarp(final Warp warp) {
    final Vector3d position = warp.getPosition();
    final Vector2f rotation = warp.getRotation();
    final List<UUID> playerIds = new ArrayList<UUID>();
    playerIds.add(warp.getCreator());
    playerIds.addAll(warp.getInvitedPlayers());

    // @formatter:off
    create(configuration).transaction(new TransactionalRunnable() {
      @Override
      public void run(Configuration configuration) throws Exception {

        //Insert all players
        List<Insert<Record>> playerInserts = new ArrayList<Insert<Record>>();
        for (UUID playerId : playerIds) {
          playerInserts.add(insertOrIgnore(configuration, PLAYER, PLAYER.UUID, playerId));
        }
        create(configuration).batch(playerInserts).execute();

        //Insert the world
        insertOrIgnore(configuration, WORLD, WORLD.UUID, warp.getWorldIdentifier()).execute();

        //Insert the warp
        create(configuration)
            .insertInto(WARP)
            .set(WARP.NAME, warp.getName())
            .set(WARP.PLAYER_ID,
                 select(PLAYER.PLAYER_ID)
                 .from(PLAYER)
                 .where(PLAYER.UUID.eq(warp.getCreator()))
                 .limit(1)
            )
            .set(WARP.TYPE, warp.getType())
            .set(WARP.X, position.getX())
            .set(WARP.Y, position.getY())
            .set(WARP.Z, position.getZ())
            .set(WARP.PITCH, rotation.getX())
            .set(WARP.YAW, rotation.getY())
            .set(WARP.WORLD_ID,
                 select(WORLD.WORLD_ID)
                 .from(WORLD)
                 .where(WORLD.UUID.eq(warp.getWorldIdentifier()))
                 .limit(1))
            .set(WARP.CREATION_DATE, warp.getCreationDate())
            .set(WARP.VISITS, UInteger.valueOf(warp.getVisits()))
            .set(WARP.WELCOME_MESSAGE, warp.getWelcomeMessage())
        .execute();

        //Insert all groups
        List<Insert<Record>> groupInserts = new ArrayList<Insert<Record>>();
        for (String groupName : warp.getInvitedGroups()) {
          groupInserts.add(insertOrIgnore(configuration, GROUP, GROUP.NAME, groupName));
        }
        create(configuration).batch(groupInserts).execute();

        //insert all player-invitations
        List<InsertSetMoreStep<Record>> warpPlayerInserts = new
            ArrayList<InsertSetMoreStep<Record>>();
        for (UUID invited : warp.getInvitedPlayers()) {
          warpPlayerInserts.add(create(configuration)
            .insertInto(WARP_PLAYER_MAP)
            .set(WARP_PLAYER_MAP.WARP_ID,
                 select(WARP.WARP_ID)
                  .from(WARP)
                  .where(WARP.NAME.eq(warp.getName()))
                  .limit(1)
            )
            .set(WARP_PLAYER_MAP.PLAYER_ID,
                 select(PLAYER.PLAYER_ID)
                 .from(PLAYER)
                 .where(PLAYER.UUID.eq(invited))
                 .limit(1)
            )
          );
        }
        create(configuration).batch(warpPlayerInserts).execute();

        //insert all group-invitations
        List<InsertSetMoreStep<Record>> warpGroupInserts = new
            ArrayList<InsertSetMoreStep<Record>>();
        for (String groupName : warp.getInvitedGroups()) {
          warpGroupInserts.add(create(configuration)
            .insertInto(WARP_GROUP_MAP)
            .set(WARP_GROUP_MAP.WARP_ID,
                 select(WARP.WARP_ID)
                  .from(WARP)
                  .where(WARP.NAME.eq(warp.getName()))
                  .limit(1)
            )
            .set(WARP_GROUP_MAP.GROUP_ID,
                 select(GROUP.GROUP_ID)
                 .from(GROUP)
                 .where(GROUP.NAME.eq(groupName))
                 .limit(1)
            )
          );
        }
        create(configuration).batch(warpGroupInserts).execute();
      }
    });
    // @formatter:on
  }

  @Override
  public void removeWarp(final Warp warp) {
    // @formatter:off
    create(configuration)
        .delete(WARP)
        .where(WARP.NAME.eq(warp.getName()))
    .execute();
    // @formatter:on
  }

  @Override
  public List<Warp> getWarps() {
    // Alias for the player-table to represent the warp-creator
    Player creatorTable = PLAYER.as("c");

    // query the database and group results by name - each map-entry
    // contains all values for one single warp
    // @formatter:off
    Map<String, Result<Record14<String, UUID, Type, Double, Double, Double, Float, Float, UUID, Date,
        UInteger, String, UUID, String>>> groupedResults = create(configuration)
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

      Vector3d position = new Vector3d(r.getValue(0, WARP.X), r.getValue(0, WARP.Y), r.getValue(0, WARP.Z));
      Vector2f rotation = new Vector2f(r.getValue(0, WARP.PITCH), r.getValue(0, WARP.YAW));

      WarpBuilder
              builder =
              new WarpBuilder(r.getValue(0, WARP.NAME), r.getValue(0, creatorTable.UUID), r.getValue(0, WORLD.UUID),
                      position, rotation);

      // optional values
      builder.setType(r.getValue(0, WARP.TYPE));
      builder.setCreationDate(r.getValue(0, WARP.CREATION_DATE));
      builder.setVisits(r.getValue(0, WARP.VISITS).intValue());
      builder.setWelcomeMessage(r.getValue(0, WARP.WELCOME_MESSAGE));

      for (@Nullable String groupName : r.getValues(GROUP.NAME)) {
        if (groupName != null) {
          builder.addInvitedGroup(groupName);
        }
      }

      for (@Nullable UUID inviteeUniqueId : r.getValues(PLAYER.UUID)) {
        if (inviteeUniqueId != null) {
          builder.addInvitedPlayer(inviteeUniqueId);
        }
      }

      ret.add(builder.build());
    }

    return ret;
  }

  @Override
  public void inviteGroup(final Warp warp, final String groupId) {
    create(configuration).transaction(new TransactionalRunnable() {
      @Override
      public void run(Configuration configuration) throws Exception {
        // @formatter:off
        insertOrIgnore(configuration, GROUP, GROUP.NAME, groupId).execute();

        create(configuration)
            .insertInto(WARP_GROUP_MAP)
            .set(WARP_GROUP_MAP.WARP_ID,
                 select(WARP.WARP_ID)
                  .from(WARP)
                  .where(WARP.NAME.eq(warp.getName()))
                  .limit(1)
            )
            .set(WARP_GROUP_MAP.GROUP_ID,
                 select(GROUP.GROUP_ID)
                 .from(GROUP)
                 .where(GROUP.NAME.eq(groupId))
                 .limit(1)
            )
        .execute();
        // @formatter:on
      }
    });
  }

  @Override
  public void invitePlayer(final Warp warp, final UUID uniqueId) {
    create(configuration).transaction(new TransactionalRunnable() {
      @Override
      public void run(Configuration configuration) throws Exception {
        // @formatter:off
        insertOrIgnore(configuration, PLAYER, PLAYER.UUID, uniqueId).execute();

        create(configuration)
            .insertInto(WARP_PLAYER_MAP)
            .set(WARP_PLAYER_MAP.WARP_ID,
                 select(WARP.WARP_ID)
                  .from(WARP)
                  .where(WARP.NAME.eq(warp.getName()))
                  .limit(1)
            )
            .set(WARP_PLAYER_MAP.PLAYER_ID,
                 select(PLAYER.PLAYER_ID)
                 .from(PLAYER)
                 .where(PLAYER.UUID.eq(uniqueId))
                 .limit(1)
            )
        .execute();
        // @formatter:on
      }
    });
  }

  @Override
  public void uninviteGroup(final Warp warp, final String groupId) {
    // @formatter:off
    create(configuration)
        .delete(WARP_GROUP_MAP)
        .where(
            WARP_GROUP_MAP.WARP_ID.eq(
              select(WARP.WARP_ID)
              .from(WARP)
              .where(WARP.NAME.eq(warp.getName()))
              .limit(1))
            .and(WARP_GROUP_MAP.GROUP_ID.eq(
              select(GROUP.GROUP_ID)
              .from(GROUP)
              .where(GROUP.NAME.eq(groupId))
              .limit(1))
            )
        )
    .execute();
    // @formatter:on
  }

  @Override
  public void uninvitePlayer(final Warp warp, final UUID uniqueId) {
    // @formatter:off
    create(configuration)
        .delete(WARP_PLAYER_MAP)
        .where(
            WARP_PLAYER_MAP.WARP_ID.eq(
              select(WARP.WARP_ID)
              .from(WARP)
              .where(WARP.NAME.eq(warp.getName()))
              .limit(1))
            .and(WARP_PLAYER_MAP.PLAYER_ID.eq(
              select(PLAYER.PLAYER_ID)
              .from(PLAYER)
              .where(PLAYER.UUID.eq(uniqueId))
              .limit(1))
            )
        )
    .execute();
    // @formatter:on
  }

  @Override
  public void updateCreator(final Warp warp) {
    create(configuration).transaction(new TransactionalRunnable() {
      @Override
      public void run(Configuration configuration) throws Exception {
        // @formatter:off
        insertOrIgnore(configuration, PLAYER, PLAYER.UUID, warp.getCreator()).execute();

        create(configuration)
            .update(WARP)
            .set(WARP.PLAYER_ID,
                select(PLAYER.PLAYER_ID)
                .from(PLAYER)
                .where(PLAYER.UUID.eq(warp.getCreator()))
                .limit(1)
            )
            .where(WARP.NAME.eq(warp.getName()))
        .execute();
        // @formatter:on
      }
    });
  }

  @Override
  public void updateLocation(final Warp warp) {
    final Vector3d position = warp.getPosition();
    final Vector2f rotation = warp.getRotation();

    create(configuration).transaction(new TransactionalRunnable() {
      @Override
      public void run(Configuration configuration) throws Exception {
        // @formatter:off
        insertOrIgnore(configuration, WORLD, WORLD.UUID, warp.getWorldIdentifier()).execute();

        create(configuration)
            .update(WARP)
            .set(WARP.X, position.getX())
            .set(WARP.Y, position.getY())
            .set(WARP.Z, position.getZ())
            .set(WARP.PITCH, rotation.getX())
            .set(WARP.YAW, rotation.getY())
            .set(WARP.WORLD_ID,
                 select(WORLD.WORLD_ID)
                 .from(WORLD)
                 .where(WORLD.UUID.eq(warp.getWorldIdentifier()))
                 .limit(1))
            .where(WARP.NAME.eq(warp.getName()))
        .execute();
        // @formatter:on
      }
    });
  }

  @Override
  public void updateType(final Warp warp) {
    // @formatter:off
    create(configuration)
        .update(WARP)
        .set(WARP.TYPE, warp.getType())
        .where(WARP.NAME.eq(warp.getName()))
    .execute();
    // @formatter:on
  }

  @Override
  public void updateVisits(final Warp warp) {
    // @formatter:off
    create(configuration)
        .update(WARP)
        .set(WARP.VISITS, UInteger.valueOf(warp.getVisits()))
        .where(WARP.NAME.eq(warp.getName()))
    .execute();
    // @formatter:on
  }

  @Override
  public void updateWelcomeMessage(final Warp warp) {
    // @formatter:off
    create(configuration)
        .update(WARP)
        .set(WARP.WELCOME_MESSAGE, warp.getWelcomeMessage())
        .where(WARP.NAME.eq(warp.getName()))
    .execute();
    // @formatter:on
  }

  /**
   * Creates an {@code INSERT ... ON DUPLICATE IGNORE} query that insert the given {@code value} into the given {@code
   * uniqueField} in the given {@code table}, assuming that the given {@code value} should be unique.
   *
   * <p>JOOQ's native {@link org.jooq.InsertQuery#onDuplicateKeyIgnore(boolean)} implementation only supports CUBRID,
   * HSQLDB, MariaDB and MySQL in JOOQ 3.6 - full support is added in 3.7.</p>
   *
   * <p>To be compatible with all supported databases, this implementation emulates the query as follows:
   * <code><pre>INSERT INTO [dst] ( ... )
   * SELECT [values]
   * WHERE NOT EXISTS (
   *   SELECT 1
   *   FROM [dst]
   *   WHERE [dst.key] = [values.key]
   * )</pre></code></p>
   *
   * @param configuration the {@code Configuration} used to generate the query
   * @param table         the {@code Table} to insert in
   * @param uniqueField   the {@code TableField}  to insert - must be unique!
   * @param value         the value to insert
   * @return a corresponding {@code Insert} query
   */
  private <R extends Record, T> Insert<R> insertOrIgnore(Configuration configuration, Table<R> table,
                                                         TableField<R, T> uniqueField, T value) {
    // @formatter:off
    //XXX use onDuplicateKeyIgnore() in JOOQ 3.7
    return create(configuration)
        .insertInto(table)
        .columns(uniqueField)
        .select(
          select(val(value, uniqueField))
          .whereNotExists(
              selectOne()
              .from(table)
              .where(uniqueField.eq(value))
          )
        );
    // @formatter:on
  }
}

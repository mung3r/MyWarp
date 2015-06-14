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

package me.taylorkelly.mywarp.storage.source;

import static com.google.common.base.Preconditions.checkArgument;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.util.EulerDirection;
import me.taylorkelly.mywarp.util.MyWarpLogger;
import me.taylorkelly.mywarp.util.Vector3;
import me.taylorkelly.mywarp.util.profile.Profile;
import me.taylorkelly.mywarp.warp.Warp;
import me.taylorkelly.mywarp.warp.WarpBuilder;

import org.jooq.Configuration;
import org.jooq.Record13;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.jdbc.JDBCUtils;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.sql.DataSource;

/**
 * A {@link WarpSource} for databases with a legacy scheme (pre 3.0).
 * <p>The legacy database stores player and world names as strings, instead of using unique IDs. Calling {@link
 * #getWarps()} will convert both. Player names are accuired by calling the configured {@link
 * me.taylorkelly.mywarp.util.profile.ProfileService}, witch may result in a blocking call.</p>
 */
public class LegacyWarpSource implements WarpSource {

  private static final Logger log = MyWarpLogger.getLogger(LegacyWarpSource.class);

  private final Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();
  private final MyWarp myWarp;
  private final ImmutableMap<String, UUID> worldsSnapshot;
  private final Configuration configuration;
  private final String tableName;

  /**
   * Creates an instance.
   *
   * @param myWarp         the MyWarp instance
   * @param worldsSnapshot a mapping of world names to uniqueIds
   * @throws SQLException on a database error
   */
  public LegacyWarpSource(DataSource dataSource, String tableName, MyWarp myWarp, Map<String, UUID> worldsSnapshot)
      throws SQLException {
    SQLDialect dialect = JDBCUtils.dialect(dataSource.getConnection());
    checkArgument(dialect.equals(SQLDialect.MYSQL) || dialect.equals(SQLDialect.SQLITE));

    this.myWarp = myWarp;
    this.worldsSnapshot = ImmutableMap.copyOf(worldsSnapshot);

    this.configuration =
        new DefaultConfiguration().set(dialect).set(new Settings().withRenderNameStyle(RenderNameStyle.QUOTED))
            .set(dataSource);
    this.tableName = tableName;
  }

  @Override
  public List<Warp> getWarps() {

    // @formatter:off
    Result<Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String,
        String>>
        results =
        DSL.using(configuration).select(field(name("name"), String.class), //1
                      field(name("creator"), String.class), //2
                      field(name("publicAll"), Boolean.class), //3
                      field(name("x"), Double.class), //4
                      field(name("y"), Double.class), //5
                      field(name("z"), Double.class), //6
                      field(name("yaw"), Float.class), //7
                      field(name("pitch"), Float.class), //8
                      field(name("world"), String.class), //9
                      field(name("visits"), Integer.class), //10
                      field(name("welcomeMessage"), String.class), //11
                      field(name("permissions"), String.class), //12
                      field(name("groupPermissions"), String.class)) //13
            .from(table(tableName)).fetch();
    // @formatter:on
    log.info("{} entries found.", results.size());

    Set<String> playerNames = new HashSet<String>(results.getValues("creator", String.class));
    for (String invitedPlayers : results.getValues("permissions", String.class)) {
      Iterables.addAll(playerNames, splitter.split(invitedPlayers));
    }
    log.info("Looking up unique IDs for {} unique players.", playerNames.size());

    List<Profile> profiles = myWarp.getProfileService().getByName(playerNames);
    log.info("{} unique IDs found.", profiles.size());

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

    List<Warp> ret = new ArrayList<Warp>(results.size());

    for (Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String,
        String> r : results) {
      String warpName = r.value1();

      String creatorName = r.value2();
      Profile creator = profileLookup.get(creatorName);
      if (creator == null) {
        log.warn("For the creator of '{}' ({}) no unique ID could be found. The warp will be ignored.", warpName,
                 creatorName);
        continue;
      }

      Warp.Type type = r.value3() ? Warp.Type.PUBLIC : Warp.Type.PRIVATE;

      Vector3 position = new Vector3(r.value4(), r.value5(), r.value6());
      EulerDirection rotation = new EulerDirection(r.value8(), r.value7(), 0);

      String worldName = r.value9();
      UUID worldId = worldsSnapshot.get(worldName);
      if (worldId == null) {
        log.warn("For the world of '{}' ({}) no unique ID could be found. The warp will be ignored.", warpName,
                 worldName);
        continue;
      }

      WarpBuilder builder = new WarpBuilder(myWarp, warpName, creator, worldId, position, rotation);

      // optional values
      builder.setType(type);
      builder.setVisits(r.value10());
      builder.setWelcomeMessage(r.value11());

      builder.addInvitedGroups(splitter.split(r.value13()));

      for (String playerName : splitter.split(r.value12())) {
        Profile invitee = profileLookup.get(playerName);
        if (invitee == null) {
          log.warn("{}, who is invited to '{}' does not have a unique ID. The invitation will be ignored.", playerName,
                   warpName);
          continue;
        }
        builder.addInvitedPlayer(invitee);
      }

      ret.add(builder.build());
      log.debug("Warp '{}' exported.", warpName);
    }

    log.info("{} warps exported from source.", ret.size());
    return ret;
  }
}

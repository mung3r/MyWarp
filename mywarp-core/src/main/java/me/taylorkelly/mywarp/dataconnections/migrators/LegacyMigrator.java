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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.taylorkelly.mywarp.LocalWorld;
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

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * An abstract migrator for legacy (pre 3.0) database layouts.
 */
public abstract class LegacyMigrator {

    private final Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();

    /**
     * Migrates warps from the given DSLContext looking inside the table of the
     * given name.
     * 
     * @param create
     *            the DSLContext that provides database access
     * @param tableName
     *            the name of the table that contains the data
     * @return a collection of warps migrated from the given data source
     * @throws DataConnectionException
     *             if the player UUID conversion fails
     */
    public Collection<Warp> migrateLegacyWarps(DSLContext create, String tableName)
            throws DataConnectionException {
        Result<Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String, String>> results = create
                .select(fieldByName(String.class, "name"), fieldByName(String.class, "creator"),
                // NON-NLS NON-NLS
                        fieldByName(Boolean.class, "publicAll"), fieldByName(Double.class, "x"),
                        // NON-NLS NON-NLS
                        fieldByName(Double.class, "y"), fieldByName(Double.class, "z"), // NON-NLS
                                                                                        // NON-NLS
                        fieldByName(Float.class, "yaw"), fieldByName(Float.class, "pitch"), // NON-NLS
                                                                                            // NON-NLS
                        fieldByName(String.class, "world"), fieldByName(Integer.class, "visits"),
                        // NON-NLS NON-NLS
                        fieldByName(String.class, "welcomeMessage"), // NON-NLS
                        fieldByName(String.class, "permissions"), // NON-NLS
                        fieldByName(String.class, "groupPermissions")).from(tableByName(tableName)).fetch(); // NON-NLS

        Set<String> playerNames = new HashSet<String>(results.getValues("creator", String.class)); // NON-NLS
        for (String invitedPlayers : results.getValues("permissions", String.class)) { // NON-NLS
            Iterables.addAll(playerNames, splitter.split(invitedPlayers));
        }

        Map<String, Profile> cache = new HashMap<String, Profile>();
        for (String invitedPlayer : playerNames) {
            Optional<Profile> optionalInvited = MyWarp.getInstance().getProfileService().get(invitedPlayer);
            if (!optionalInvited.isPresent()) {
                // REVIEW log error?
                continue;
            }
            cache.put(invitedPlayer, optionalInvited.get());
        }

        Set<Warp> ret = new HashSet<Warp>(results.size());

        for (Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String, String> r : results) {
            Warp.Type type = r.value3() ? Warp.Type.PUBLIC : Warp.Type.PRIVATE;

            Optional<LocalWorld> optionalWorld = MyWarp.getInstance().getLoadedWorld(r.value9());
            if (!optionalWorld.isPresent()) {
                // REVIEW log error?
                continue;
            }
            LocalWorld world = optionalWorld.get();
            Vector3 position = new Vector3(r.value4(), r.value5(), r.value6());
            EulerDirection rotation = new EulerDirection(r.value7(), r.value8(), 0);

            WarpBuilder builder = new WarpBuilder(r.value1(), cache.get(r.value2()), type, world, position,
                    rotation);

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
                builder.addInvitedPlayer(cache.get(playerName));
            }

            ret.add(builder.build());
        }
        return ret;
    }

}

/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;

import org.jooq.DSLContext;
import org.jooq.Record13;
import org.jooq.Result;

import static org.jooq.impl.DSL.*;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * An abstract migrator for legacy (pre 2.7) database layouts.
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
                        fieldByName(Boolean.class, "publicAll"), fieldByName(Double.class, "x"),
                        fieldByName(Double.class, "y"), fieldByName(Double.class, "z"),
                        fieldByName(Float.class, "yaw"), fieldByName(Float.class, "pitch"),
                        fieldByName(String.class, "world"), fieldByName(Integer.class, "visits"),
                        fieldByName(String.class, "welcomeMessage"),
                        fieldByName(String.class, "permissions"),
                        fieldByName(String.class, "groupPermissions")).from(tableByName(tableName)).fetch();

        // TODO don't split this string twice
        Set<String> playerNames = new HashSet<String>(results.getValues("creator", String.class));
        for (String invitedPlayers : results.getValues("permissions", String.class)) {
            Iterables.addAll(playerNames, splitter.split(invitedPlayers));
        }

        UUIDFetcher fetcher = new UUIDFetcher(playerNames);
        Map<String, UUID> lookup = null;
        try {
            lookup = fetcher.call();
        } catch (Exception e) {
            throw new DataConnectionException(
                    "Failed to connect to Mojang's servers for name to UUID conversation.", e);
        }

        Set<Warp> ret = new HashSet<Warp>(results.size());

        for (Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String, String> r : results) {
            // TODO add validation for missing values
            UUID worldId = MyWarp.server().getWorld(r.value9()).getUID();
            Warp.Type type = r.value3() ? Warp.Type.PUBLIC : Warp.Type.PRIVATE;
            Set<String> invitedGroups = Sets.newHashSet(splitter.split(r.value13()));

            Set<UUID> invitedPlayerIds = new HashSet<UUID>();
            for (String invitedPlayer : splitter.split(r.value12())) {
                invitedPlayerIds.add(lookup.get(invitedPlayer));
            }
            Warp w = new Warp(r.value1(), lookup.get(r.value2()), type, r.value4(), r.value5(), r.value6(),
                    r.value7(), r.value8(), worldId, new Date(), r.value10(), r.value11(), invitedPlayerIds,
                    invitedGroups);
            ret.add(w);
        }
        return ret;
    }

}

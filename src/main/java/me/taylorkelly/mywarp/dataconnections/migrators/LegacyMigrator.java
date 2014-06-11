package me.taylorkelly.mywarp.dataconnections.migrators;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.DataConnectionException;

import org.jooq.DSLContext;
import org.jooq.Record13;
import org.jooq.Result;
import org.jooq.impl.DSL;

import com.google.common.base.Splitter;
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
                .select(DSL.fieldByName(String.class, "name"), DSL.fieldByName(String.class, "creator"),
                        DSL.fieldByName(Boolean.class, "publicAll"),

                        DSL.fieldByName(Double.class, "x"), DSL.fieldByName(Double.class, "y"),
                        DSL.fieldByName(Double.class, "z"),

                        DSL.fieldByName(Float.class, "yaw"), DSL.fieldByName(Float.class, "pitch"),
                        DSL.fieldByName(String.class, "world"),

                        DSL.fieldByName(Integer.class, "visits"),
                        DSL.fieldByName(String.class, "welcomeMessage"),

                        DSL.fieldByName(String.class, "permissions"),

                        DSL.fieldByName(String.class, "groupPermissions")).from(DSL.tableByName(tableName))
                .fetch();

        // TODO don't split this string twice
        List<String> playerNames = results.getValues("creator", String.class);
        for (String invitedPlayers : results.getValues("permissions", String.class)) {
            for (String invitedPlayer : splitter.split(invitedPlayers)) {
                if (playerNames.contains(invitedPlayer)) {
                    continue;
                }
                playerNames.add(invitedPlayer);
            }
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

        for (Iterator<Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String, String>> it = results
                .iterator(); it.hasNext();) {
            Record13<String, String, Boolean, Double, Double, Double, Float, Float, String, Integer, String, String, String> record = it
                    .next();
            UUID worldId = MyWarp.server().getWorld(record.value9()).getUID();
            Warp.Type type = record.value3() ? Warp.Type.PUBLIC : Warp.Type.PRIVATE;
            Collection<String> invitedGroups = Sets.newHashSet(splitter.split(record.value13()));

            Collection<UUID> invitedPlayerIds = new HashSet<UUID>();
            for (String invitedPlayer : splitter.split(record.value12())) {
                invitedPlayerIds.add(lookup.get(invitedPlayer));
            }
            Warp w = new Warp(record.value1(), lookup.get(record.value2()), type, record.value4(),
                    record.value5(), record.value6(), record.value7(), record.value8(), worldId,
                    record.value10(), record.value11(), invitedPlayerIds, invitedGroups);
            ret.add(w);
        }
        return ret;
    }

}

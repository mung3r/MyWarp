package me.taylorkelly.mywarp.dataconnections;

import static me.taylorkelly.mywarp.dataconnections.jooq.Tables.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.Player;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.GroupRecord;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.PlayerRecord;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.WarpRecord;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.WorldRecord;

import org.jooq.DSLContext;
import org.jooq.Record13;
import org.jooq.Result;
import org.jooq.types.UInteger;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * The connection to a SQL database via JOOQ.
 */
public class JOOQConnection implements DataConnection {

    private final DSLContext create;

    private final Connection conn;

    private final ListeningExecutorService executor;

    /**
     * Creates this JOOQConnection using the given DSLContext with the given
     * connection.
     * 
     * @param create
     *            the DSLContext to use
     * @param conn
     *            the Connection to use
     */
    protected JOOQConnection(DSLContext create, Connection conn, ListeningExecutorService executor) {
        this.create = create;
        this.conn = conn;
        this.executor = executor;
    }

    @Override
    public void close() {
        executor.shutdown();

        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            MyWarp.logger().log(Level.WARNING, "Failed to close SQL connection.", e);
        }

    }

    @Override
    public void addWarp(final Warp warp) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord record = create.newRecord(WARP);

                record.setName(warp.getName());

                PlayerRecord playerRecord = create.fetchOne(PLAYER, PLAYER.PLAYER_.eq(warp.getCreatorId()));
                if (playerRecord == null) {
                    playerRecord = create.newRecord(PLAYER);
                    playerRecord.setPlayer(warp.getCreatorId());
                    playerRecord.store();
                }
                record.setPlayer_id(playerRecord.getPlayer_id());

                record.setType(warp.getType());

                record.setX(warp.getX());
                record.setY(warp.getY());
                record.setZ(warp.getZ());
                record.setPitch(warp.getPitch());
                record.setYaw(warp.getYaw());

                // world
                WorldRecord worldRecord = create.fetchOne(WORLD, WORLD.WORLD_.eq(warp.getWorldId()));

                if (worldRecord == null) {
                    worldRecord = create.newRecord(WORLD);
                    worldRecord.setWorld(warp.getWorldId());
                    worldRecord.store();
                }
                record.setWorld_id(worldRecord.getWorld_id());

                record.setVisits(UInteger.valueOf(warp.getVisits()));
                record.setWelcome_message(warp.getWelcomeMessage());

                // TODO actually use timestamp & fee
                record.setCreation_date(new Timestamp(Calendar.getInstance().getTime().getTime()));
                record.setFee(0.0);

                record.store();
            }

        });

    }

    @Override
    public void deleteWarp(final Warp warp) {
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
                Player c = PLAYER.as("c");

                // query the database and group results by name - each map-entry
                // contains all values for one single warp
                Map<String, Result<Record13<String, UUID, Type, Double, Double, Double, Float, Float, UUID, UInteger, String, UUID, String>>> groupedResults = create
                        .select(WARP.NAME, c.PLAYER_, WARP.TYPE, WARP.X, WARP.Y, WARP.Z, WARP.YAW,
                                WARP.PITCH, WORLD.WORLD_, WARP.VISITS, WARP.WELCOME_MESSAGE, PLAYER.PLAYER_,
                                GROUP.GROUP_)
                        .from(WARP
                                .join(WORLD).on(WARP.WORLD_ID.eq(WORLD.WORLD_ID))
                        .join(c).on(WARP.PLAYER_ID.eq(c.PLAYER_ID))
                        .leftOuterJoin(WARP2PLAYER).on(WARP2PLAYER.WARP_ID.eq(WARP.WARP_ID))
                        .leftOuterJoin(PLAYER).on(WARP2PLAYER.PLAYER_ID.eq(PLAYER.PLAYER_ID)))
                        .leftOuterJoin(WARP2GROUP).on(WARP2GROUP.WARP_ID.eq(WARP.WARP_ID))
                        .leftOuterJoin(GROUP).on(WARP2GROUP.GROUP_ID.eq(GROUP.GROUP_ID))
                        .fetch().intoGroups(WARP.NAME);

                // create warp-instances from the results
                Collection<Warp> ret = new ArrayList<Warp>(groupedResults.size());
                for (Result<Record13<String, UUID, Type, Double, Double, Double, Float, Float, UUID, UInteger, String, UUID, String>> result : groupedResults
                        .values()) {
                    //XXX move code into a pretty helper method
                    Warp warp = new Warp(result.getValue(0, WARP.NAME), result.getValue(0, c.PLAYER_), result
                            .getValue(0, WARP.TYPE), result.getValue(0, WARP.X), result.getValue(0, WARP.Y),
                            result.getValue(0, WARP.Z), result.getValue(0, WARP.YAW), result.getValue(0,
                                    WARP.PITCH), result.getValue(0, WORLD.WORLD_), result.getValue(0,
                                    WARP.VISITS).intValue(), result.getValue(0, WARP.WELCOME_MESSAGE),
                            Collections2.filter(result.getValues(PLAYER.PLAYER_), Predicates.notNull()),
                            Collections2.filter(result.getValues(GROUP.GROUP_), Predicates.notNull()));
                    ret.add(warp);
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

                GroupRecord groupRecord = create.fetchOne(GROUP, GROUP.GROUP_.eq(groupId));
                if (groupRecord == null) {
                    groupRecord = create.newRecord(GROUP);
                    groupRecord.setGroup(groupId);
                    groupRecord.store();
                }

                create.insertInto(WARP2GROUP).set(WARP2GROUP.WARP_ID, warpRecord.getWarp_id())
                        .set(WARP2GROUP.GROUP_ID, groupRecord.getGroup_id()).execute();
            }

        });

    }

    @Override
    public void invitePlayer(final Warp warp, final UUID player) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

                PlayerRecord playerRecord = create.fetchOne(PLAYER, PLAYER.PLAYER_.eq(player));

                if (playerRecord == null) {
                    playerRecord = create.newRecord(PLAYER);
                    playerRecord.setPlayer(player);
                    playerRecord.store();
                }

                create.insertInto(WARP2PLAYER).set(WARP2PLAYER.WARP_ID, warpRecord.getWarp_id())
                        .set(WARP2PLAYER.PLAYER_ID, playerRecord.getPlayer_id()).execute();
            }

        });

    }

    @Override
    public void uninviteGroup(final Warp warp, final String groupId) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
                GroupRecord groupRecord = create.fetchOne(GROUP, GROUP.GROUP_.eq(groupId));

                create.delete(WARP2GROUP).where(
                        WARP2GROUP.WARP_ID.eq(warpRecord.getWarp_id()).and(
                                WARP2GROUP.GROUP_ID.eq(groupRecord.getGroup_id()))).execute();
            }

        });

    }

    @Override
    public void uninvitePlayer(final Warp warp, final UUID player) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));
                PlayerRecord playerRecord = create.fetchOne(PLAYER, PLAYER.PLAYER_.eq(player));

                create.delete(WARP2PLAYER).where(
                        WARP2PLAYER.WARP_ID.eq(warpRecord.getWarp_id()).and(
                                WARP2PLAYER.PLAYER_ID.eq(playerRecord.getPlayer_id()))).execute();
            }

        });

    }

    @Override
    public void updateCreator(final Warp warp) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord record = create.fetchOne(WARP, WARP.NAME.eq(warp.getName()));

                PlayerRecord playerRecord = create.fetchOne(PLAYER, PLAYER.PLAYER_.eq(warp.getCreatorId()));

                if (playerRecord == null) {
                    playerRecord = create.newRecord(PLAYER);
                    playerRecord.setPlayer(warp.getCreatorId());
                    playerRecord.store();
                }
                record.setPlayer_id(playerRecord.getPlayer_id());
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

                // set the loc
                record.setX(warp.getX());
                record.setY(warp.getY());
                record.setZ(warp.getZ());
                record.setPitch(warp.getPitch());
                record.setYaw(warp.getYaw());

                // world
                WorldRecord worldRecord = create.fetchOne(WORLD, WORLD.WORLD_.eq(warp.getWorldId()));

                if (worldRecord == null) {
                    worldRecord = create.newRecord(WORLD);
                    worldRecord.setWorld(warp.getWorldId());
                    worldRecord.store();
                }
                record.setWorld_id(worldRecord.getWorld_id());
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

                record.setWelcome_message(warp.getWelcomeMessage());
                record.update();
            }

        });

    }

}

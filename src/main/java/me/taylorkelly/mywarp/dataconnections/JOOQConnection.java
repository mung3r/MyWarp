package me.taylorkelly.mywarp.dataconnections;

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

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record13;
import org.jooq.Result;
import org.jooq.types.UInteger;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.dataconnections.DataConnection;
import me.taylorkelly.mywarp.dataconnections.jooq.Tables;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.Player;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.GroupRecord;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.PlayerRecord;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.WarpRecord;
import me.taylorkelly.mywarp.dataconnections.jooq.tables.records.WorldRecord;

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
     *            the Connection zo use
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
                WarpRecord record = create.newRecord(Tables.WARP);

                record.setName(warp.getName());

                PlayerRecord playerRecord = create.fetchOne(Tables.PLAYER,
                        Tables.PLAYER.PLAYER_.eq(warp.getCreatorId()));
                if (playerRecord == null) {
                    playerRecord = create.newRecord(Tables.PLAYER);
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
                WorldRecord worldRecord = create.fetchOne(Tables.WORLD,
                        Tables.WORLD.WORLD_.eq(warp.getWorldId()));

                if (worldRecord == null) {
                    worldRecord = create.newRecord(Tables.WORLD);
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
                Query query = create.delete(Tables.WARP).where(Tables.WARP.NAME.eq(warp.getName()));
                query.execute();
            }

        });
    }

    @Override
    public ListenableFuture<Collection<Warp>> getWarps() {
        return executor.submit(new Callable<Collection<Warp>>() {

            @Override
            public Collection<Warp> call() {
                // Alias for the player-table to represent the warp-creator
                Player c = Tables.PLAYER.as("c");

                // query the database and group results by name - each map-entry
                // contains all values for one single warp
                Map<String, Result<Record13<String, UUID, Type, Double, Double, Double, Float, Float, UUID, UInteger, String, UUID, String>>> groupedResults = create
                        .select(Tables.WARP.NAME, c.PLAYER_, Tables.WARP.TYPE, Tables.WARP.X, Tables.WARP.Y,
                                Tables.WARP.Z, Tables.WARP.YAW, Tables.WARP.PITCH, Tables.WORLD.WORLD_,
                                Tables.WARP.VISITS, Tables.WARP.WELCOME_MESSAGE, Tables.PLAYER.PLAYER_,
                                Tables.GROUP.GROUP_)
                        .from(Tables.WARP.join(Tables.WORLD)
                                .on(Tables.WARP.WORLD_ID.eq(Tables.WORLD.WORLD_ID)).join(c)
                                .on(Tables.WARP.PLAYER_ID.eq(c.PLAYER_ID)).leftOuterJoin(Tables.WARP2PLAYER)
                                .on(Tables.WARP2PLAYER.WARP_ID.eq(Tables.WARP.WARP_ID))
                                .leftOuterJoin(Tables.PLAYER)
                                .on(Tables.WARP2PLAYER.PLAYER_ID.eq(Tables.PLAYER.PLAYER_ID)))
                        .leftOuterJoin(Tables.WARP2GROUP)
                        .on(Tables.WARP2GROUP.WARP_ID.eq(Tables.WARP.WARP_ID)).leftOuterJoin(Tables.GROUP)
                        .on(Tables.WARP2GROUP.GROUP_ID.eq(Tables.GROUP.GROUP_ID)).fetch()
                        .intoGroups(Tables.WARP.NAME);

                // create warp-instances from the results
                Collection<Warp> ret = new ArrayList<Warp>(groupedResults.size());
                for (Result<Record13<String, UUID, Type, Double, Double, Double, Float, Float, UUID, UInteger, String, UUID, String>> result : groupedResults
                        .values()) {
                    Warp warp = new Warp(result.getValue(0, Tables.WARP.NAME), result.getValue(0, c.PLAYER_),
                            result.getValue(0, Tables.WARP.TYPE), result.getValue(0, Tables.WARP.X), result
                                    .getValue(0, Tables.WARP.Y), result.getValue(0, Tables.WARP.Z), result
                                    .getValue(0, Tables.WARP.YAW), result.getValue(0, Tables.WARP.PITCH),
                            result.getValue(0, Tables.WORLD.WORLD_), result.getValue(0, Tables.WARP.VISITS)
                                    .intValue(), result.getValue(0, Tables.WARP.WELCOME_MESSAGE), result
                                    .getValues(Tables.PLAYER.PLAYER_), result.getValues(Tables.GROUP.GROUP_));
                    ret.add(warp);
                }

                return ret;
            }

        });

    }

    @Override
    public void inviteGroup(final Warp warp, final String group) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

                GroupRecord groupRecord = create.fetchOne(Tables.GROUP, Tables.GROUP.GROUP_.eq(group));

                if (groupRecord == null) {
                    groupRecord = create.newRecord(Tables.GROUP);
                    groupRecord.setGroup(group);
                    groupRecord.store();
                }

                Query query = create.insertInto(Tables.WARP2PLAYER)
                        .set(Tables.WARP2PLAYER.WARP_ID, warpRecord.getWarp_id())
                        .set(Tables.WARP2PLAYER.PLAYER_ID, groupRecord.getGroup_id());
                query.execute();
            }

        });

    }

    @Override
    public void invitePlayer(final Warp warp, final UUID player) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

                PlayerRecord playerRecord = create.fetchOne(Tables.PLAYER, Tables.PLAYER.PLAYER_.eq(player));

                if (playerRecord == null) {
                    playerRecord = create.newRecord(Tables.PLAYER);
                    playerRecord.setPlayer(player);
                    playerRecord.store();
                }

                Query query = create.insertInto(Tables.WARP2PLAYER)
                        .set(Tables.WARP2PLAYER.WARP_ID, warpRecord.getWarp_id())
                        .set(Tables.WARP2PLAYER.PLAYER_ID, playerRecord.getPlayer_id());
                query.execute();
            }

        });

    }

    @Override
    public void uninviteGroup(final Warp warp, final String group) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));
                GroupRecord groupRecord = create.fetchOne(Tables.GROUP, Tables.GROUP.GROUP_.eq(group));

                create.delete(Tables.WARP2GROUP).where(
                        Tables.WARP2GROUP.WARP_ID.eq(warpRecord.getWarp_id()).and(
                                Tables.WARP2GROUP.GROUP_ID.eq(groupRecord.getGroup_id())));
            }

        });

    }

    @Override
    public void uninvitePlayer(final Warp warp, final UUID player) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord warpRecord = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));
                PlayerRecord playerRecord = create.fetchOne(Tables.PLAYER, Tables.PLAYER.PLAYER_.eq(player));

                Query query = create.delete(Tables.WARP2PLAYER).where(
                        Tables.WARP2PLAYER.WARP_ID.eq(warpRecord.getWarp_id()).and(
                                Tables.WARP2PLAYER.PLAYER_ID.eq(playerRecord.getPlayer_id())));
                query.execute();
            }

        });

    }

    @Override
    public void updateCreator(final Warp warp) {
        executor.submit(new Runnable() {

            @Override
            public void run() {
                WarpRecord record = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

                PlayerRecord playerRecord = create.fetchOne(Tables.PLAYER,
                        Tables.PLAYER.PLAYER_.eq(warp.getCreatorId()));

                if (playerRecord == null) {
                    playerRecord = create.newRecord(Tables.PLAYER);
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
                WarpRecord record = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

                // set the loc
                record.setX(warp.getX());
                record.setY(warp.getY());
                record.setZ(warp.getZ());
                record.setPitch(warp.getPitch());
                record.setYaw(warp.getYaw());

                // world
                WorldRecord worldRecord = create.fetchOne(Tables.WORLD,
                        Tables.WORLD.WORLD_.eq(warp.getWorldId()));

                if (worldRecord == null) {
                    worldRecord = create.newRecord(Tables.WORLD);
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
                WarpRecord record = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

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
                WarpRecord record = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

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
                WarpRecord record = create.fetchOne(Tables.WARP, Tables.WARP.NAME.eq(warp.getName()));

                record.setWelcome_message(warp.getWelcomeMessage());
                record.update();
            }

        });

    }

}

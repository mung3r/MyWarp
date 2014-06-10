package me.taylorkelly.mywarp.dataconnections.migrators;

import java.util.Collection;

import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.dataconnections.DataConnection;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class DataConnectionMigrator implements DataMigrator {

    private final ListenableFuture<DataConnection> futureConnection;
    private DataConnection storedConn = null;

    public DataConnectionMigrator(ListenableFuture<DataConnection> futureConnection) {
        this.futureConnection = futureConnection;
    }

    @Override
    public ListenableFuture<Collection<Warp>> getWarps() {
        ListenableFuture<Collection<Warp>> futureWarps = Futures.chain(futureConnection,
                new Function<DataConnection, ListenableFuture<Collection<Warp>>>() {

                    @Override
                    public ListenableFuture<Collection<Warp>> apply(DataConnection conn) {
                        storedConn = conn;
                        ListenableFuture<Collection<Warp>> ret = conn.getWarps();
                        return ret;
                    }

                });
        // of the function above fails the database connection remains open
        Futures.addCallback(futureWarps, new FutureCallback<Collection<Warp>>() {

            @Override
            public void onFailure(Throwable t) {
                if (storedConn != null) {
                    storedConn.close();
                }
            }

            @Override
            public void onSuccess(Collection<Warp> warps) {
                if (storedConn != null) {
                    storedConn.close();
                }
            }

        });
        return futureWarps;
    }

}

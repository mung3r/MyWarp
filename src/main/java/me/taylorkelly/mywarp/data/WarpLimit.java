package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

public class WarpLimit extends ValuePermissionContainer {

    private final int maxTotal;
    private final int maxPublic;
    private final int maxPrivate;

    public WarpLimit(String name, int maxTotal, int maxPublic, int maxPrivate) {
        super(name);
        this.maxTotal = maxTotal;
        this.maxPublic = maxPublic;
        this.maxPrivate = maxPrivate;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getMaxPublic() {
        return maxPublic;
    }

    public int getMaxPrivate() {
        return maxPrivate;
    }
}
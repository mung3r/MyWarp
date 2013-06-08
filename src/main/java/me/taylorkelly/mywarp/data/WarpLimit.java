package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * This storage object stores represents one warp-limit with it's respective
 * values
 */
public class WarpLimit extends ValuePermissionContainer {

    private final int maxTotal;
    private final int maxPublic;
    private final int maxPrivate;

    /**
     * Initializes the WarpLimit. The given limits are stored internally.
     * 
     * @param name
     *            the name used on permission lookup
     * @param maxTotal
     *            the total warp-limit
     * @param maxPublic
     *            the public warp-limit
     * @param maxPrivate
     *            the private warp-limit
     */
    public WarpLimit(String name, int maxTotal, int maxPublic, int maxPrivate) {
        super(name);
        this.maxTotal = maxTotal;
        this.maxPublic = maxPublic;
        this.maxPrivate = maxPrivate;
    }

    /**
     * Gets the limit for all warps
     * 
     * @return the total warp-limit
     */
    public int getMaxTotal() {
        return maxTotal;
    }

    /**
     * Gets the limit for public warps
     * 
     * @return the public warp-limit
     */
    public int getMaxPublic() {
        return maxPublic;
    }

    /**
     * Gets the limit for private warps
     * 
     * @return the private warp-limit
     */
    public int getMaxPrivate() {
        return maxPrivate;
    }
}
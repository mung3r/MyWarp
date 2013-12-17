package me.taylorkelly.mywarp.data;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * This storage object stores represents one warp-limit with it's respective
 * values
 */
public class WarpLimit extends ValuePermissionContainer {

    private final int maxTotal;
    private final int maxPublic;
    private final int maxPrivate;
    private final List<String> worlds;

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
    public WarpLimit(String name, int maxTotal, int maxPublic, int maxPrivate, List<String> worlds) {
        super(name);
        this.maxTotal = maxTotal;
        this.maxPublic = maxPublic;
        this.maxPrivate = maxPrivate;
        this.worlds = worlds;
        
        MyWarp.logger().info("Initated warp-limit: " + name + "(maxTotal: " + maxTotal + ", maxPublic: " + maxPublic + ", maxPrivate: " + maxPrivate + ", worlds: " + StringUtils.join(worlds, ";") + ")"); 
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

    public List<String> getEffectiveWorlds() {
        return Collections.unmodifiableList(worlds);
    }
    
    public boolean isGlobal() {
        return worlds.contains("all");
    }
    
    public boolean isEffectiveWorld (String worldname) {
        return isGlobal() || worlds.contains(worldname);
    }
}
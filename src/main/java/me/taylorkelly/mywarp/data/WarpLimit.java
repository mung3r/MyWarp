package me.taylorkelly.mywarp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.World;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * This storage object stores represents one warp-limit with it's respective
 * values
 */
public class WarpLimit extends ValuePermissionContainer {

    private final int totalLimit;
    private final int publicLimit;
    private final int privateLimit;
    private final List<String> affectedWorlds;

    /**
     * Initializes the WarpLimit. The given limits are stored internally.
     * 
     * @param name
     *            the name used on permission lookup
     * @param totalLimit
     *            the total warp-limit
     * @param publicLimit
     *            the public warp-limit
     * @param privateLimit
     *            the private warp-limit
     */
    public WarpLimit(String name, int totalLimit, int publicLimit, int privateLimit,
            List<String> affectedWorlds) {
        super(name);
        this.totalLimit = totalLimit;
        this.publicLimit = publicLimit;
        this.privateLimit = privateLimit;
        this.affectedWorlds = affectedWorlds;
    }

    /**
     * Gets the limit for all warps
     * 
     * @return the total warp-limit
     */
    public int getTotalLimit() {
        return totalLimit;
    }

    /**
     * Gets the limit for public warps
     * 
     * @return the public warp-limit
     */
    public int getPublicLimit() {
        return publicLimit;
    }

    /**
     * Gets the limit for private warps
     * 
     * @return the private warp-limit
     */
    public int getPrivateLimit() {
        return privateLimit;
    }

    /**
     * Gets a list of all worlds affected by this limit. The returned list will
     * be unmodifiable!
     * 
     * @return an unmodifiable list with all affected words
     */
    public List<String> getAffectedWorlds() {
        // if the limit is global, worlds just contains "all"
        if (isGlobal()) {
            List<String> affectedWorlds = new ArrayList<String>();
            for (World world : MyWarp.server().getWorlds()) {
                affectedWorlds.add(world.getName());
            }
            return Collections.unmodifiableList(affectedWorlds);
        }
        return Collections.unmodifiableList(affectedWorlds);
    }

    /**
     * Returns whether this limit is global and affects all worlds or not and
     * only affects specific worlds.
     * 
     * @return true if the limit is global, false if not
     */
    public boolean isGlobal() {
        return affectedWorlds.contains("all");
    }

    /**
     * Returns if the world of the given name is affected by this limit.
     * 
     * @param worldname
     *            the case sensitive worldname
     * @return true if the world is affected by this limit, false if not.
     */
    public boolean isEffectiveWorld(String worldname) {
        return isGlobal() || affectedWorlds.contains(worldname);
    }
}
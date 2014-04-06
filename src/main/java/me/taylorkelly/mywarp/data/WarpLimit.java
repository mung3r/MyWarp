package me.taylorkelly.mywarp.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.bukkit.World;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * This storage object stores represents one warp-limit with it's respective
 * values
 */
public class WarpLimit extends ValuePermissionContainer {

    public static enum Limit {
        TOTAL, PRIVATE, PUBLIC
    }

    private final EnumMap<Limit, Integer> limits = new EnumMap<Limit, Integer>(WarpLimit.Limit.class);

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
     * @param affectedWorlds
     *            a list of worlds affected by this plugin. If it contains
     *            <code>all</code>, the limit is global.
     */
    public WarpLimit(String name, int totalLimit, int publicLimit, int privateLimit,
            List<String> affectedWorlds) {
        super(name);

        limits.put(Limit.TOTAL, totalLimit);
        limits.put(Limit.PUBLIC, publicLimit);
        limits.put(Limit.PRIVATE, privateLimit);
        this.affectedWorlds = affectedWorlds;
    }

    public int getLimit(Limit limit) {
        return limits.get(limit);
    }

    // mapping to warp-types
    public int getLimit(Type type) {
        switch (type) {
        case PRIVATE:
            return limits.get(Limit.PRIVATE);
        case PUBLIC:
            return limits.get(Limit.PUBLIC);
        default:
            return 0;
        }
    }

    /**
     * Gets a list of all worlds affected by this limit.
     * 
     * @return a list with all affected worlds
     */
    public List<World> getAffectedWorlds() {
        // if the limit is global, worlds just contains "all"
        if (isGlobal()) {
            return MyWarp.server().getWorlds();
        }
        List<World> ret = new ArrayList<World>();
        for (String worldname : affectedWorlds) {
            ret.add(MyWarp.server().getWorld(worldname));
        }
        return ret;
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
     * Returns if the given world is affected by this limit.
     * 
     * @param world
     *            the world
     * @return true if the world is affected by this limit, false if not.
     */
    public boolean isEffectiveWorld(World world) {
        return isGlobal() || affectedWorlds.contains(world.getName());
    }
}
package me.taylorkelly.mywarp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.data.WarpLimit.Limit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * A warp-manager represents a warp-network. Each warp within this network is
 * unique.
 * 
 */
public class WarpManager {

    /**
     * This map stores all warps managed by this manager under their name.
     */
    private final Map<String, Warp> warpMap = new HashMap<String, Warp>();

    public enum BuildWarp {
        ALLOW, DENY_TOTAL, DENY_PRIVATE, DENY_PUBLIC
    }

    public WarpManager() {
        populate(MyWarp.inst().getConnectionManager().getWarps());
        MyWarp.logger().info(getLoadedWarpNumber() + " warps loaded.");
    }

    /**
     * Adds a newly created warp with the given name and type at the player's
     * current position to this manager.
     * 
     * @param name
     *            the name of the warp
     * @param player
     *            the player
     * @param type
     *            the type
     */
    public void addWarp(String name, Player player, Type type) {
        Warp warp = new Warp(name, player, type);

        warpMap.put(name, warp);
        MyWarp.inst().getConnectionManager().addWarp(warp);

        if (MyWarp.inst().getWarpSettings().dynmapEnabled && type == Type.PUBLIC) {
            MyWarp.inst().getMarkers().addWarp(warp);
        }
    }

    /**
     * Deletes the given warp from this manager.
     * 
     * @param warp
     *            the warp
     */
    public void deleteWarp(Warp warp) {
        warpMap.remove(warp.getName());
        MyWarp.inst().getConnectionManager().deleteWarp(warp);

        if (MyWarp.inst().getWarpSettings().dynmapEnabled) {
            MyWarp.inst().getMarkers().deleteWarp(warp);
        }
    }

    /**
     * Populates this manager with all given warps.
     * 
     * @param warps
     *            a collection of warps
     */
    public void populate(Collection<Warp> warps) {
        for (Warp warp : warps) {
            warpMap.put(warp.getName(), warp);
        }
    }

    /**
     * Gets the number of all warps that are currently managed by this manger.
     * 
     * @return the total number of warps
     */
    public int getLoadedWarpNumber() {
        return warpMap.size();
    }

    /**
     * Checks if a warp with the given name is currently managed by this
     * manager.
     * 
     * @param name
     *            the exact name
     * @return whether a warp with this name is already present
     */
    public boolean warpExists(String name) {
        return warpMap.containsKey(name);
    }

    /**
     * Gets the warp of the given name from this manager. Will return
     * <code>null</code> if a warp with the given name is not available on this
     * manager.
     * 
     * @param name
     *            the exact name
     * @return the warp with the given name
     */
    @Nullable
    public Warp getWarp(String name) {
        return warpMap.get(name);
    }

    /**
     * Gets a live view of all warps that fulfill the given predicate currently
     * managed by this manager.
     * 
     * @param predicate
     *            the predicate
     * @return all matching warps
     */
    public Collection<Warp> getWarps(Predicate<Warp> predicate) {
        return Collections2.filter(warpMap.values(), predicate);
    }

    /**
     * Matches a creator of warps managed by this manager that fulfill the given
     * predicate. Will return <code>null</code> if either no creator matches or
     * multiple creator match the given filter.
     * 
     * @param filter
     *            the filter
     * @param predicate
     *            the predicate
     * @return a matching creator
     */
    @Nullable
    public OfflinePlayer getMatchingCreator(String filter, Predicate<Warp> predicate) {
        Collection<Warp> applicableWarps = getWarps(predicate);
        OfflinePlayer ret = null;
        for (Warp warp : applicableWarps) {
            MyWarp.logger().info("Checking warp: " + warp.getName());
            OfflinePlayer creator = warp.getCreator();
            MyWarp.logger().info("With Creator: " + creator.getName());
            if (StringUtils.equalsIgnoreCase(creator.getName(), filter)) {
                // minecraft names are, as of 1.7.x case insensitive
                return creator;
            }
            if (StringUtils.containsIgnoreCase(creator.getName(), filter)) {
                if (ret != null) {
                    // no clear match so there is no point in continuing
                    return null;
                }
                ret = creator;
            }
        }
        return ret;
    }

    /**
     * Matches a world that contains warps managed by this manager that fulfill
     * the given predicate. Will return <code>null</code> if either no world
     * matches or multiple worlds match the given filter.
     * 
     * @param filter
     *            the filter
     * @param predicate
     *            the predicate
     * @return a matching world
     */
    @Nullable
    public World getMatchingWorld(String filter, Predicate<Warp> predicate) {
        Collection<Warp> applicableWarps = getWarps(predicate);
        World ret = null;
        for (Warp warp : applicableWarps) {
            World world = warp.getWorld();
            if (world == null) {
                // world not loaded
                continue;
            }
            if (world.getName().equals(filter)) {
                return world;
            }
            if (StringUtils.containsIgnoreCase(world.getName(), filter)) {
                if (ret != null) {
                    // no clear match so there is no point in continuing
                    return null;
                }
                ret = world;
            }
        }
        return ret;
    }

    public BuildWarp canAddWarp(final Player player, final World world, boolean newlyBuild, final Type type) {
        if ((!newlyBuild || MyWarp.inst().getPermissionsManager()
                .hasPermission(player, "mywarp.limit.disobey." + world.getName() + ".total"))
                && MyWarp
                        .inst()
                        .getPermissionsManager()
                        .hasPermission(player,
                                "mywarp.limit.disobey." + world.getName() + "." + type.getPermissionSuffix())) {
            return BuildWarp.ALLOW;
        }
        WarpLimit limit = MyWarp.inst().getPermissionsManager().getWarpLimit(player);
        final List<String> affectedWorlds = new ArrayList<String>();
        for (World affectedWorld : limit.getAffectedWorlds()) {
            // only count warps on worlds the player cannot disobey limits on
            if (!MyWarp
                    .inst()
                    .getPermissionsManager()
                    .hasPermission(
                            player,
                            "mywarp.limit.disobey." + affectedWorld.getName() + "."
                                    + type.getPermissionSuffix())) {
                affectedWorlds.add(affectedWorld.getName());
            }
        }
        Collection<Warp> totalWarps = getWarps(new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isCreator(player) && warp.isType(type);
            }

        });

        if (newlyBuild && atLeast(totalWarps, limit.getLimit(Limit.TOTAL))) {
            return BuildWarp.DENY_TOTAL;
        }

        if (atLeast(Collections2.filter(totalWarps, new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return warp.isType(type);
            }

        }), limit.getLimit(type))) {
            switch (type) {
            case PUBLIC:
                return BuildWarp.DENY_PUBLIC;
            case PRIVATE:
                return BuildWarp.DENY_PRIVATE;
            }
        }
        return BuildWarp.ALLOW;
    }

    private <T> boolean atLeast(Iterable<T> iterable, int count) {
        return Iterables.size(Iterables.limit(iterable, count)) == count;
    }
}

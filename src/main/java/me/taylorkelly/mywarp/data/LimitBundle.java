package me.taylorkelly.mywarp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.permissions.valuebundles.MultiworldValueBundle;
import me.taylorkelly.mywarp.utils.WarpUtils;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;

/**
 * A bundle that stores warp creation limits
 */
public class LimitBundle extends MultiworldValueBundle {

    /**
     * The different types limits.
     */
    public enum Limit {
        /**
         * The total limit (accounts all warps).
         */
        TOTAL(Predicates.<Warp> alwaysTrue(), "total"),
        /**
         * The private limit (accounts only private warps).
         */
        PRIVATE(WarpUtils.isType(Warp.Type.PRIVATE), "private"),
        /**
         * The public limit (accounts only public warps).
         */
        PUBLIC(WarpUtils.isType(Warp.Type.PUBLIC), "public");

        private final Predicate<Warp> condition;
        private final String permissionSuffix;

        /**
         * Initializes this Limit.
         * 
         * @param check
         *            the corresponding build-warp
         */
        private Limit(Predicate<Warp> condition, String permissionSuffix) {
            this.condition = condition;
            this.permissionSuffix = permissionSuffix;
        }
    }

    private final Map<Limit, Integer> limits = new EnumMap<Limit, Integer>(Limit.class);

    /**
     * Initializes this limit-bundle as global. It will affect all worlds.
     * 
     * @param identifier
     *            the unique identifier
     * @param totalLimit
     *            the total limit
     * @param publicLimit
     *            the public limit
     * @param privateLimit
     *            the private limit
     */
    public LimitBundle(String identifier, int totalLimit, int publicLimit, int privateLimit) {
        this(identifier, totalLimit, publicLimit, privateLimit, null);
    }

    /**
     * Initializes this limit-bundle. It will affect all given worlds.
     * 
     * @param identifier
     *            the unique identifier
     * @param totalLimit
     *            the total limit
     * @param publicLimit
     *            the public limit
     * @param privateLimit
     *            the private limit
     * @param affectedWorlds
     *            a collection of worlds that should be affected by this bundle.
     *            Can be null to affect all worlds.
     */
    public LimitBundle(String identifier, int totalLimit, int publicLimit, int privateLimit,
            @Nullable Iterable<String> affectedWorlds) {
        super(identifier, affectedWorlds);
        limits.put(LimitBundle.Limit.TOTAL, totalLimit);
        limits.put(LimitBundle.Limit.PUBLIC, publicLimit);
        limits.put(LimitBundle.Limit.PRIVATE, privateLimit);
    }

    /**
     * Returns whether the given iterable contains has least the given number of
     * entries.
     * 
     * @param iterable
     *            the iterable to check
     * @param count
     *            the number of entries the iterable should have at least
     * @return true if the given iterable has at least the given number of
     *         entries
     */
    private <T> boolean atLeast(Iterable<T> iterable, int count) {
        return Iterables.size(Iterables.limit(iterable, count)) == count;
    }

    /**
     * Returns whether the given player on the given world currently exceeds the
     * given limit on basis of the given collection of warps. A player exceeds a
     * limit if he has more or exactly as many warps as allowed by the given
     * limit.
     * 
     * @param limit
     *            the limit
     * @param player
     *            the player
     * @param world
     *            the world
     * @param warps
     *            the collection of warps
     * @return true if the player exceeds the limit on the given world
     */
    public boolean exceedsLimit(Limit limit, Player player, World world, Collection<Warp> warps) {
        final List<UUID> effectiveAffectedWorlds = new ArrayList<UUID>();
        for (World affectedWorld : getAffectedWorlds()) {
            if (MyWarp
                    .inst()
                    .getPermissionsManager()
                    .hasPermission(player,
                            "mywarp.limit.disobey." + affectedWorld.getName() + "." + limit.permissionSuffix)) {
                if (affectedWorld == world) {
                    // player can disobey the checked limit on this world so we
                    // can stop here
                    return false;
                }
            } else {
                effectiveAffectedWorlds.add(affectedWorld.getUID());
            }
        }

        return atLeast(Collections2.filter(warps, Predicates.and(limit.condition, new Predicate<Warp>() {

            @Override
            public boolean apply(Warp warp) {
                return effectiveAffectedWorlds.contains(warp.getWorldId());
            }

        })), limits.get(limit));

    }

    /**
     * Gets the maximum number of warps a user can create under the given limit.
     * 
     * @param limit
     *            the type of limit
     * @return the maximum number of warps
     */
    public int getLimit(Limit limit) {
        return limits.get(limit);
    }

    @Override
    protected String getBasePermission() {
        return "mywarp.limit";
    }

    @Override
    public String toString() {
        return "LimitBundle [getIdentifier()=" + getIdentifier() + ", limits=" + limits + ", affectedWorlds="
                + affectedWorlds + "]";
    }
}

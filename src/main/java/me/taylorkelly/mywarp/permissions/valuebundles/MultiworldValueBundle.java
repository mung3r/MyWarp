package me.taylorkelly.mywarp.permissions.valuebundles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.World;

import com.google.common.collect.ImmutableSet;

/**
 * A value-bundle implementation that allows storing values for a set of
 * different worlds instead of globally.
 */
public abstract class MultiworldValueBundle extends AbstractValueBundle {

    protected final ImmutableSet<String> affectedWorlds;

    /**
     * Initializes this bundle with the given identifier and the given world
     * names. The bundle will only affect the given worlds.
     * 
     * @param identifier
     *            the identifier
     * @param worldNames
     *            an iterable containing the names of worlds that the bundle
     *            should affect. Can be <code>null</code> to affect all worlds.
     */
    public MultiworldValueBundle(String identifier, @Nullable Iterable<String> worldNames) {
        super(identifier);
        if (worldNames != null) {
            affectedWorlds = ImmutableSet.copyOf(worldNames);
        } else {
            // the limit is global!
            affectedWorlds = null;
        }
    }

    /**
     * Gets a list of all worlds that are affected by this bundle.
     * 
     * @return a list with all affected worlds
     */
    public List<World> getAffectedWorlds() {
        if (isGlobal()) {
            return MyWarp.server().getWorlds();
        }
        List<World> ret = new ArrayList<World>();
        for (String worldName : affectedWorlds) {
            ret.add(MyWarp.server().getWorld(worldName));
        }
        return ret;
    }

    /**
     * Returns whether this bundle affects all worlds.
     * 
     * @return true if this bundle affects all worlds
     */
    public boolean isGlobal() {
        return affectedWorlds == null;
    }

    /**
     * Returns whether the given world is affected by this bundle.
     * 
     * @param world
     *            the world to check
     * @return true if the given world is affected
     */
    public boolean isAffectedWorld(World world) {
        return isGlobal() || affectedWorlds.contains(world.getName());
    }
}

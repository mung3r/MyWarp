package me.taylorkelly.mywarp.utils;

import java.util.concurrent.ConcurrentHashMap;

import me.taylorkelly.mywarp.MyWarp;

/**
 * This class extends a {@link ConcurrentHashMap} to implement an automatic
 * removal. Whenever an item is added to the map, an automatically cleanse
 * process is run that will remove the item after the timeframe defined upon
 * creation of this object.
 * 
 * @param <K>
 * @param <V>
 */
public class TempConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    /**
     * The default cleanse time in bukkit-ticks (20 ticks = 1 second).
     */
    private static final long DEF_CLEANSE = 30 * 20;
    private static final long serialVersionUID = 3245002001629448132L;

    private final long cleanse;

    /**
     * Creates a new map with the same mappings as the given map. The map is
     * created with the default cleanse (600) a capacity of 1.5 times the number
     * of mappings in the given map or 16 (whichever is greater), and a default
     * load factor (0.75) and concurrencyLevel (16).
     */
    public TempConcurrentHashMap() {
        this(DEF_CLEANSE);
    }

    /**
     * Creates a new map with the same mappings as the given map. The map is
     * created with the specified cleanse, a capacity of 1.5 times the number of
     * mappings in the given map or 16 (whichever is greater), and a default
     * load factor (0.75) and concurrencyLevel (16).
     * 
     * @param cleanse
     *            after which elements are removed from the map in bukkit-ticks
     */
    public TempConcurrentHashMap(long cleanse) {
        super();

        this.cleanse = cleanse;
    }

    @Override
    public V put(final K key, V value) {
        MyWarp.server().getScheduler().runTaskLaterAsynchronously(MyWarp.inst(), new Runnable() {
            @Override
            public void run() {
                remove(key);
            }
        }, cleanse);

        return super.put(key, value);
    }

}

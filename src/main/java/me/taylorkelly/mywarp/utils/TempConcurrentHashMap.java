package me.taylorkelly.mywarp.utils;

import java.util.concurrent.ConcurrentHashMap;

import me.taylorkelly.mywarp.MyWarp;

public class TempConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long DEF_CLEANSE = 30 * 20;

    private static final long serialVersionUID = 3245002001629448132L;

    private final long cleanse;
    private final MyWarp plugin;

    public TempConcurrentHashMap(MyWarp plugin) {
        this(plugin, DEF_CLEANSE);
    }

    public TempConcurrentHashMap(MyWarp plugin, long cleanse) {
        super();

        this.plugin = plugin;
        this.cleanse = cleanse;
    }

    @Override
    public V put(final K key, V value) {
        plugin.getServer().getScheduler()
                .runTaskLaterAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        remove(key);
                    }
                }, cleanse);

        return super.put(key, value);
    }

}

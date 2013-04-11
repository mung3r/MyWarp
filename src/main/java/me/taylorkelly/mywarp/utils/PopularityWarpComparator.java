package me.taylorkelly.mywarp.utils;

import java.util.Comparator;

import me.taylorkelly.mywarp.data.Warp;

public class PopularityWarpComparator implements Comparator<Warp> {
    @Override
    public int compare(Warp w1, Warp w2) {
        return w1.visits != w2.visits ? (w1.visits > w2.visits ? -1 : 1)
                : w1.name.compareTo(w2.name);
    }
}

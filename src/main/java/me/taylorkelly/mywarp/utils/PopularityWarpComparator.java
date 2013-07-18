package me.taylorkelly.mywarp.utils;

import java.util.Comparator;

import me.taylorkelly.mywarp.data.Warp;

/**
 * This comperator will compare {@link Warp}s based on their popularity
 */
public class PopularityWarpComparator implements Comparator<Warp> {
    @Override
    public int compare(Warp w1, Warp w2) {
        return w1.getVisits() != w2.getVisits() ? (w1.getVisits() > w2.getVisits() ? -1 : 1) : w1.getName()
                .compareTo(w2.getName());
    }
}

package me.taylorkelly.mywarp.utils;

import java.util.Comparator;

import me.taylorkelly.mywarp.data.WarpLimit;

public class WarpLimitComparator implements Comparator<WarpLimit> {

    @Override
    public int compare(WarpLimit warpLimit1, WarpLimit warpLimit2) {
        return warpLimit1.getName().compareTo(warpLimit2.getName());
    }

}

package me.taylorkelly.mywarp.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

public class Matcher {

    private final Collection<Warp> matchingWarps;
    private final String filter;

    private Matcher(final String filter, Predicate<Warp> predicate) {
        this.filter = filter;

        matchingWarps = MyWarp.inst().getWarpManager()
                .getWarps(Predicates.and(predicate, new Predicate<Warp>() {

                    @Override
                    public boolean apply(Warp warp) {
                        return StringUtils.containsIgnoreCase(warp.getName(), filter);
                    }

                }));
    }
    
    public static Matcher match(final String filter, Predicate<Warp> predicate) {
        return new Matcher(filter, predicate);
    }

    @Nullable
    public Warp getExactMatch() {
        // only one warp contains the filter sequence
        if (matchingWarps.size() <= 1) {
            return Iterables.getFirst(matchingWarps, null);
        }

        // filter for warps that have the exact name (case insensitive)
        Set<Warp> sameNameWarps = new HashSet<Warp>();
        for (Warp warp : matchingWarps) {
            if (StringUtils.equalsIgnoreCase(warp.getName(), filter)) {
                sameNameWarps.add(warp);
            }
        }
        if (sameNameWarps.size() <= 1) {
            return Iterables.getFirst(sameNameWarps, null);
        }

        // filter for warps that have the exact name (case sensitive)
        Set<Warp> sameCaseNameWarps = new HashSet<Warp>();
        for (Warp warp : sameNameWarps) {
            if (StringUtils.equalsIgnoreCase(warp.getName(), filter)) {
                sameCaseNameWarps.add(warp);
            }
        }
        return Iterables.getFirst(sameCaseNameWarps, null);
    }

    public SortedSet<Warp> getMatches() {
        SortedSet<Warp> sortedMatches = new TreeSet<Warp>();
        sortedMatches.addAll(matchingWarps);
        return sortedMatches;
    }

    public SortedSet<Warp> getMatches(Comparator<Warp> comparator) {
        SortedSet<Warp> sortedMatches = new TreeSet<Warp>(comparator);
        sortedMatches.addAll(matchingWarps);
        return sortedMatches;
    }

    @Nullable
    public Warp getMatch() {
        return Iterables.getFirst(getMatches(), null);
    }

    @Nullable
    public Warp getMatch(Comparator<Warp> comparator) {
        return Iterables.getFirst(getMatches(comparator), null);
    }

}

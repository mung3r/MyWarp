package me.taylorkelly.mywarp.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.WarpManager;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

/**
 * A matcher provides sorted or filtered access to warps matching a given
 * filter.
 */
public class Matcher {

    private final Collection<Warp> matchingWarps;
    private final String nameFilter;

    /**
     * Initializes this matcher.
     * 
     * @see #match(String, Predicate)
     * 
     * @param nameFilter
     *            the nameFilter
     * @param predicate
     *            the predicate
     */
    private Matcher(final String nameFilter, Predicate<Warp> predicate) {
        this.nameFilter = nameFilter;

        matchingWarps = MyWarp.inst().getWarpManager()
                .getWarps(Predicates.and(predicate, new Predicate<Warp>() {

                    @Override
                    public boolean apply(Warp warp) {
                        return StringUtils.containsIgnoreCase(warp.getName(), nameFilter);
                    }

                }));
    }

    /**
     * Returns a Matcher that matches warps that fulfill the given name-filter
     * and the given predicate. The name-filter matches all warps with a name
     * that contains the given string case insensitively.
     * 
     * @see WarpManager#getWarps(Predicate)
     * 
     * @param nameFilter
     *            the nameFIlter
     * @param predicate
     *            the predicate
     * @return a Matcher using the given values
     */
    public static Matcher match(String nameFilter, Predicate<Warp> predicate) {
        return new Matcher(nameFilter, predicate);
    }

    /**
     * Attempts to get an exact match. An exact match is a warp that is:
     * <ul>
     * <li>either the only warp matching this matchers criteria,</li>
     * <li>or the only warp whose name includes case-insensitively this matchers
     * nameFiler,</li>
     * <li>or the only warp whose name is identical woth this matchers
     * name-filter.</li>
     * </ul>
     * 
     * @return a warp matching one of the criteria above or null
     */
    @Nullable
    public Warp getExactMatch() {
        // only one warp contains the filter sequence
        if (matchingWarps.size() <= 1) {
            return Iterables.getFirst(matchingWarps, null);
        }

        // filter for warps that have the exact name (case insensitive)
        Set<Warp> sameNameWarps = new HashSet<Warp>();
        for (Warp warp : matchingWarps) {
            if (StringUtils.equalsIgnoreCase(warp.getName(), nameFilter)) {
                sameNameWarps.add(warp);
            }
        }
        if (sameNameWarps.size() <= 1) {
            return Iterables.getFirst(sameNameWarps, null);
        }

        // filter for warps that have the exact name (case sensitive)
        Set<Warp> sameCaseNameWarps = new HashSet<Warp>();
        for (Warp warp : sameNameWarps) {
            if (StringUtils.equalsIgnoreCase(warp.getName(), nameFilter)) {
                sameCaseNameWarps.add(warp);
            }
        }
        return Iterables.getFirst(sameCaseNameWarps, null);
    }

    /**
     * Gets a naturally sorted immutable list that contains all warps matching
     * this Matchers criteria.
     * 
     * @return a list of all matches
     */
    public ImmutableList<Warp> getMatches() {
        return Ordering.natural().immutableSortedCopy(matchingWarps);
    }

    /**
     * Gets a sorted immutable list that contains all warps matching this
     * Matchers criteria.
     * 
     * @param comparator
     *            the comparator used to sort the list
     * @return a list of all matches
     */
    public ImmutableList<Warp> getMatches(Comparator<Warp> comparator) {
        return Ordering.from(comparator).immutableSortedCopy(matchingWarps);
    }

    /**
     * Gets the warp that matches this Matchers criteria and is the first when
     * sorting all matching warps naturally. However, this warp is not
     * guaranteed to be an exact match.
     * 
     * @return a matching warp or null, if there are not matching warps
     */
    @Nullable
    public Warp getMatch() {
        return Iterables.getFirst(getMatches(), null);
    }

    /**
     * Gets the warp that matches this Matchers criteria and is the first when
     * sorting all matching warps using the given comparator. However, this warp
     * is not guaranteed to be an exact match.
     * 
     * @param comparator
     *            the comparator used to sort the matches
     * @return a matching warp or null, if there are not matching warps
     */
    @Nullable
    public Warp getMatch(Comparator<Warp> comparator) {
        return Iterables.getFirst(getMatches(comparator), null);
    }

}

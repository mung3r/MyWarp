package me.taylorkelly.mywarp.utils;

import java.util.TreeSet;

import me.taylorkelly.mywarp.data.Warp;

/**
 * Stores matches of a search
 */
public class MatchList {
    /**
     * Initializes the object.
     * 
     * @param exactMatches
     *            the exact matches, contains only warp that exactly match with
     *            the query
     * @param matches
     *            the matches, contains all warps that somehow match with the
     *            query
     */
    public MatchList(TreeSet<Warp> exactMatches, TreeSet<Warp> matches) {
        this.exactMatches = exactMatches;
        this.matches = matches;
    }

    public TreeSet<Warp> exactMatches;
    public TreeSet<Warp> matches;

    /**
     * Gets the closest match. Will return null if no exact match could be found
     * 
     * @param name
     *            the name
     * @return the closest match, must be exact
     */
    public Warp getMatch() {
        if (exactMatches.size() == 1) {
            return exactMatches.iterator().next();
        }
        if (exactMatches.size() == 0 && matches.size() == 1) {
            return matches.iterator().next();
        }
        return null;
    }

    public Warp getLikliestMatch() {
        if (!exactMatches.isEmpty()) {
            return exactMatches.iterator().next();
        }
        if (!matches.isEmpty()) {
            return matches.iterator().next();
        }
        return null;
    }
}

package me.taylorkelly.mywarp.utils;

import java.util.TreeSet;

import me.taylorkelly.mywarp.data.Warp;

/**
 * Stores matches of a search
 */
public class MatchList {
    public MatchList(TreeSet<Warp> exactMatches, TreeSet<Warp> matches) {
        this.exactMatches = exactMatches;
        this.matches = matches;
    }

    public TreeSet<Warp> exactMatches;
    public TreeSet<Warp> matches;

    /**
     * Gets the closest match. Will return the name if no exact match could be found
     * 
     * @param name the name
     * @return the closest match
     */
    public String getMatch(String name) {
        if (exactMatches.size() == 1) {
            return exactMatches.iterator().next().name;
        }
        if (exactMatches.size() == 0 && matches.size() == 1) {
            return matches.iterator().next().name;
        }
        return name;
    }
}

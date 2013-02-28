package me.taylorkelly.mywarp.data;

import java.util.TreeSet;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.utils.MatchList;
import me.taylorkelly.mywarp.utils.popularityWarpComperator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Searches for warps
 */
public class Searcher {
    private WarpList warpList;
    private CommandSender executor;
    private Player player;

    private TreeSet<Warp> exactMatches;
    private TreeSet<Warp> matches;

    private String query;

    public Searcher (CommandSender executor, String query,
            WarpList warpList) {
        this.warpList = warpList;
        this.executor = executor;
        this.query = query;
    }

    /**
     * Searches for warps and sends results back to the executor
     */
    public void search() {
        if (executor instanceof Player) {
            player = (Player) executor;
        }
        
        MatchList matchingWarps = warpList.getMatches(query, player, new popularityWarpComperator());
        this.exactMatches = matchingWarps.exactMatches;
        this.matches = matchingWarps.matches;
        
        if (exactMatches.size() == 0 && matches.size() == 0) {
            executor.sendMessage(LanguageManager.getString("search.noMatches")
                    .replaceAll("%query%", query));
        } else {
            if (exactMatches.size() > 0) {
                executor.sendMessage(LanguageManager.getString(
                        "search.exactMatches").replaceAll("%query%", query));
                sendWarpMatches(exactMatches);
            }
            if (matches.size() > 0) {
                executor.sendMessage(LanguageManager.getString(
                        "search.partitalMatches").replaceAll("%query%", query));
                sendWarpMatches(matches);
            }
        }
    }

    private void sendWarpMatches(TreeSet<Warp> warps) {
        for (Warp warp : warps) {
            String color;
            if (player == null || warp.playerIsCreator(player.getName())) {
                color = ChatColor.AQUA.toString();
            } else if (warp.publicAll) {
                color = ChatColor.GREEN.toString();
            } else {
                color = ChatColor.RED.toString();
            }
            String creator = player != null ? (warp.creator
                    .equalsIgnoreCase(player.getName()) ? LanguageManager
                    .getString("list.you") : warp.creator) : warp.creator;
            int x = (int) Math.round(warp.x);
            int y = warp.y;
            int z = (int) Math.round(warp.z);
            executor.sendMessage(color + "'" + warp.name + "' "
                    + ChatColor.WHITE
                    + LanguageManager.getColorlessString("list.by") + " "+ creator
                    + " @(" + x + ", " + y + ", " + z + ")");
        }
    }
}
package me.taylorkelly.mywarp.data;

import java.util.ArrayList;

import me.taylorkelly.mywarp.LanguageManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Searcher {
    private WarpList warpList;
    private CommandSender executor;
    private Player player;

    private ArrayList<Warp> exactMatches;
    private ArrayList<Warp> matches;

    private String query;

    public Searcher(WarpList warpList) {
        this.warpList = warpList;
    }

    public void addExecutor(CommandSender executor, Player player) {
        this.executor = executor;
        this.player = player;
    }

    public void setQuery(String name) {
        this.query = name;
        MatchList matches = warpList.getMatches(name, player);
        this.exactMatches = matches.exactMatches;
        this.matches = matches.matches;
    }

    public void sendWarpMatches(ArrayList<Warp> warps) {
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

    public void search() {
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
}

class MatchList {
    public MatchList(ArrayList<Warp> exactMatches, ArrayList<Warp> matches) {
        this.exactMatches = exactMatches;
        this.matches = matches;
    }

    public ArrayList<Warp> exactMatches;
    public ArrayList<Warp> matches;

    public String getMatch(String name) {
        if (exactMatches.size() == 1) {
            return exactMatches.get(0).name;
        }
        if (exactMatches.size() == 0 && matches.size() == 1) {
            return matches.get(0).name;
        }
        return name;
    }
}
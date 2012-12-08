package me.taylorkelly.mywarp.data;

import java.util.ArrayList;

import me.taylorkelly.mywarp.LanguageManager;

import org.angelsl.minecraft.randomshit.fontwidth.MinecraftFontWidthCalculator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Lister {
    private WarpList warpList;
    private CommandSender executor;
    private Player player;

    private int maxPages;
    private int page;
    private String warpCreator;

    private static final int WARPS_PER_PAGE = 8;
    ArrayList<Warp> sortedWarps;

    public Lister(WarpList warpList) {
        this.warpList = warpList;
    }

    public void addExecutor(CommandSender executor, Player player) {
        this.executor = executor;
        this.player = player;
    }

    public void setWarpCreator(String warpCreator) {
        this.warpCreator = warpCreator;
    }

    public void setPage(int page) {
        this.page = page;
        int start = (page - 1) * WARPS_PER_PAGE;
        if (warpCreator == null) {
            sortedWarps = warpList
                    .getSortedWarps(player, start, WARPS_PER_PAGE);
            maxPages = (int) Math.ceil(warpList.getMaxWarps(player)
                    / (double) WARPS_PER_PAGE);
        } else {
            String matchingWarpCreator = warpList.getMatchingCreator(player,
                    warpCreator);
            sortedWarps = warpList.getSortedWarpsPerCreator(player,
                    matchingWarpCreator, start, WARPS_PER_PAGE);
            maxPages = (int) Math.ceil(warpList.getMaxWarpsPerCreator(player,
                    matchingWarpCreator) / (double) WARPS_PER_PAGE);
        }
    }

    public void list() {
        if (maxPages == 0) {
            executor.sendMessage(LanguageManager.getString("list.noWarps"));
        } else {
            String intro = "------------------- "
                    + LanguageManager.getColorlessString("list.page") + " "
                    + page + "/" + maxPages + " -------------------";
            executor.sendMessage(ChatColor.YELLOW + intro);
            for (Warp warp : sortedWarps) {
                String name = warp.name;
                String creator = player != null ? (warp.creator
                        .equalsIgnoreCase(player.getName()) ? LanguageManager
                        .getColorlessString("list.you") : warp.creator)
                        : warp.creator;
                int x = (int) Math.round(warp.x);
                int y = warp.y;
                int z = (int) Math.round(warp.z);
                String color;
                if (player != null && warp.playerIsCreator(player.getName())) {
                    color = ChatColor.AQUA.toString();
                } else if (warp.publicAll) {
                    color = ChatColor.GREEN.toString();
                } else {
                    color = ChatColor.RED.toString();
                }

                String location = " @(" + x + ", " + y + ", " + z + ")";
                String creatorString = (warp.publicAll ? "(+)" : "(-)") + " "
                        + LanguageManager.getColorlessString("list.by") + " "
                        + creator;

                // Find remaining length left
                int left = MinecraftFontWidthCalculator.getStringWidth(intro)
                        - MinecraftFontWidthCalculator.getStringWidth("''"
                                + creatorString + location);

                int nameLength = MinecraftFontWidthCalculator
                        .getStringWidth(name);
                if (left > nameLength) {
                    name = "'" + name + "'" + ChatColor.WHITE + creatorString
                            + whitespace(left - nameLength);
                } else if (left < nameLength) {
                    name = "'" + substring(name, left) + "'" + ChatColor.WHITE
                            + creatorString;
                }

                executor.sendMessage(color + name + location);
            }
        }
    }

    /**
     * Lob shit off that string till it fits.
     */
    private String substring(String name, int left) {
        while (MinecraftFontWidthCalculator.getStringWidth(name) > left) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    public int getMaxPages(Player player) {
        return (int) Math.ceil(warpList.getMaxWarps(player)
                / (double) WARPS_PER_PAGE);
    }

    public int getMaxPagesPerCreator(Player player, String warpCreator) {
        String matchingWarpCreator = warpList.getMatchingCreator(player,
                warpCreator);
        return (int) Math.ceil(warpList.getMaxWarpsPerCreator(player,
                matchingWarpCreator) / (double) WARPS_PER_PAGE);
    }

    public String whitespace(int length) {
        int spaceWidth = MinecraftFontWidthCalculator.getStringWidth(" ");

        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < length; i += spaceWidth) {
            ret.append(" ");
        }

        return ret.toString();
    }
}

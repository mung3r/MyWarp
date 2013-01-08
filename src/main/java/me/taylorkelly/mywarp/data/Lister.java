package me.taylorkelly.mywarp.data;

import java.util.ArrayList;

import me.taylorkelly.mywarp.LanguageManager;

import org.angelsl.minecraft.randomshit.fontwidth.MinecraftFontWidthCalculator;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * List specific warps
 */
public class Lister {
    private WarpList warpList;
    private CommandSender executor;
    private Player player;

    private int maxPages;
    private int page;
    private String creator;
    private ArrayList<Warp> sortedWarps;

    private static final int WARPS_PER_PAGE = 8;

    public Lister(CommandSender executor, String creator, int page,
            WarpList warpList) {
        this.executor = executor;
        this.creator = creator;
        this.page = page;
        this.warpList = warpList;
    }

    /**
     * Lists the warps and send results to the executor
     */
    public void listWarps() {
        if (page < 1) {
            executor.sendMessage(LanguageManager
                    .getString("list.page.negative"));
            return;
        }

        player = null;
        if (executor instanceof Player) {
            player = (Player) executor;
        }
        maxPages = (int) Math.ceil(warpList.getMaxWarps(player, creator)
                / (double) WARPS_PER_PAGE);
        if (maxPages == 0) {
            executor.sendMessage(LanguageManager.getString("list.noWarps"));
            return;
        }
        if (page > maxPages) {
            executor.sendMessage(LanguageManager.getString("list.page.toHigh")
                    .replaceAll("%pages%", Integer.toString(maxPages)));
            return;
        }

        if (creator != null) {
            creator = warpList.getMatchingCreator(player, creator);
        }
        int start = (page - 1) * WARPS_PER_PAGE;
        sortedWarps = warpList.getSortedWarps(player, creator, start,
                WARPS_PER_PAGE);

        String intro = "------------------- "
                + LanguageManager.getColorlessString("list.page") + " " + page
                + "/" + maxPages + " -------------------";

        // send results to the executor
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

            int nameLength = MinecraftFontWidthCalculator.getStringWidth(name);
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

    private String substring(String str, int left) {
        while (MinecraftFontWidthCalculator.getStringWidth(str) > left) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private String whitespace(int length) {
        int spaceWidth = MinecraftFontWidthCalculator.getStringWidth(" ");
        StringBuilder ret = new StringBuilder();

        for (int i = 0; i < length; i += spaceWidth) {
            ret.append(" ");
        }
        return ret.toString();
    }
}

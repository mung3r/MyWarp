package me.taylorkelly.mywarp.data;

import java.util.TreeSet;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.utils.MinecraftFontWidthCalculator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * List specific warps
 */
public class Lister {
    private WarpList warpList;
    private CommandSender sender;
    private Player player;

    private int maxPages;
    private int page;
    private String creator;
    private TreeSet<Warp> sortedWarps;

    private static final int WARPS_PER_PAGE = 8;

    public Lister(CommandSender sender, String creator, int page,
            WarpList warpList) {
        this.sender = sender;
        this.creator = creator;
        this.page = page;
        this.warpList = warpList;
    }

    /**
     * Lists the warps and send results to the executor
     */
    public void listWarps() {
        if (page < 1) {
            sender.sendMessage(LanguageManager.getString("list.page.negative"));
            return;
        }
        player = sender instanceof Player ? (Player) sender : null;
        creator = creator != null ? warpList
                .getMatchingCreator(player, creator) : null;

        maxPages = (int) Math.ceil(warpList.getMaxWarps(player, creator)
                / (double) WARPS_PER_PAGE);
        if (maxPages == 0) {
            sender.sendMessage(LanguageManager.getString("list.noWarps"));
            return;
        }
        if (page > maxPages) {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "list.page.toHigh", "%pages%", Integer.toString(maxPages)));
            return;
        }

        int start = (page - 1) * WARPS_PER_PAGE;
        sortedWarps = warpList.getSortedWarps(player, creator, start,
                WARPS_PER_PAGE);

        String intro = "------------------- "
                + LanguageManager.getColorlessString("list.page") + " " + page
                + "/" + maxPages + " -------------------";

        // send results to the sender
        sender.sendMessage(ChatColor.YELLOW + intro);
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
            sender.sendMessage(color + name + location);
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

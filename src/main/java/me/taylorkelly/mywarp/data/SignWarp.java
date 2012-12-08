package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class SignWarp {

    /**
     * Precondition: Only call if isSignWarp() returned true
     */
    public static void warpSign(Sign sign, WarpList list, Player player) {
        String name = sign.getLine(2);
        Warp warp = list.getWarp(name);

        if (!warp.playerCanWarp(player)) {
            player.sendMessage(LanguageManager.getString(
                    "error.noPermission.warpto").replaceAll("%warp%", name));
            return;
        }
        if (WarpSettings.worldAccess
                && !list.playerCanAccessWorld(player, warp.world)) {
            player.sendMessage(LanguageManager.getString(
                    "error.noPermission.world").replaceAll("%world%",
                    warp.world));
            return;
        }
        list.warpTo(name, player);
    }

    public static void createSignWarp(SignChangeEvent sign) {
        sign.setLine(1, "[MyWarp]");
    }

    public static boolean isSignWarp(Sign sign) {
        return sign.getLine(1).equals("[MyWarp]");
    }

    public static boolean isSignWarp(SignChangeEvent sign) {
        return sign.getLine(1).equalsIgnoreCase("[MyWarp]");
    }
}
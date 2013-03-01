package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.WarpSettings;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class SignWarp {

    public static void warpSign(Sign sign, WarpList list, Player player) {
        String name = sign.getLine(2);

        if (!list.warpExists(name)) {
            player.sendMessage(LanguageManager.getEffectiveString(
                    "error.noSuchWarp", "%warp%", name));
            return;
        }
        Warp warp = list.getWarp(name);

        if (!warp.playerCanWarp(player)) {
            player.sendMessage(LanguageManager.getEffectiveString(
                    "error.noPermission.warpto", "%warp%", name));
            return;
        }
        if (WarpSettings.worldAccess
                && !list.playerCanAccessWorld(player, warp.world)) {
            player.sendMessage(LanguageManager.getEffectiveString(
                    "error.noPermission.world", "%world%", warp.world));
            return;
        }
        list.warpTo(warp, player);
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
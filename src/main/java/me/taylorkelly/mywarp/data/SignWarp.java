package me.taylorkelly.mywarp.data;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class SignWarp {
    
    /**
     * Precondition: Only call if isSignWarp() returned true
     */
    public static void warpSign(Sign sign, WarpList list, Player player) {
        list.warpTo(sign.getLine(2), player);
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

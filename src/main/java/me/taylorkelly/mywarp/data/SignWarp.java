package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.economy.Fee;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class SignWarp {

    private MyWarp plugin;

    private static final String SIGN_TEXT = "[MyWarp]";

    public SignWarp(MyWarp plugin) {
        this.plugin = plugin;
    }

    public void warpSign(Sign sign, Player player) {
        if (!MyWarp.getWarpPermissions().useSignWarp(player)) {
            player.sendMessage(LanguageManager
                    .getString("sign.noPermission.use"));
            return;
        }

        String name = sign.getLine(2);

        if (!plugin.getWarpList().warpExists(name)) {
            player.sendMessage(ChatColor.RED
                    + LanguageManager.getEffectiveString("error.noSuchWarp",
                            "%warp%", name));
            return;
        }
        Warp warp = plugin.getWarpList().getWarp(name);

        if (!warp.playerCanWarp(player)) {
            player.sendMessage(ChatColor.RED
                    + LanguageManager.getEffectiveString(
                            "error.noPermission.warpto", "%warp%", name));
            return;
        }
        if (WarpSettings.worldAccess
                && !plugin.getWarpList().playerCanAccessWorld(player,
                        warp.world)) {
            player.sendMessage(ChatColor.RED
                    + LanguageManager.getEffectiveString(
                            "error.noPermission.world", "%world%", warp.world));
            return;
        }

        if (WarpSettings.useEconomy) {
            double fee = MyWarp.getWarpPermissions().getEconomyPrices(player)
                    .getFee(Fee.WARP_SIGN_USE);
            if (!plugin.getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + LanguageManager.getEffectiveString(
                                "error.economy.cannotAfford", "%amount%",
                                Double.toString(fee)));
                return;
            }
            plugin.getEconomyLink().withdrawSender(player, fee);
        }
        plugin.getWarpList().warpTo(warp, player);
    }

    public boolean createSignWarp(SignChangeEvent sign, Player player) {
        if (!MyWarp.getWarpPermissions().createSignWarp(player)) {
            player.sendMessage(LanguageManager
                    .getString("sign.noPermission.create"));
            return false;
        }
        String name = sign.getLine(2);

        if (!plugin.getWarpList().warpExists(name)) {
            player.sendMessage(LanguageManager.getEffectiveString(
                    "error.noSuchWarp", "%warp%", name));
            return false;
        }
        Warp warp = plugin.getWarpList().getWarp(name);

        if (!warp.playerCanModify(player)
                && !MyWarp.getWarpPermissions().createSignWarpAll(player)) {
            player.sendMessage(LanguageManager.getEffectiveString(
                    "sign.noPermission.create", "%warp%", name));
            return false;
        }

        if (WarpSettings.useEconomy) {
            double fee = MyWarp.getWarpPermissions().getEconomyPrices(player)
                    .getFee(Fee.WARP_SIGN_CREATE);
            if (!plugin.getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + LanguageManager.getEffectiveString(
                                "error.economy.cannotAfford", "%amount%",
                                Double.toString(fee)));
                return false;
            }
            plugin.getEconomyLink().withdrawSender(player, fee);
        }

        sign.setLine(1, SIGN_TEXT);
        player.sendMessage(LanguageManager.getString("sign.created"));
        return true;
    }

    public boolean isSignWarp(Sign sign) {
        return isSignWarp(sign.getLines());
    }

    public boolean isSignWarp(String[] lines) {
        return lines[1].equalsIgnoreCase(SIGN_TEXT);
    }
}
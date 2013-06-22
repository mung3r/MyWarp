package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.Fee;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Contains several help methods that allow dealing with warp signs
 */
public class WarpSignUtils {

    private static final String SIGN_TEXT = "[MyWarp]";

    /**
     * Warps the given player to the warp noted on the given sign. This method
     * expects that the given sign is a valid warp sign!
     * 
     * @param sign
     *            the sign with the warp
     * @param player
     *            the player who should be teleported
     */
    public static void warpFromSign(Sign sign, Player player) {
        if (!MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.warp.sign.use")) {
            player.sendMessage(MyWarp.inst().getLanguageManager()
                    .getString("sign.noPermission.use"));
            return;
        }

        String name = sign.getLine(2);

        if (!MyWarp.inst().getWarpManager().warpExists(name)) {
            player.sendMessage(ChatColor.RED
                    + MyWarp.inst()
                            .getLanguageManager()
                            .getEffectiveString("error.noSuchWarp", "%warp%",
                                    name));
            return;
        }
        Warp warp = MyWarp.inst().getWarpManager().getWarp(name);

        if (!warp.playerCanWarp(player)) {
            player.sendMessage(ChatColor.RED
                    + MyWarp.inst()
                            .getLanguageManager()
                            .getEffectiveString("error.noPermission.warpto",
                                    "%warp%", name));
            return;
        }

        if (MyWarp.inst().getWarpSettings().useEconomy) {
            double fee = MyWarp.inst().getPermissionsManager()
                    .getEconomyPrices(player).getFee(Fee.WARP_SIGN_USE);
            if (!MyWarp.inst().getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + MyWarp.inst()
                                .getLanguageManager()
                                .getEffectiveString(
                                        "error.economy.cannotAfford",
                                        "%amount%", Double.toString(fee)));
                return;
            }
            MyWarp.inst().getEconomyLink().withdrawSender(player, fee);
        }
        MyWarp.inst().getWarpManager().warpTo(warp, player, false);
    }

    /**
     * Validates a warp sign, taken from the given sign change event. This
     * method expects that the given event belongs to a valid warp sign!
     * 
     * @param sign
     *            the sign change event
     * @param player
     *            the player who created the warp sign
     * @return true if the sign could be created, false if not.
     */
    public static boolean validateWarpSign(SignChangeEvent sign, Player player) {
        if (!MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.warp.sign.create")) {
            player.sendMessage(MyWarp.inst().getLanguageManager()
                    .getString("sign.noPermission.create"));
            return false;
        }
        String name = sign.getLine(2);

        if (!MyWarp.inst().getWarpManager().warpExists(name)) {
            player.sendMessage(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.noSuchWarp", "%warp%", name));
            return false;
        }
        Warp warp = MyWarp.inst().getWarpManager().getWarp(name);

        if (!warp.playerCanModify(player)
                && !MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.warp.sign.create.all")) {
            player.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("sign.noPermission.create", "%warp%",
                            name));
            return false;
        }

        if (MyWarp.inst().getWarpSettings().useEconomy) {
            double fee = MyWarp.inst().getPermissionsManager()
                    .getEconomyPrices(player).getFee(Fee.WARP_SIGN_CREATE);
            if (!MyWarp.inst().getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + MyWarp.inst()
                                .getLanguageManager()
                                .getEffectiveString(
                                        "error.economy.cannotAfford",
                                        "%amount%", Double.toString(fee)));
                return false;
            }
            MyWarp.inst().getEconomyLink().withdrawSender(player, fee);
        }

        sign.setLine(1, SIGN_TEXT);
        player.sendMessage(MyWarp.inst().getLanguageManager()
                .getString("sign.created"));
        return true;
    }

    /**
     * Calls {@link #isSignWarp(String[])} to determine if the given sign is a
     * warp sign.
     * 
     * @param sign
     *            the sign to check
     * @return true if the sign is a warp sign, false if not
     */
    public static boolean isSignWarp(Sign sign) {
        return isSignWarp(sign.getLines());
    }

    /**
     * Determines if the given array of lines belongs to a warp sign.
     * 
     * @param lines
     *            an array with the lines of the sign
     * @return true if the sign is a warp sign, false if not
     */
    public static boolean isSignWarp(String[] lines) {
        return lines[1].equalsIgnoreCase(SIGN_TEXT);
    }
}
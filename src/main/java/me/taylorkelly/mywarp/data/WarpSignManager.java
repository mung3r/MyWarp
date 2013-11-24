package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.Fee;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Attachable;

/**
 * Manages warp-signs
 */
public class WarpSignManager implements Listener {

    /**
     * Called whenever a sign is changed
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (isSignWarp(event.getLines())) {
            if (!validateWarpSign(event, event.getPlayer())) {
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }

    /**
     * Called whenever a player interacts with a block
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();

            if (block.getState() instanceof Sign && isSignWarp((Sign) block.getState())) {

                warpFromSign((Sign) block.getState(), event.getPlayer());
                event.setCancelled(true);

            } else if (block.getType() == Material.STONE_BUTTON || block.getType() == Material.WOOD_BUTTON
                    || block.getType() == Material.LEVER) {

                Attachable attachable = (Attachable) block.getState().getData();
                Block behind = block.getRelative(attachable.getAttachedFace(), 2);

                if (!(behind.getState() instanceof Sign)) {
                    return;
                }

                org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) behind.getState().getData();
                Sign signBut = (Sign) behind.getState();

                if (!(signMat.getFacing() == attachable.getAttachedFace() && isSignWarp(signBut))) {
                    return;
                }

                warpFromSign(signBut, event.getPlayer());
            }
        } else if (event.getAction().equals(Action.PHYSICAL)) {
            if (event.getClickedBlock().getType() == Material.WOOD_PLATE
                    || event.getClickedBlock().getType() == Material.STONE_PLATE) {
                Block twoBelow = event.getClickedBlock().getRelative(BlockFace.DOWN, 2);

                if (!(twoBelow.getState() instanceof Sign)) {
                    return;
                }
                Sign signBelow = (Sign) twoBelow.getState();

                if (!(isSignWarp(signBelow))) {
                    return;
                }
                warpFromSign(signBelow, event.getPlayer());
            }
        }
    }

    /**
     * Warps the given player to the warp noted on the given sign. This method
     * expects that the given sign is a valid warp sign!
     * 
     * @param sign
     *            the sign with the warp
     * @param player
     *            the player who should be teleported
     */
    public void warpFromSign(Sign sign, final Player player) {
        if (!MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.warp.sign.use")) {
            player.sendMessage(MyWarp.inst().getLanguageManager().getString("sign.noPermission.use", player));
            return;
        }

        String name = sign.getLine(2);

        if (!MyWarp.inst().getWarpManager().warpExists(name)) {
            player.sendMessage(ChatColor.RED
                    + MyWarp.inst().getLanguageManager()
                            .getEffectiveString("error.noSuchWarp", player, "%warp%", name));
            return;
        }
        final Warp warp = MyWarp.inst().getWarpManager().getWarp(name);

        if (!warp.playerCanWarp(player)) {
            player.sendMessage(ChatColor.RED
                    + MyWarp.inst().getLanguageManager()
                            .getEffectiveString("sign.noPermission.warpto", player, "%warp%", name));
            return;
        }

        if (MyWarp.inst().getWarpSettings().economyEnabled) {
            double fee = MyWarp.inst().getPermissionsManager().getEconomyPrices(player)
                    .getFee(Fee.WARP_SIGN_USE);
            if (!MyWarp.inst().getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + MyWarp.inst()
                                .getLanguageManager()
                                .getEffectiveString("error.economy.cannotAfford", player, "%amount%",
                                        Double.toString(fee)));
                return;
            }
            MyWarp.inst().getEconomyLink().withdrawSender(player, fee);
        }

        // workaround for BUKKIT-4365
        if (!sign.getWorld().getName().equals(warp.getWorld())) {
            MyWarp.server().getScheduler().scheduleSyncDelayedTask(MyWarp.inst(), new Runnable() {

                @Override
                public void run() {
                    warp.warp(player, false);
                }

            }, 1L);
        } else {
            warp.warp(player, false);
        }

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
    public boolean validateWarpSign(SignChangeEvent sign, Player player) {
        if (!MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.warp.sign.create")) {
            player.sendMessage(MyWarp.inst().getLanguageManager().getString("sign.noPermission.create", player));
            return false;
        }
        String name = sign.getLine(2);

        if (!MyWarp.inst().getWarpManager().warpExists(name)) {
            player.sendMessage(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.noSuchWarp", player, "%warp%", name));
            return false;
        }
        Warp warp = MyWarp.inst().getWarpManager().getWarp(name);

        if (!warp.playerCanModify(player)
                && !MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.warp.sign.create.all")) {
            player.sendMessage(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("sign.noPermission.create", player, "%warp%", name));
            return false;
        }

        if (MyWarp.inst().getWarpSettings().economyEnabled) {
            double fee = MyWarp.inst().getPermissionsManager().getEconomyPrices(player)
                    .getFee(Fee.WARP_SIGN_CREATE);
            if (!MyWarp.inst().getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + MyWarp.inst()
                                .getLanguageManager()
                                .getEffectiveString("error.economy.cannotAfford", player, "%amount%",
                                        Double.toString(fee)));
                return false;
            }
            MyWarp.inst().getEconomyLink().withdrawSender(player, fee);
        }

        // get the right spelling (case) out of the config
        String line = sign.getLine(1);
        line = line.substring(1, line.length() - 1);
        sign.setLine(1, "[" + MyWarp.inst().getWarpSettings().warpSignsIdentifiers.ceiling(line) + "]");

        player.sendMessage(MyWarp.inst().getLanguageManager().getString("sign.created", player));
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
    public boolean isSignWarp(Sign sign) {
        return isSignWarp(sign.getLines());
    }

    /**
     * Determines if the given array of lines belongs to a warp sign.
     * 
     * @param lines
     *            an array with the lines of the sign
     * @return true if the sign is a warp sign, false if not
     */
    public boolean isSignWarp(String[] lines) {
        return lines[1].startsWith("[")
                && lines[1].endsWith("]")
                && MyWarp.inst().getWarpSettings().warpSignsIdentifiers.contains(lines[1].substring(1,
                        lines[1].length() - 1));
    }
}
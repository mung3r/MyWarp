package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.SignWarp;
import me.taylorkelly.mywarp.data.WarpList;
import me.taylorkelly.mywarp.permissions.WarpPermissions;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Attachable;

public class MWPlayerListener implements Listener {
    private WarpList warpList;
    private WarpPermissions warpPermissions;

    public MWPlayerListener(MyWarp plugin) {
        warpList = plugin.getWarpList();
        warpPermissions = MyWarp.getWarpPermissions();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();

            if (block.getState() instanceof Sign
                    && SignWarp.isSignWarp((Sign) block.getState())) {

                if (!warpPermissions.signWarp(event.getPlayer())) {
                    event.getPlayer().sendMessage(
                            LanguageManager.getString("sign.noPermission.use"));
                    return;
                }
                SignWarp.warpSign((Sign) block.getState(), this.warpList,
                        event.getPlayer());

            } else if (block.getType() == Material.STONE_BUTTON
                    || block.getType() == Material.WOOD_BUTTON
                    || block.getType() == Material.LEVER) {

                Attachable attachable = (Attachable) block.getState().getData();
                Block behind = block.getRelative(attachable.getAttachedFace(), 2);

                if (!(behind.getState() instanceof Sign)) {
                    return;
                }

                org.bukkit.material.Sign signMat = (org.bukkit.material.Sign) behind
                        .getState().getData();
                Sign signBut = (Sign) behind.getState();

                if (!(signMat.getFacing() == attachable.getAttachedFace() && SignWarp
                        .isSignWarp(signBut))) {
                    return;
                }

                if (!warpPermissions.signWarp(event.getPlayer())) {
                    event.getPlayer().sendMessage(
                            LanguageManager.getString("sign.noPermission.use"));
                    return;
                }
                SignWarp.warpSign(signBut, this.warpList, event.getPlayer());
            }
        } else if (event.getAction().equals(Action.PHYSICAL)) {
            if (event.getClickedBlock().getType() == Material.WOOD_PLATE
                    || event.getClickedBlock().getType() == Material.STONE_PLATE) {
                Block twoBelow = event.getClickedBlock().getRelative(BlockFace.DOWN, 2);

                if (!(twoBelow.getState() instanceof Sign)) {
                    return;
                }
                Sign signBelow = (Sign) twoBelow.getState();

                if (!(SignWarp.isSignWarp(signBelow))) {
                    return;
                }

                if (!warpPermissions.signWarp(event.getPlayer())) {
                    event.getPlayer().sendMessage(
                            LanguageManager.getString("sign.noPermission.use"));
                    return;
                }
                SignWarp.warpSign(signBelow, this.warpList, event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.warpList.waitingForWelcome(player)) {
            this.warpList.setWelcomeMessage(player, event.getMessage());
            this.warpList.notWaiting(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || !WarpSettings.abortOnMove) {
            return;
        }

        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }

        Player player = event.getPlayer();
        if (PlayerWarmup.isActive(player.getName())
                && !warpPermissions.disobeyWarmupMoveAbort(player)) {
            PlayerWarmup.endTimer(player.getName());
            player.sendMessage(LanguageManager.getString("timer.warmup.canceled.move"));
        }
    }
}
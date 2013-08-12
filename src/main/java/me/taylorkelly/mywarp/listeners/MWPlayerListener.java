package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.WarpSignUtils;

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
import org.bukkit.material.Attachable;

public class MWPlayerListener implements Listener {

    /**
     * Called whenever a player interacts with a block
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!MyWarp.inst().getWarpSettings().warpSignsEnabled) {
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();

            if (block.getState() instanceof Sign && WarpSignUtils.isSignWarp((Sign) block.getState())) {

                WarpSignUtils.warpFromSign((Sign) block.getState(), event.getPlayer());
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

                if (!(signMat.getFacing() == attachable.getAttachedFace() && WarpSignUtils
                        .isSignWarp(signBut))) {
                    return;
                }

                WarpSignUtils.warpFromSign(signBut, event.getPlayer());
            }
        } else if (event.getAction().equals(Action.PHYSICAL)) {
            if (event.getClickedBlock().getType() == Material.WOOD_PLATE
                    || event.getClickedBlock().getType() == Material.STONE_PLATE) {
                Block twoBelow = event.getClickedBlock().getRelative(BlockFace.DOWN, 2);

                if (!(twoBelow.getState() instanceof Sign)) {
                    return;
                }
                Sign signBelow = (Sign) twoBelow.getState();

                if (!(WarpSignUtils.isSignWarp(signBelow))) {
                    return;
                }
                WarpSignUtils.warpFromSign(signBelow, event.getPlayer());
            }
        }
    }

    /**
     * Called when a player sends a chat message. Asynchronous, called methods
     * must be threadsafe.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (MyWarp.inst().getWarpManager().waitingForWelcome(player)) {
            MyWarp.inst().getWarpManager().setWelcomeMessage(player, event.getMessage());
            MyWarp.inst().getWarpManager().notWaiting(player);
            event.setCancelled(true);
        }
    }
}
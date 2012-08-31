package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.SignWarp;
import me.taylorkelly.mywarp.data.WarpList;
import me.taylorkelly.mywarp.permissions.WarpPermissions;
import me.taylorkelly.mywarp.timer.PlayerTimer;
import me.taylorkelly.mywarp.timer.Warmup;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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
            if (((block.getState() instanceof Sign))
                    && (SignWarp.isSignWarp((Sign) block.getState()))
                    && (warpPermissions.signWarp(event.getPlayer())))
                SignWarp.warpSign((Sign) block.getState(), this.warpList,
                        event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (this.warpList.waitingForWelcome(player)) {
            this.warpList.setWelcomeMessage(player, event.getMessage());
            this.warpList.notWaiting(player);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (WarpSettings.loadChunks) {
            World world = event.getPlayer().getWorld();
            Chunk chunk = world.getChunkAt(event.getTo());
            int x = chunk.getX();
            int z = chunk.getZ();
            world.refreshChunk(x, z);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;

        if (WarpSettings.abortOnMove) {
            Player player = event.getPlayer();
            Warmup warmup = MyWarp.getWarpPermissions().getWarmup(player);
            if (PlayerTimer.isActive(player.getName(), warmup)) {
                Location fromLoc = event.getFrom();
                Location toLoc = event.getTo();
                if (fromLoc.getBlockX() != toLoc.getBlockX() || fromLoc.getBlockY() != toLoc.getBlockY() || fromLoc.getBlockZ() != toLoc.getBlockZ()) {
                    PlayerTimer.endTimer(player.getName(), warmup);
                    player.sendMessage(ChatColor.RED
                            + " You mustn't move while warming up. Your "
                            + ChatColor.RESET + "/warp" + ChatColor.RED
                            + " was canceled.");
                }
            }
        }
    }
}
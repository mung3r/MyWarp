package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.SignWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.WarpList;
import me.taylorkelly.mywarp.permissions.WarpPermissions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class MWBlockListener implements Listener {
    private WarpPermissions warpPermissions;
    private WarpList warpList;

    public MWBlockListener(MyWarp plugin) {
        warpPermissions = MyWarp.getWarpPermissions();
        warpList = plugin.getWarpList();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (SignWarp.isSignWarp(event))
            if (warpPermissions.createSignWarp(player)) {
                String name = event.getLine(2);

                if (!warpList.warpExists(name)) {
                    player.sendMessage(ChatColor.RED + "No such warp '" + name + "'");
                    event.setCancelled(true);
                }
                Warp warp = warpList.getWarp(name);

                if (!warp.playerCanModify(player)) {
                    player.sendMessage(ChatColor.RED
                            + "You do not have permission to create a SignWarp to '"
                            + name + "'");
                    event.setCancelled(true);
                }
                SignWarp.createSignWarp(event);
                player.sendMessage(ChatColor.AQUA + "Successfully created a SignWarp");
            } else {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to create a SignWarp.");
                event.setCancelled(true);
            }
    }
}
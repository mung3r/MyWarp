package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.SignWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.WarpList;
import me.taylorkelly.mywarp.permissions.WarpPermissions;

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
        if (SignWarp.isSignWarp(event)) {
            Player player = event.getPlayer();
            if (warpPermissions.createSignWarp(player)) {
                String name = event.getLine(2);

                if (!warpList.warpExists(name)) {
                    player.sendMessage(LanguageManager.getString(
                            "error.noSuchWarp").replaceAll("%warp%", name));
                    event.setCancelled(true);
                    event.getBlock().breakNaturally();
                    return;
                }
                Warp warp = warpList.getWarp(name);

                if (!warp.playerCanModify(player)) {
                    player.sendMessage(LanguageManager.getString(
                            "sign.noPermission.create").replaceAll("%warp%",
                            name));
                    event.setCancelled(true);
                    event.getBlock().breakNaturally();
                    return;
                }
                SignWarp.createSignWarp(event);
                player.sendMessage(LanguageManager.getString("sign.created"));
            } else {
                player.sendMessage(LanguageManager
                        .getString("sign.noPermission.create"));
                event.setCancelled(true);
                event.getBlock().breakNaturally();
                return;
            }
        }
    }
}
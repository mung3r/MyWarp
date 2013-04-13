package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class MWBlockListener implements Listener {

    private final MyWarp plugin;

    public MWBlockListener(MyWarp plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        if (plugin.getSignWarp().isSignWarp(event.getLines())) {
            if (!plugin.getSignWarp().createSignWarp(event, event.getPlayer())) {
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }
}
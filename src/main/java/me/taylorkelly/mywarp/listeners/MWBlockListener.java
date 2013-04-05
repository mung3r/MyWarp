package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class MWBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        if (MyWarp.signWarp.isSignWarp(event.getLines())) {
            if (!MyWarp.signWarp.createSignWarp(event, event.getPlayer())) {
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }
}
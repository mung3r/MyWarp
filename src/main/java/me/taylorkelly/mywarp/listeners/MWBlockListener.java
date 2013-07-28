package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.data.WarpSignUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class MWBlockListener implements Listener {

    /**
     * Called whenever a sign is changed
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (WarpSignUtils.isSignWarp(event.getLines())) {
            if (!WarpSignUtils.validateWarpSign(event, event.getPlayer())) {
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }
}
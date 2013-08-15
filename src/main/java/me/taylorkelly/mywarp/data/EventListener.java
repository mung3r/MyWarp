package me.taylorkelly.mywarp.data;

import me.taylorkelly.mywarp.MyWarp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * MyWarps listener for non-optional events
 */
public class EventListener implements Listener {

    /**
     * Called when a player sends a chat message. Asynchronous, called methods
     * MUST be threadsafe.
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
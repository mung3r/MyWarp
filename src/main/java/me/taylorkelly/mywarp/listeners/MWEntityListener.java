package me.taylorkelly.mywarp.listeners;

import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MWEntityListener implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player)
                || !WarpSettings.abortOnDamage) {
            return;
        }

        Player victim = (Player) event.getEntity();

        if (PlayerWarmup.isActive(victim.getName())) {
            PlayerWarmup.endTimer(victim.getName());
            victim.sendMessage(ChatColor.RED
                    + " You mustn't take damage while warming up. Your "
                    + ChatColor.RESET + "/warp" + ChatColor.RED + " was canceled.");
        }
    }
}

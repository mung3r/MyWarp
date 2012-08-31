package me.taylorkelly.mywarp.timer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.WarpSettings;

public class PlayerCooldown implements Runnable {

    Player player;

    public PlayerCooldown(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        if (WarpSettings.coolDownNotify) {
            player.sendMessage(ChatColor.AQUA
                    + "You have cooled down, feel free to use " + ChatColor.RESET
                    + "/warp" + ChatColor.AQUA + " again.");
        }

    }
}

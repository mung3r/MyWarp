package me.taylorkelly.mywarp.timer;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.scheduler.Scheduler;

public class PlayerWarmup implements Runnable {

    private MyWarp plugin;
    private Player player;
    private Cooldown cooldown;
    private Warp warp;
    private Server server;

    public PlayerWarmup(MyWarp plugin, Player player, Cooldown cooldown, Warp warp,
            Server server) {
        this.plugin = plugin;
        this.player = player;
        this.cooldown = cooldown;
        this.warp = warp;
        this.server = server;
    }

    @Override
    public void run() {
        if (warp.warp(player, server)) {
            player.sendMessage(ChatColor.AQUA + warp.getSpecificWelcomeMessage(player));
        }
        if (!MyWarp.getWarpPermissions().disobeyCooldown(player)) {
            Scheduler.schedulePlayerTimer(Scheduler.playerCooldown(plugin, player,
                    cooldown));
        }
    }
}

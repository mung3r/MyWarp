package me.taylorkelly.mywarp.timer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.MyWarp;

public class PlayerWarmup extends PlayerTimer {

    final private MyWarp plugin;
    final private Time cooldown;
    final private String warp;

    public static Map<String, PlayerTimer> warmups = new HashMap<String, PlayerTimer>();

    public PlayerWarmup(MyWarp plugin, Player player, Time durration,
            String warp, Time cooldown) {
        super(plugin, player, durration);
        this.plugin = plugin;
        this.cooldown = cooldown;
        this.warp = warp;
    }

    @Override
    public void run() {
        super.run();
        if (!player.isOnline()) {
            return;
        }
        plugin.getWarpList().warpTo(warp, player);
        if (!MyWarp.getWarpPermissions().disobeyCooldown(player)) {
            new PlayerCooldown(plugin, player, cooldown);
        }
    }

    @Override
    public Map<String, PlayerTimer> getTimerMap() {
        return warmups;
    }

    public static Integer getRemainingWarmup(String player) {
        PlayerTimer pw = warmups.get(player);
        if (pw != null) {
            return pw.getRemainingTime();
        }
        return 0;
    }

    public static Boolean isActive(String player) {
        return warmups.containsKey(player);
    }

    public static void endWarmup(String player) {
        PlayerTimer pw = warmups.get(player);
        if (pw != null) {
            pw.cancel();
        }
    }
}

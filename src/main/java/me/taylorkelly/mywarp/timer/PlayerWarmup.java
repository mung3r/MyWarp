package me.taylorkelly.mywarp.timer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

public class PlayerWarmup extends PlayerTimer {

    final private MyWarp plugin;
    final private Time cooldown;
    final private Warp warp;

    public static Map<String, PlayerTimer> warmups = new HashMap<String, PlayerTimer>();

    public PlayerWarmup(MyWarp plugin, Player player, Time durration,
            Warp warp, Time cooldown) {
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
        // even if the warp is removed while the warmup is running, the
        // garbage collector does not delete it until the warmup is over
        plugin.getWarpList().warpTo(warp, player);
        if (!MyWarp.warpPermissions.disobeyCooldown(player)) {
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

package me.taylorkelly.mywarp.timer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;

public class PlayerCooldown extends PlayerTimer {

    public PlayerCooldown(MyWarp plugin, Player player, Time durration) {
        super(plugin, player, durration);
    }

    public static Map<String, PlayerTimer> cooldowns = new HashMap<String, PlayerTimer>();

    @Override
    public void run() {
        super.run();
        if (!player.isOnline()) {
            return;
        }
        if (WarpSettings.coolDownNotify) {
            player.sendMessage(LanguageManager
                    .getString("timer.cooldown.ended"));
        }
    }

    @Override
    public Map<String, PlayerTimer> getTimerMap() {
        return cooldowns;
    }

    public static Integer getRemainingCooldown(String player) {
        PlayerTimer pc = cooldowns.get(player);
        if (pc != null) {
            return pc.getRemainingTime();
        }
        return 0;
    }

    public static Boolean isActive(String player) {
        return cooldowns.containsKey(player);
    }

    public static void endCooldown(String player) {
        PlayerTimer pc = cooldowns.get(player);
        if (pc != null) {
            pc.cancel();
        }
    }
}

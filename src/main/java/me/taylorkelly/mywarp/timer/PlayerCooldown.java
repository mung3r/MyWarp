package me.taylorkelly.mywarp.timer;

import java.util.HashMap;
import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.entity.Player;

/**
 * This class manages and acts as warp-cooldown for players
 */
public class PlayerCooldown extends PlayerTimer {

    /**
     * Initializes this cooldown
     * 
     * @param player
     *            the player the cooldown applies for
     * @param durration
     *            the durration of this cooldown
     */
    public PlayerCooldown(Player player, Time durration) {
        super(player, durration);
    }

    private static Map<String, PlayerTimer> cooldowns = new HashMap<String, PlayerTimer>();

    @Override
    public void run() {
        super.run();
        if (!player.isOnline()) {
            return;
        }
        if (MyWarp.inst().getWarpSettings().timersCooldownNotify) {
            player.sendMessage(MyWarp.inst().getLanguageManager().getString("timer.cooldown.ended"));
        }
    }

    @Override
    public Map<String, PlayerTimer> getTimerMap() {
        return cooldowns;
    }

    /**
     * Gets the remaining time on the cooldown of the given player. Will return
     * 0 if the player is not cooling down.
     * 
     * @param player
     *            the player#s name
     * @return the remaining time on the cooldown in seconds
     */
    public static Integer getRemainingCooldown(String player) {
        PlayerTimer pc = cooldowns.get(player);
        if (pc != null) {
            return pc.getRemainingTime();
        }
        return 0;
    }

    /**
     * Checks if the given player is coolding down
     * 
     * @param player
     *            the player's name
     * @return whether the player is cooling down
     */
    public static Boolean isActive(String player) {
        return cooldowns.containsKey(player);
    }

    /**
     * Ends the cooldown for the given player
     * 
     * @param player
     *            the player's name
     */
    public static void endCooldown(String player) {
        PlayerTimer pc = cooldowns.get(player);
        if (pc != null) {
            pc.cancel();
        }
    }
}

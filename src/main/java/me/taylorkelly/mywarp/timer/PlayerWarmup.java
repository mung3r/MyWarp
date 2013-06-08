package me.taylorkelly.mywarp.timer;

import java.util.HashMap;
import java.util.Map;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.bukkit.entity.Player;

/**
 * This class manages and acts as warp-warmups from players
 */
public class PlayerWarmup extends PlayerTimer {

    final private Time cooldown;
    final private Warp warp;

    private static Map<String, PlayerTimer> warmups = new HashMap<String, PlayerTimer>();

    /**
     * Initializes this player warmup
     * 
     * @param player
     *            the player this warmup applies for
     * @param durration
     *            the duration of this warmup
     * @param warp
     *            the warp that should be used after warming up
     * @param cooldown
     *            the cooldown that applies after the warp
     */
    public PlayerWarmup(Player player, Time durration, Warp warp, Time cooldown) {
        super(player, durration);
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
        MyWarp.inst().getWarpList().warpTo(warp, player);
        if (!MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.cooldown.disobey")) {
            new PlayerCooldown(player, cooldown);
        }
    }

    @Override
    public Map<String, PlayerTimer> getTimerMap() {
        return warmups;
    }

    /**
     * Gets the remaining time on the warmup of the given player. Will return 0
     * if the player is not warming up.
     * 
     * @param player
     *            the player#s name
     * @return the remaining time on the warmup in seconds
     */
    public static Integer getRemainingWarmup(String player) {
        PlayerTimer pw = warmups.get(player);
        if (pw != null) {
            return pw.getRemainingTime();
        }
        return 0;
    }

    /**
     * Checks if the given player is warming up
     * 
     * @param player
     *            the player's name
     * @return whether the player is warming up
     */
    public static Boolean isActive(String player) {
        return warmups.containsKey(player);
    }

    /**
     * Ends the warmup for the given player
     * 
     * @param player
     *            the player's name
     */
    public static void endWarmup(String player) {
        PlayerTimer pw = warmups.get(player);
        if (pw != null) {
            pw.cancel();
        }
    }
}

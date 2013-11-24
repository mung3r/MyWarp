package me.taylorkelly.mywarp.timer;

import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.timer.TimerFactory.TimerAction;

/**
 * Represents the action that takes place once a warp-cooldown is finished.
 */
public class WarpCooldown extends TimerAction<String> {

    /**
     * Initializes the warp-cooldown.
     * 
     * @param timerFactory the {@link TimerFactory} instance this action is registered on
     * @param player the player who is cooling down
     * @param duration the duration of the cooldown
     */
    public WarpCooldown(TimerFactory timerFactory, Player player, Time duration) {
        timerFactory.super(player.getName(), duration.getTicks());
    }

    @Override
    public void action() {
        if (MyWarp.inst().getWarpSettings().timersCooldownNotify) {
            Player player = MyWarp.server().getPlayerExact(type);

            if (player != null) {
                player.sendMessage(MyWarp.inst().getLanguageManager().getString("timer.cooldown.ended", player));
            }
        }

    }

}

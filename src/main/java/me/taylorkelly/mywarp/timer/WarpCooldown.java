package me.taylorkelly.mywarp.timer;

import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.entity.Player;

/**
 * A cooldown that blocks a player from teleporting to warps.
 */
public class WarpCooldown extends TimerAction<UUID> {

    /**
     * Initializes this WarpCooldown.
     * 
     * @param player
     *            the player who is cooling down
     */
    public WarpCooldown(Player player) {
        super(player.getUniqueId(), MyWarp.inst().getPermissionsManager().getTimeBundleManager()
                .getBundle(player).getTicks(TimeBundle.Time.WARP_COOLDOWN));
    }

    @Override
    public void action() {
        if (MyWarp.inst().getWarpSettings().timersCooldownNotify) {
            Player player = MyWarp.server().getPlayer(type);

            if (player != null) {
                player.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.warp-to.cooldown.ended", player));
            }
        }
    }

}

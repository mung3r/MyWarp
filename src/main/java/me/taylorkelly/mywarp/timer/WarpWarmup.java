package me.taylorkelly.mywarp.timer;

import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.FeeBundle;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A warmup that teleports a player to a warp when done.
 */
public class WarpWarmup extends TimerAction<UUID> {

    private static final int CHECK_FREQUENCY = 2;

    private final Warp warp;

    /**
     * Initializes the warp-warmup.
     * 
     * @param player
     *            the player who is cooling down
     * @param warp
     *            the warp that the player wants to use
     */
    public WarpWarmup(Player player, Warp warp) {
        super(player.getUniqueId(), MyWarp.inst().getPermissionsManager().getTimeBundleManager()
                .getBundle(player).getTicks(TimeBundle.Time.WARP_WARMUP));
        this.warp = warp;
    }

    @Override
    protected void run(TimerManager timerManager, Plugin plugin) {
        Player player = MyWarp.server().getPlayer(type);

        if (MyWarp.inst().getSettings().isTimersWarmupNotifyOnStart()) {
            player.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.warp-to.warmup.started", player, warp.getName(), duration / 20));
        }

        if (MyWarp.inst().getSettings().isTimersWarmupAbortOnDamage()) {
            new HealthCheck(player.getHealth()).runTaskTimer(MyWarp.inst(), 20 * CHECK_FREQUENCY,
                    20 * CHECK_FREQUENCY);
        }
        if (MyWarp.inst().getSettings().isTimersWarmupAbortOnMove()) {
            new MovementCheck(player.getLocation()).runTaskTimer(MyWarp.inst(), 20 * CHECK_FREQUENCY,
                    20 * CHECK_FREQUENCY);
        }
        super.run(timerManager, plugin);
    }

    @Override
    public void action() {
        Player player = MyWarp.server().getPlayer(type);
        if (player == null) {
            return;
        }

        if (MyWarp.inst().isEconomySetup()) {
            FeeBundle fees = MyWarp.inst().getPermissionsManager().getFeeBundleManager().getBundle(player);
            if (!fees.hasAtLeast(player, FeeBundle.Fee.WARP_TO)) {
                return;
            }
        }

        warp.teleport(player, FeeBundle.Fee.WARP_TO);
        MyWarp.inst().getTimerManager().registerNewTimer(new WarpCooldown(player));
    }

    /**
     * Runnable that checks if the player moves
     */
    private class MovementCheck extends PlayerCheck {

        private final Location originalLoc;

        public MovementCheck(Location originalLoc) {
            this.originalLoc = originalLoc;
        }

        @Override
        public boolean cancelAction(Player player) {
            Location loc = player.getLocation();
            if (loc.distanceSquared(originalLoc) >= 2 * 2) {
                // REVIEW permission logic
                if (!MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.timer.disobey.moveabort")) {
                    player.sendMessage(MyWarp.inst().getLocalizationManager()
                            .getString("commands.warp-to.warmup.cancelled-move", player));
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Runnable that checks if the player loses any health
     */
    private class HealthCheck extends PlayerCheck {

        private final double originalHealth;

        public HealthCheck(double originalHealth) {
            this.originalHealth = originalHealth;
        }

        @Override
        public boolean cancelAction(Player player) {
            if (player.getHealth() < originalHealth) {
                // REVIEW permission logic
                if (!MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.timer.disobey.dmgabort")) {
                    player.sendMessage(MyWarp.inst().getLocalizationManager()
                            .getString("commands.warp-to.warmup.cancelled-damage", player));
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * An abstract runnable that runs checks on a player
     */
    private abstract class PlayerCheck extends BukkitRunnable {

        public abstract boolean cancelAction(Player player);

        @Override
        public void run() {
            if (!MyWarp.inst().getTimerManager().hasRunningTimer(type, WarpWarmup.class)) {
                cancel();
                return;
            }
            Player player = MyWarp.server().getPlayer(type);
            if (player == null) {
                cancel();
                return;
            }
            if (cancelAction(player)) {
                WarpWarmup.this.cancel();
                cancel();
            }
        }
    }
}

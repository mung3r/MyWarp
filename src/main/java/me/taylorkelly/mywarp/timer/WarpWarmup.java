package me.taylorkelly.mywarp.timer;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.timer.TimerFactory.TimerAction;

/**
 * Represents the action that takes place once a warp-warmup is finished.
 */
public class WarpWarmup extends TimerAction<UUID> {

    private static final int CHECK_FREQUENCY = 2;

    private final Warp warp;
    private final Location originalLoc;
    private final double originalHealth;

    /**
     * Initializes the warp-warmup.
     * 
     * @param timerFactory
     *            the {@link TimerFactory} instance this action is registered on
     * @param player
     *            the player who is cooling down
     * @param warp
     *            the warp that the player wants to use
     * @param duration
     *            the duration of the warmup
     */
    public WarpWarmup(TimerFactory timerFactory, Player player, Warp warp, Time duration) {
        timerFactory.super(player.getUniqueId(), duration.getTicks());
        this.warp = warp;

        originalLoc = player.getLocation().clone();
        originalHealth = player.getHealth();

        if (MyWarp.inst().getWarpSettings().timersWarmupNotify) {
            player.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getString("commands.warp-to.warmup.started", player, warp.getName(),
                            duration.getSeconds()));
        }

        if (MyWarp.inst().getWarpSettings().timersAbortOnDamage) {
            new HealthCheck().runTaskTimer(MyWarp.inst(), 20 * CHECK_FREQUENCY, 20 * CHECK_FREQUENCY);
        }
        if (MyWarp.inst().getWarpSettings().timersAbortOnMove) {
            new MovementCheck().runTaskTimer(MyWarp.inst(), 20 * CHECK_FREQUENCY, 20 * CHECK_FREQUENCY);
        }
    }

    @Override
    public void action() {
        Player player = MyWarp.server().getPlayer(type);
        if (player == null) {
            return;
        }

        if (MyWarp.inst().getWarpSettings().economyEnabled) {
            double fee = MyWarp.inst().getPermissionsManager().getEconomyPrices(player).getFee(Fee.WARP_TO);

            if (!MyWarp.inst().getEconomyLink().canAfford(player, fee)) {
                player.sendMessage(ChatColor.RED
                        + MyWarp.inst().getLocalizationManager()
                                .getString("economy.cannot-afford", player, fee));
                return;
            }
        }

        warp.teleport(player, true);
        if (!MyWarp.inst().getPermissionsManager().hasPermission(player, "mywarp.cooldown.disobey")) {
            MyWarp.inst()
                    .getTimerFactory()
                    .registerNewTimer(
                            new WarpCooldown(MyWarp.inst().getTimerFactory(), player, MyWarp.inst()
                                    .getPermissionsManager().getCooldown(player)));
        }
    }

    /**
     * Runnable that checks if the player moves
     */
    private class MovementCheck extends BukkitRunnable {

        @Override
        public void run() {
            if (!MyWarp.inst().getTimerFactory().hasRunningTimer(type, WarpWarmup.class)) {
                cancel();
                return;
            }

            Player player = MyWarp.server().getPlayer(type);
            if (player == null) {
                cancel();
                return;
            }

            Location loc = player.getLocation();
            if (loc.distanceSquared(originalLoc) >= 2 * 2) {
                if (!MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.warmup.disobey.moveabort")) {
                    WarpWarmup.this.cancel();
                    player.sendMessage(MyWarp.inst().getLocalizationManager()
                            .getString("commands.warp-to.warmup.cancelled-move", player));
                    cancel();
                }
            }
        }
    }

    /**
     * Runnable that checks if the player loses any health
     */
    private class HealthCheck extends BukkitRunnable {

        @Override
        public void run() {
            if (!MyWarp.inst().getTimerFactory().hasRunningTimer(type, WarpWarmup.class)) {
                cancel();
                return;
            }

            Player player = MyWarp.server().getPlayer(type);
            if (player == null) {
                cancel();
                return;
            }

            if (player.getHealth() < originalHealth) {
                if (!MyWarp.inst().getPermissionsManager()
                        .hasPermission(player, "mywarp.warmup.disobey.dmgabort")) {
                    WarpWarmup.this.cancel();
                    player.sendMessage(MyWarp.inst().getLocalizationManager()
                            .getString("commands.warp-to.warmup.cancelled-damage", player));
                    cancel();
                }
            }
        }
    }
}

package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.timer.PlayerCooldown;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import me.taylorkelly.mywarp.timer.Time;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpToCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public WarpToCommand(MyWarp plugin) {
        super("WarpTo");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.warpTo"));
        setUsage("/warp §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("warp", "mywarp", "mw");
        setPermission("mywarp.warp.basic.warp");
    }

    @Override
    public boolean execute(final CommandSender executor, String identifier,
            final String[] args) {
        if (executor instanceof Player) {
            Player player = (Player) executor;
            String name = plugin.getWarpList().getMatche(
                    StringUtils.join(args, ' '), player);

            if (!plugin.getWarpList().warpExists(name)) {
                player.sendMessage(LanguageManager
                        .getString("error.noSuchWarp").replaceAll("%warp%",
                                name));
                return true;
            }

            Warp warp = plugin.getWarpList().getWarp(name);

            if (!warp.playerCanWarp(player)) {
                player.sendMessage(LanguageManager.getString(
                        "error.noPermission.warpto").replaceAll("%warp%", name));
                return true;
            }

            if (WarpSettings.worldAccess
                    && !plugin.getWarpList().playerCanAccessWorld(player,
                            warp.world)) {
                player.sendMessage(LanguageManager.getString(
                        "error.noPermission.world").replaceAll("%world%",
                        warp.world));
                return true;
            }

            if (WarpSettings.useTimers) {
                Time cooldown = MyWarp.getWarpPermissions().getCooldown(
                        player);
                Time warmup = MyWarp.getWarpPermissions().getWarmup(player);

                if (PlayerCooldown.isActive(player.getName())) {
                    player.sendMessage(LanguageManager.getString(
                            "timer.cooldown.cooling").replaceAll(
                            "%seconds%",
                            Integer.toString(PlayerCooldown
                                    .getRemainingCooldown(player.getName()))));
                    return true;
                }

                if (PlayerWarmup.isActive(player.getName())) {
                    player.sendMessage(LanguageManager.getString(
                            "timer.warmup.warming").replaceAll(
                            "%seconds%",
                            Integer.toString(PlayerWarmup
                                    .getRemainingWarmup(player.getName()))));
                    return true;
                }

                if (MyWarp.getWarpPermissions().disobeyWarmup(player)) {
                    plugin.getWarpList().warpTo(name, player);

                    if (!MyWarp.getWarpPermissions().disobeyCooldown(player)) {
                        new PlayerCooldown(plugin, player, cooldown);
                    }
                    return true;
                }

                new PlayerWarmup(plugin, player, warmup, name, cooldown);

                if (WarpSettings.warmUpNotify) {
                    player.sendMessage(LanguageManager
                            .getString("timer.warmup.warming")
                            .replaceAll("%warp%", name)
                            .replaceAll("%seconds%",
                                    Integer.toString(warmup.getInt())));
                }
                return true;

            } else {
                plugin.getWarpList().warpTo(name, player);
                return true;
            }
        } else {
            executor.sendMessage(LanguageManager
                    .getString("error.consoleSender.warpto"));
            return true;
        }
    }
}

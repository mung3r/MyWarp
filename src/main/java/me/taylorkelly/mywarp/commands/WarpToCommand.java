package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.scheduler.Scheduler;
import me.taylorkelly.mywarp.timer.Cooldown;
import me.taylorkelly.mywarp.timer.PlayerCooldown;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import me.taylorkelly.mywarp.timer.Warmup;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpToCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public WarpToCommand(MyWarp plugin) {
        super("WarpTo");
        this.plugin = plugin;
        setDescription("Warp to §9<name>");
        setUsage("/warp §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("warp", "mywarp", "mw");
        setPermission("mywarp.warp.basic.warp");
    }

    @Override
    public boolean execute(final CommandSender executor, String identifier,
            final String[] args) {
        if (executor instanceof Player) {
            Player player = (Player) executor;
            String name = plugin.getWarpList().getMatche(StringUtils.join(args, ' '),
                    player);

            if (!plugin.getWarpList().warpExists(name)) {
                player.sendMessage(ChatColor.RED + "No such warp '" + name + "'");
                return true;
            }

            Warp warp = plugin.getWarpList().getWarp(name);

            if (!warp.playerCanWarp(player)) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to warp to '" + name + "'");
                return true;
            }

            if (WarpSettings.useTimers) {
                Cooldown cooldown = MyWarp.getWarpPermissions().getCooldown(player);
                Warmup warmup = MyWarp.getWarpPermissions().getWarmup(player);

                if (PlayerCooldown.isActive(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You need to wait "
                            + PlayerCooldown.getRemainingTime(player.getName())
                            + " seconds before you can warp again.");
                    return true;
                }

                if (PlayerWarmup.isActive(player.getName())) {
                    player.sendMessage(ChatColor.RED + "You need to wait "
                            + PlayerWarmup.getRemainingTime(player.getName())
                            + " seconds untill you are teleported.");
                    return true;
                }

                if (MyWarp.getWarpPermissions().disobeyWarmup(player)) {
                    plugin.getWarpList().warpTo(name, player);

                    if (!MyWarp.getWarpPermissions().disobeyCooldown(player)) {
                        Scheduler.schedulePlayerCooldown(Scheduler.playerCooldown(
                                plugin, player, cooldown));
                    }
                    return true;
                }

                Scheduler.schedulePlayerWarmup(Scheduler.playerWarmup(plugin, player,
                        warmup, cooldown, name));

                if (WarpSettings.warmUpNotify) {
                    player.sendMessage(ChatColor.AQUA + "You will be teleported to '"
                            + warp.name + "' in " + warmup.getInt() + " seconds.");
                }
                return true;

            } else {
                plugin.getWarpList().warpTo(name, player);
                return true;
            }
        } else {
            executor.sendMessage("Console cannot warp to locations!");
            return true;
        }
    }
}

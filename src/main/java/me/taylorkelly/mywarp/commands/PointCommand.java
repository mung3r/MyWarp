package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PointCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public PointCommand(MyWarp plugin) {
        super("Point");
        this.plugin = plugin;
        setDescription("Point your compass to §9<name>");
        setUsage("/warp point §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("point");
        setPermission("mywarp.warp.basic.compass");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
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
                        + "You do not have permission to point to: '" + name + "'");
                return true;
            }

            plugin.getWarpList().point(StringUtils.join(args, ' '), (Player) executor);
            player.sendMessage(ChatColor.AQUA + "Your compass now guides you to '"
                    + name + "'");
            return true;
        } else {
            executor.sendMessage("Console cannot point its compass to warps!");
            return true;
        }
    }
}

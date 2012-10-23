package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatePrivateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public CreatePrivateCommand(MyWarp plugin) {
        super("pcreate");
        this.plugin = plugin;
        setDescription("Create a new private warp called §9<name>");
        setUsage("/warp pcreate §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("pcreate");
        setPermission("mywarp.warp.basic.createprivate");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (executor instanceof Player) {
            Player player = (Player) executor;
            String name = StringUtils.join(args, ' ');

            if (!plugin.getWarpList().playerCanBuildWarp(player)) {
                player.sendMessage(ChatColor.RED
                        + "You have reached your max # of warps " + ChatColor.YELLOW
                        + "(" + MyWarp.getWarpPermissions().maxTotalWarps(player) + ")");
                player.sendMessage("Delete some of your warps to make more");
                return true;
            }

            if (!plugin.getWarpList().playerCanBuildPrivateWarp(player)) {
                player.sendMessage(ChatColor.RED
                        + "You have reached your max # of private warps "
                        + ChatColor.YELLOW + "("
                        + MyWarp.getWarpPermissions().maxPrivateWarps(player) + ")");
                player.sendMessage("Delete some of your warps to make more");
                return true;
            }

            if (plugin.getWarpList().warpExists(name)) {
                player.sendMessage(ChatColor.RED + "Warp called '" + name
                        + "' already exists.");
                return true;
            }

            plugin.getWarpList().addWarpPrivate(name, player);
            player.sendMessage(ChatColor.AQUA + "Successfully created '" + name + "'");
            player.sendMessage("If you'd like to invite others to it,");
            player.sendMessage("Use: " + ChatColor.RED + "/warp invite <player> " + name);
            return true;
        } else {
            executor.sendMessage("Console cannot create private warps for themselves!");
            return true;
        }
    }
}

package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public CreateCommand(MyWarp plugin) {
        super("Create");
        this.plugin = plugin;
        setDescription("Create a new warp called §9<name>");
        setUsage("/warp create|set §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("create", "set");
        setPermission("mywarp.warp.basic.createpublic");
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

            if (!plugin.getWarpList().playerCanBuildPublicWarp(player)) {
                player.sendMessage(ChatColor.RED
                        + "You have reached your max # of public warps "
                        + ChatColor.YELLOW + "("
                        + MyWarp.getWarpPermissions().maxPublicWarps(player) + ")");
                player.sendMessage("Delete some of your warps to make more");
                return true;
            }

            if (plugin.getWarpList().warpExists(name)) {
                player.sendMessage(ChatColor.RED + "Warp called '" + name
                        + "' already exists.");
                return true;
            }

            plugin.getWarpList().addWarp(name, player);
            player.sendMessage(ChatColor.AQUA + "Successfully created '" + name + "'");
            player.sendMessage("If you'd like to privatize it,");
            player.sendMessage("Use: " + ChatColor.RED + "/warp private " + name);
            return true;
        } else {
            executor.sendMessage("Console cannot create warps for themselve!");
            return true;
        }
    }
}

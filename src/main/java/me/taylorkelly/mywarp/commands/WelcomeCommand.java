package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WelcomeCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public WelcomeCommand(MyWarp plugin) {
        super("Welcome");
        this.plugin = plugin;
        setDescription("Change the welcome message of §9<name>");
        setUsage("/warp welcome §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("welcome");
        setPermission("mywarp.warp.basic.welcome");
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

            if (!warp.playerCanModify(player)) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to modify '" + name + "'");
                return true;
            }

            plugin.getWarpList().welcomeMessage(name, player);
            player.sendMessage(ChatColor.AQUA + "Enter the welcome message for '" + name
                    + "'");
            return true;

        } else {
            executor.sendMessage("Console cannot change warp welcome messages!");
            return true;
        }
    }
}

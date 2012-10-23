package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminWarpToCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public AdminWarpToCommand(MyWarp plugin) {
        super("WarpPlayer");
        this.plugin = plugin;
        setDescription("Warp §8<player>§e to §9<name>");
        setUsage("/warp player §8<player> §9<name>");
        setArgumentRange(2, 255);
        setIdentifiers("player");
        setPermission("mywarp.admin");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        Player invitee = plugin.getServer().getPlayer(args[0]);
        String name = plugin.getWarpList().getMatche(
                StringUtils.join(Arrays.asList(args).subList(1, args.length), ' '),
                player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(ChatColor.RED + "No such warp '" + name + "'");
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null ? !warp.playerCanWarp(player) : false) {
            executor.sendMessage(ChatColor.RED
                    + "You do not have permission to warp to '" + name + "'");
            return true;
        }

        if (invitee == null) {
            executor.sendMessage(ChatColor.RED
                    + "You can not warp a player who is not online.");
            return true;
        }

        plugin.getWarpList().warpTo(name, invitee);
        executor.sendMessage(ChatColor.AQUA + "Successfully warped " + invitee.getName());
        return true;
    }
}

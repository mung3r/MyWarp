package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PublicCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public PublicCommand(MyWarp plugin) {
        super("Public");
        this.plugin = plugin;
        setDescription("Make §9<name>§e a public warp");
        setUsage("/warp public §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("public");
        setPermission("mywarp.warp.soc.public");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }
        String name = plugin.getWarpList()
                .getMatche(StringUtils.join(args, ' '), player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(ChatColor.RED + "No such warp '" + name + "'");
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null ? !warp.playerCanModify(player) : false) {
            executor.sendMessage(ChatColor.RED
                    + "You do not have permission to publicize '" + name + "'");
            return true;
        }

        if (player != null ? !plugin.getWarpList().playerCanBuildPrivateWarp(player)
                : false) {
            executor.sendMessage(ChatColor.RED
                    + "You have reached your max # of public warps " + ChatColor.YELLOW
                    + "(" + MyWarp.getWarpPermissions().maxPrivateWarps(player) + ")");
            executor.sendMessage("Delete some of your warps to make more");
            return true;
        }

        plugin.getWarpList().publicize(name);
        executor.sendMessage(ChatColor.AQUA + "You have publicized '" + name + "'");
        return true;
    }
}

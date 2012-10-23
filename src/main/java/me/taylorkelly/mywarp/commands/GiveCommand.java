package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public GiveCommand(MyWarp plugin) {
        super("Give");
        this.plugin = plugin;
        setDescription("Give your warp §9<name>§e to §8<player>");
        setUsage("/warp give §8<player> §9<name>");
        setArgumentRange(2, 255);
        setIdentifiers("give");
        setPermission("mywarp.warp.soc.give");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        Player givee = plugin.getServer().getPlayer(args[0]);

        if (givee == null) {
            executor.sendMessage(ChatColor.RED + args[0]
                    + "needs to be online to receive warps.");
            return true;
        }

        String giveeName = givee.getName();
        String name = plugin.getWarpList().getMatche(
                StringUtils.join(Arrays.asList(args).subList(1, args.length), ' '),
                player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(ChatColor.RED + "No such warp '" + name + "'");
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null ? !warp.playerCanModify(player) : false) {
            executor.sendMessage(ChatColor.RED + "You do not have permission to give '"
                    + name + "' to " + giveeName);
            return true;
        }

        if (warp.playerIsCreator(giveeName)) {
            executor.sendMessage(ChatColor.RED + giveeName + " is already the owner.");
            return true;
        }

        if (!plugin.getWarpList().playerCanBuildWarp(givee)) {
            executor.sendMessage(ChatColor.RED + "Player " + giveeName
                    + " has reached his max # of warps " + ChatColor.YELLOW + "("
                    + MyWarp.getWarpPermissions().maxTotalWarps(player) + ")");
            executor.sendMessage("Tell him to delete some of his warps to receive this one.");
            return true;
        }

        if (warp.publicAll) {
            if (!plugin.getWarpList().playerCanBuildPublicWarp(givee)) {
                executor.sendMessage(ChatColor.RED + "Player " + giveeName
                        + " has reached his max # of public warps " + ChatColor.YELLOW
                        + "(" + MyWarp.getWarpPermissions().maxPublicWarps(player) + ")");
                executor.sendMessage("Tell him to delete some of his warps to receive this one.");
                return true;
            }
        } else if (!plugin.getWarpList().playerCanBuildPrivateWarp(givee)) {
            executor.sendMessage(ChatColor.RED + "Player " + giveeName
                    + " has reached his max # of private warps " + ChatColor.YELLOW
                    + "(" + MyWarp.getWarpPermissions().maxPrivateWarps(player) + ")");
            executor.sendMessage("Tell him to delete some of his warps to receive this one.");
            return true;
        }

        plugin.getWarpList().give(name, givee);
        executor.sendMessage(ChatColor.AQUA + "You have given '" + name + "' to "
                + giveeName);
        givee.sendMessage(ChatColor.AQUA + "You've been given '" + name + "' by "
                + executor.getName());
        return true;
    }
}

package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public InviteCommand(MyWarp plugin) {
        super("Invite");
        this.plugin = plugin;
        setDescription("Invite §8<player>§e to §9<name>");
        setUsage("/warp invite §8<player> §9<name>");
        setArgumentRange(2, 255);
        setIdentifiers("invite");
        setPermission("mywarp.warp.soc.invite.player");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        String name = plugin.getWarpList().getMatche(
                StringUtils.join(Arrays.asList(args).subList(1, args.length), ' '),
                player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(ChatColor.RED + "No such warp '" + name + "'");
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (args[0].startsWith("g:")) {
            if (player != null ? !warp.playerCanModify(player) : false) {
                executor.sendMessage(ChatColor.RED
                        + "You do not have permission to invite groups to '" + name
                        + "'");
                return true;
            }

            if (!MyWarp.getWarpPermissions().hasPermission((Player) executor,
                    "mywarp.warp.soc.invite.group")) {
                executor.sendMessage("You don't have permission to invite groups.");
                return true;
            }

            String inviteeName = args[0].substring(2);

            if (warp.groupIsInvited(inviteeName)) {
                executor.sendMessage(ChatColor.RED + "Group " + inviteeName
                        + " is already invited to this warp.");
                return true;
            }

            plugin.getWarpList().inviteGroup(name, inviteeName);
            executor.sendMessage(ChatColor.AQUA + "You have invited group "
                    + inviteeName + " to '" + name + "'");

            if (warp.publicAll) {
                executor.sendMessage(ChatColor.RED + "But '" + name
                        + "' is still public.");
            }
            return true;
        } else {
            if (player != null ? warp.playerCanModify(player) : false) {
                executor.sendMessage(ChatColor.RED
                        + "You do not have permission to invite players to '" + name
                        + "'");
                return true;
            }

            Player invitee = plugin.getServer().getPlayer(args[0]);
            String inviteeName = (invitee == null) ? args[0] : invitee.getName();

            if (warp.playerIsInvited(inviteeName)) {
                executor.sendMessage(ChatColor.RED + inviteeName
                        + " is already invited to this warp.");
                return true;
            }

            if (warp.playerIsCreator(inviteeName)) {
                executor.sendMessage(ChatColor.RED + inviteeName
                        + " is the creator, of course he's the invited!");
                return true;
            }

            plugin.getWarpList().invitePlayer(name, inviteeName);
            executor.sendMessage(ChatColor.AQUA + "You have invited " + inviteeName
                    + " to '" + name + "'");

            if (warp.publicAll) {
                executor.sendMessage(ChatColor.RED + "But '" + name
                        + "' is still public.");
            }

            if (invitee != null) {
                invitee.sendMessage(ChatColor.AQUA + "You've been invited to warp '"
                        + name + "' by " + executor.getName());
                invitee.sendMessage("Use: " + ChatColor.RED + "/warp " + name
                        + ChatColor.WHITE + " to warp to it.");
            }
            return true;
        }
    }
}

package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UninviteCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public UninviteCommand(MyWarp plugin) {
        super("Uninvite");
        this.plugin = plugin;
        setDescription("Uninvite §8<player|group>§e from §9<name>");
        setUsage("/warp uninvite §8<player|group> §9<name>");
        setArgumentRange(2, 255);
        setIdentifiers("uninvite");
        setPermission("mywarp.warp.soc.uninvite.player");
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
                        + "You do not have permission to uninvite groups from '" + name
                        + "'");
                return true;
            }

            if (!MyWarp.getWarpPermissions().hasPermission((Player) executor,
                    "mywarp.warp.soc.uninvite.group")) {
                executor.sendMessage("You don't have permission to uninvite groups.");
                return true;
            }

            String inviteeName = args[0].substring(2);

            if (!warp.groupIsInvited(inviteeName)) {
                executor.sendMessage(ChatColor.RED + inviteeName
                        + " is not invited to this warp.");
                return true;
            }

            plugin.getWarpList().uninviteGroup(name, inviteeName);
            executor.sendMessage(ChatColor.AQUA + "You have uninvited group "
                    + inviteeName + " from '" + name + "'");

            if (warp.publicAll) {
                executor.sendMessage(ChatColor.RED + "But '" + name + "' is still public.");
            }

            return true;
        } else {
            if (player != null ? !warp.playerCanModify(player) : false) {
                executor.sendMessage(ChatColor.RED
                        + "You do not have permission to uninvite players from '" + name
                        + "'");
                return true;
            }

            Player invitee = plugin.getServer().getPlayer(args[0]);
            String inviteeName = (invitee == null) ? args[0] : invitee.getName();

            if (!warp.playerIsInvited(inviteeName)) {
                executor.sendMessage(ChatColor.RED + inviteeName
                        + " is not invited to this warp.");
                return true;
            }

            if (warp.playerIsCreator(inviteeName)) {
                executor.sendMessage(ChatColor.RED
                        + "You can't uninvite yourself. You're the creator!");
                return true;
            }

            plugin.getWarpList().uninvitePlayer(name, inviteeName);

            executor.sendMessage(ChatColor.AQUA + "You have uninvited " + inviteeName
                    + " from '" + name + "'");

            if (warp.publicAll) {
                executor.sendMessage(ChatColor.RED + "But '" + name + "' is still public.");
            }

            if (invitee != null) {
                invitee.sendMessage(ChatColor.RED + "You've been uninvited to warp '"
                        + name + "' by " + executor.getName() + ". Sorry.");
            }
            return true;
        }
    }
}

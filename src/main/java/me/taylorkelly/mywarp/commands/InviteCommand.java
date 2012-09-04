package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.StringUtils;
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
        if (executor instanceof Player) {
            if (args[0].startsWith("g:")) {
                if (MyWarp.getWarpPermissions().hasPermission((Player) executor,
                        "mywarp.warp.soc.invite.group")) {
                    String inviteeGroup = args[0].substring(2);
                    plugin.getWarpList().inviteGroup(
                            StringUtils.join(
                                    Arrays.asList(args).subList(1, args.length), ' '),
                            (Player) executor, inviteeGroup);
                } else {
                    executor.sendMessage("You don't have permission to invite groups.");
                }

            } else {
                Player invitee = plugin.getServer().getPlayer(args[0]);
                // TODO Change to matchPlayer
                String inviteeName = (invitee == null) ? args[0] : invitee.getName();

                plugin.getWarpList().invitePlayer(
                        StringUtils.join(Arrays.asList(args).subList(1, args.length),
                                ' '), (Player) executor, inviteeName);
            }
        } else {
            executor.sendMessage("Console cannot invite warps for themselves!");
        }

        return true;
    }
}

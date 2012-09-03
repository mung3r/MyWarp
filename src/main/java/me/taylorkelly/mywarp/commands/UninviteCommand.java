package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.StringUtils;
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
        setPermission("mywarp.warp.soc.uninvite");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (executor instanceof Player) {
            if (args[0].startsWith("g:")) {
                String inviteeGroup = args[0].substring(2);
                plugin.getWarpList().uninviteGroup(
                        StringUtils.join(Arrays.asList(args).subList(1, args.length),
                                ' '), (Player) executor, inviteeGroup);
            } else {
                Player invitee = plugin.getServer().getPlayer(args[0]);
                String inviteeName = (invitee == null) ? args[0] : invitee.getName();

                plugin.getWarpList().uninvitePlayer(
                        StringUtils.join(Arrays.asList(args).subList(1, args.length),
                                ' '), (Player) executor, inviteeName);
            }
        } else {
            executor.sendMessage("Console cannot uninvite warps for themselves!");
        }

        return true;
    }
}

package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UninviteCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public UninviteCommand(MyWarp plugin) {
        super("Uninvite");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.uninvite"));
        setUsage("/warp uninvite §8<"
                + LanguageManager.getColorlessString("help.usage.player") + "|"
                + LanguageManager.getColorlessString("help.usage.group")
                + "> §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(2, 255);
        setIdentifiers("uninvite");
        setPermission("mywarp.warp.soc.uninvite.player");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        String name = plugin.getWarpList().getMatche(
                StringUtils.join(Arrays.asList(args).subList(1, args.length),
                        ' '), player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(LanguageManager.getString("error.noSuchWarp")
                    .replaceAll("%warp%", name));
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (args[0].startsWith("g:")) {
            if (player != null && !warp.playerCanModify(player)) {
                executor.sendMessage(LanguageManager.getString(
                        "error.noPermission.uninvite.groups").replaceAll(
                        "%warp%", name));
                return true;
            }

            if (!MyWarp.getWarpPermissions().hasPermission((Player) executor,
                    "mywarp.warp.soc.uninvite.group")) {
                executor.sendMessage(LanguageManager
                        .getString("error.noPermission"));
                return true;
            }

            String inviteeName = args[0].substring(2);

            if (!warp.groupIsInvited(inviteeName)) {
                executor.sendMessage(LanguageManager.getString(
                        "error.uninvite.notInvited.group").replaceAll(
                        "%group%", inviteeName));
                return true;
            }

            plugin.getWarpList().uninviteGroup(name, inviteeName);

            if (warp.publicAll) {
                executor.sendMessage(LanguageManager
                        .getString("warp.uninvite.group.public")
                        .replaceAll("%warp%", name)
                        .replaceAll("%group%", inviteeName));
            } else {
                executor.sendMessage(LanguageManager
                        .getString("warp.uninvite.group.private")
                        .replaceAll("%warp%", name)
                        .replaceAll("%group%", inviteeName));
            }

            return true;
        } else {
            if (player != null && !warp.playerCanModify(player)) {
                executor.sendMessage(LanguageManager.getString(
                        "error.noPermission.uninvite.players").replaceAll(
                        "%warp%", name));
                return true;
            }

            Player invitee = plugin.getServer().getPlayer(args[0]);
            String inviteeName = (invitee == null) ? args[0] : invitee
                    .getName();

            if (!warp.playerIsInvited(inviteeName)) {
                executor.sendMessage(LanguageManager.getString(
                        "error.uninvite.notInvited.player").replaceAll(
                        "%player%", inviteeName));
                return true;
            }

            if (warp.playerIsCreator(inviteeName)) {
                executor.sendMessage(LanguageManager.getString(
                        "error.uninvite.creator").replaceAll("%player%",
                        inviteeName));
                return true;
            }

            plugin.getWarpList().uninvitePlayer(name, inviteeName);

            if (warp.publicAll) {
                executor.sendMessage(LanguageManager
                        .getString("warp.uninvite.player.public")
                        .replaceAll("%warp%", name)
                        .replaceAll("%player%", inviteeName));
            } else {
                executor.sendMessage(LanguageManager
                        .getString("warp.uninvite.player.private")
                        .replaceAll("%warp%", name)
                        .replaceAll("%player%", inviteeName));
            }

            if (invitee != null) {
                invitee.sendMessage(LanguageManager
                        .getString("warp.uninvite.uninvited"));
            }
            return true;
        }
    }
}

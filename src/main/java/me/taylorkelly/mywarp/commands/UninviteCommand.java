package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

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
        setPermission("mywarp.warp.soc.uninvite");
    }

    @Override
    public void execute(CommandSender sender, String identifier,
            String[] args) throws CommandException {

        Warp warp = CommandUtils.getWarpForModification(sender, CommandUtils.toWarpName(args, 1));

        // uninvite group
        if (args[0].startsWith("g:")) {
            if (!MyWarp.getWarpPermissions().canUninviteGroup(sender)) {
                throw new CommandException(LanguageManager
                        .getString("error.noPermission"));
            }
            String inviteeName = args[0].substring(2);

            if (!warp.groupIsInvited(inviteeName)) {
                throw new CommandException(LanguageManager.getString(
                        "error.uninvite.notInvited.group").replaceAll(
                        "%group%", inviteeName));
            }

            plugin.getWarpList().uninviteGroup(warp, inviteeName);

            if (warp.publicAll) {
                sender.sendMessage(LanguageManager
                        .getString("warp.uninvite.group.public")
                        .replaceAll("%warp%", warp.name)
                        .replaceAll("%group%", inviteeName));
            } else {
                sender.sendMessage(LanguageManager
                        .getString("warp.uninvite.group.private")
                        .replaceAll("%warp%", warp.name)
                        .replaceAll("%group%", inviteeName));
            }
            return;
        }
        // uninvite player
        Player invitee = plugin.getServer().getPlayer(args[0]);
        String inviteeName = (invitee == null) ? args[0] : invitee.getName();

        if (!warp.playerIsInvited(inviteeName)) {
            throw new CommandException(LanguageManager.getString(
                    "error.uninvite.notInvited.player").replaceAll("%player%",
                    inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(LanguageManager.getString(
                    "error.uninvite.creator").replaceAll("%player%",
                    inviteeName));
        }

        plugin.getWarpList().uninvitePlayer(warp, inviteeName);

        if (warp.publicAll) {
            sender.sendMessage(LanguageManager
                    .getString("warp.uninvite.player.public")
                    .replaceAll("%warp%", warp.name)
                    .replaceAll("%player%", inviteeName));
        } else {
            sender.sendMessage(LanguageManager
                    .getString("warp.uninvite.player.private")
                    .replaceAll("%warp%", warp.name)
                    .replaceAll("%player%", inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(LanguageManager
                    .getString("warp.uninvite.uninvited"));
        }
    }
}

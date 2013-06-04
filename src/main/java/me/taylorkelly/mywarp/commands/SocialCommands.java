package me.taylorkelly.mywarp.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.TempConcurrentHashMap;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.CommandPermissionsException;

/**
 * This class contains all commands that cover social tasks. They should be
 * included in the <code>mywarp.warp.soc.*</code> permission container.
 * 
 */
public class SocialCommands {

    private final MyWarp plugin;
    private TempConcurrentHashMap<String, Warp> givenWarps;

    public SocialCommands(MyWarp plugin) {
        this.plugin = plugin;
        givenWarps = new TempConcurrentHashMap<String, Warp>(plugin);
    }

    @Command(aliases = { "give" }, usage = "<player> <name>", desc = "cmd.description.give", fee = Fee.GIVE, min = 2, flags = "df", permissions = { "mywarp.warp.soc.give" })
    public void giveWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        // 'd' - give the warp directly without asking for acception
        // 'f' - ignore limits if enabled

        Player givee = plugin.getServer().getPlayer(args.getString(0));
        String giveeName;

        if (givee == null) {
            // givee needs to be online unless the warp is given directly
            if (!args.hasFlag('d')) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "error.player.offline", "%player%", args.getString(0)));
            }
            giveeName = args.getString(0);
        } else {
            giveeName = givee.getName();
        }

        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(1));

        if (warp.playerIsCreator(giveeName)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.give.isOwner", "%player%", giveeName));
        }

        if (!args.hasFlag('f')) {
            CommandUtils.checkPlayerLimits(givee, warp.publicAll);
        } else {
            if (!MyWarp.getWarpPermissions().hasPermission(sender,
                    "mywarp.warp.soc.give.force")) {
                throw new CommandPermissionsException();
            }
        }

        // if 'd' is present the warp is given directly
        if (args.hasFlag('d')) {
            if (!MyWarp.getWarpPermissions().hasPermission(sender,
                    "mywarp.warp.soc.give.direct")) {
                throw new CommandPermissionsException();
            }
            plugin.getWarpList().give(warp, giveeName);

            if (givee != null) {
                givee.sendMessage(LanguageManager.getEffectiveString(
                        "warp.give.accept", "%warp%", warp.name));
            }
        } else {
            givenWarps.put(giveeName, warp);
            givee.sendMessage(LanguageManager.getEffectiveString(
                    "warp.give.asked", "%warp%", warp.name, "%player%",
                    sender.getName()));
        }
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.give.given", "%warp%", warp.name, "%player%", giveeName));
    }

    @Command(aliases = { "accept" }, usage = "", desc = "cmd.description.accept", fee = Fee.ACCEPT, max = 0, permissions = { "mywarp.warp.soc.accept" })
    public void acceptGivenWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        if (!givenWarps.containsKey(sender.getName())) {
            // TODO translate
            throw new CommandException(
                    LanguageManager.getString("error.accept.noWarp"));
        }
        Warp warp = givenWarps.get(sender.getName());

        plugin.getWarpList().give(warp, sender.getName());
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.give.accept", "%warp%", warp.name));
    }

    @Command(aliases = { "invite" }, usage = "<player> <name>", desc = "cmd.description.invite", fee = Fee.INVITE, min = 2, permissions = { "mywarp.warp.soc.invite" })
    public void inviteToWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(1));

        // invite group
        if (args.getString(0).startsWith("g:")) {
            if (!MyWarp.getWarpPermissions().canInviteGroup(sender)) {
                throw new CommandPermissionsException();
            }

            String inviteeName = args.getString(0).substring(2);

            if (warp.groupIsInvited(inviteeName)) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "error.invite.invited.group", "%group%", inviteeName));
            }

            plugin.getWarpList().inviteGroup(warp, inviteeName);

            if (warp.publicAll) {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "warp.invite.group.public", "%warp%", warp.name,
                        "%group%", inviteeName));
            } else {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "warp.invite.group.private", "%warp%", warp.name,
                        "%group%", inviteeName));
            }
            return;
        }
        // invite player
        Player invitee = plugin.getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee
                .getName();

        if (warp.playerIsInvited(inviteeName)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.invite.invited.player", "%player%", inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.invite.creator", "%player%", inviteeName));
        }

        plugin.getWarpList().invitePlayer(warp, inviteeName);
        if (warp.publicAll) {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "warp.invite.player.public", "%warp%", warp.name,
                    "%player%", inviteeName));
        } else {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "warp.invite.player.private", "%warp%", warp.name,
                    "%player%", inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(LanguageManager.getEffectiveString(
                    "warp.invite.invited", "%warp%", warp.name, "%player%",
                    sender.getName()));
        }

    }

    @Command(aliases = { "private" }, usage = "<name>", desc = "cmd.description.private", fee = Fee.PRIVATE, min = 1, permissions = { "mywarp.warp.soc.private" })
    public void privatizeWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));
        if (!warp.publicAll) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.private.isPrivate", "%warp%", warp.name));
        }
        CommandUtils.checkPrivateLimit(sender);

        plugin.getWarpList().privatize(warp);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.private",
                "%warp%", warp.name));
    }

    @Command(aliases = { "public" }, usage = "<name>", desc = "cmd.description.public", fee = Fee.PUBLIC, min = 1, permissions = { "mywarp.warp.soc.public" })
    public void publicizeWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(0));
        if (warp.publicAll) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.public.isPublic", "%warp%", warp.name));
        }
        CommandUtils.checkPublicLimit(sender);

        plugin.getWarpList().publicize(warp);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.public",
                "%warp%", warp.name));
    }

    @Command(aliases = { "uninvite" }, usage = "<player> <name>", desc = "cmd.description.uninvite", fee = Fee.UNINVITE, min = 2, permissions = { "mywarp.warp.soc.uninvite" })
    public void uninviteFromWarp(CommandContext args, CommandSender sender)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                args.getJoinedStrings(1));

        // uninvite group
        if (args.getString(0).startsWith("g:")) {
            if (!MyWarp.getWarpPermissions().canUninviteGroup(sender)) {
                throw new CommandPermissionsException();
            }
            String inviteeName = args.getString(0).substring(2);

            if (!warp.groupIsInvited(inviteeName)) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "error.uninvite.notInvited.group", "%group%",
                        inviteeName));
            }

            plugin.getWarpList().uninviteGroup(warp, inviteeName);

            if (warp.publicAll) {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "warp.uninvite.group.public", "%warp%", warp.name,
                        "%group%", inviteeName));
            } else {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "warp.uninvite.group.private", "%warp%", warp.name,
                        "%group%", inviteeName));
            }
            return;
        }
        // uninvite player
        Player invitee = plugin.getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee
                .getName();

        if (!warp.playerIsInvited(inviteeName)) {
            throw new CommandException(
                    LanguageManager.getEffectiveString(
                            "error.uninvite.notInvited.player", "%player%",
                            inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.uninvite.creator", "%player%", inviteeName));
        }

        plugin.getWarpList().uninvitePlayer(warp, inviteeName);

        if (warp.publicAll) {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "warp.uninvite.player.public", "%warp%", warp.name,
                    "%player%", inviteeName));
        } else {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "warp.uninvite.player.private", "%warp%", warp.name,
                    "%player%", inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(LanguageManager
                    .getString("warp.uninvite.uninvited"));
        }
    }
}

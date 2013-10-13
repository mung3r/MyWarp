package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.TempConcurrentHashMap;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.CommandPermissionsException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class contains all commands that cover social tasks. They should be
 * included in the <code>mywarp.warp.soc.*</code> permission container.
 * 
 */
public class SocialCommands {

    private TempConcurrentHashMap<String, Warp> givenWarps = new TempConcurrentHashMap<String, Warp>();

    @Command(aliases = { "give" }, usage = "<player> <name>", desc = "cmd.description.give", fee = Fee.GIVE, min = 2, flags = "df", permissions = { "mywarp.warp.soc.give" })
    public void giveWarp(CommandContext args, CommandSender sender) throws CommandException {
        // 'd' - give the warp directly without asking for acception
        // 'f' - ignore limits if enabled

        Player givee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String giveeName;

        if (givee == null) {
            // givee needs to be online unless the warp is given directly
            if (!args.hasFlag('d')) {
                throw new CommandException(MyWarp.inst().getLanguageManager()
                        .getEffectiveString("error.player.offline", "%player%", args.getString(0)));
            }
            giveeName = args.getString(0);
        } else {
            giveeName = givee.getName();
        }

        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        if (warp.playerIsCreator(giveeName)) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.give.isOwner", "%player%", giveeName));
        }

        if (!args.hasFlag('f')) {
            CommandUtils.checkPlayerLimits(givee, warp.isPublicAll());
        } else {
            if (!MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.warp.soc.give.force")) {
                throw new CommandPermissionsException();
            }
        }

        // if 'd' is present the warp is given directly
        if (args.hasFlag('d')) {
            if (!MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.warp.soc.give.direct")) {
                throw new CommandPermissionsException();
            }
            warp.setCreator(giveeName);

            if (givee != null) {
                givee.sendMessage(MyWarp.inst().getLanguageManager()
                        .getEffectiveString("warp.give.accept", "%warp%", warp.getName()));
            }
        } else {
            givenWarps.put(giveeName, warp);
            givee.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.give.asked", "%warp%", warp.getName(), "%player%",
                            sender.getName()));
        }
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.give.given", "%warp%", warp.getName(), "%player%", giveeName));
    }

    @Command(aliases = { "accept" }, usage = "", desc = "cmd.description.accept", fee = Fee.ACCEPT, max = 0, permissions = { "mywarp.warp.soc.accept" })
    public void acceptGivenWarp(CommandContext args, CommandSender sender) throws CommandException {
        if (!givenWarps.containsKey(sender.getName())) {
            // TODO translate
            throw new CommandException(MyWarp.inst().getLanguageManager().getString("error.accept.noWarp"));
        }
        Warp warp = givenWarps.get(sender.getName());
        warp.setCreator(sender.getName());
        givenWarps.remove(sender.getName());

        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.give.accept", "%warp%", warp.getName()));
    }

    @Command(aliases = { "invite" }, usage = "<player> <name>", desc = "cmd.description.invite", fee = Fee.INVITE, min = 2, permissions = { "mywarp.warp.soc.invite" })
    public void inviteToWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        // invite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.invite.group");

            String inviteeName = args.getString(0).substring(2);

            if (warp.groupIsInvited(inviteeName)) {
                throw new CommandException(MyWarp.inst().getLanguageManager()
                        .getEffectiveString("error.invite.invited.group", "%group%", inviteeName));
            }

            warp.inviteGroup(inviteeName);

            if (warp.isPublicAll()) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("warp.invite.group.public", "%warp%", warp.getName(), "%group%",
                                inviteeName));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("warp.invite.group.private", "%warp%", warp.getName(), "%group%",
                                inviteeName));
            }
            return;
        }
        // invite player
        Player invitee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee.getName();

        if (warp.playerIsInvited(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.invite.invited.player", "%player%", inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.invite.creator", "%player%", inviteeName));
        }

        warp.invite(inviteeName);
        if (warp.isPublicAll()) {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.invite.player.public", "%warp%", warp.getName(), "%player%",
                            inviteeName));
        } else {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.invite.player.private", "%warp%", warp.getName(), "%player%",
                            inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.invite.invited", "%warp%", warp.getName(), "%player%",
                            sender.getName()));
        }

    }

    @Command(aliases = { "private" }, usage = "<name>", desc = "cmd.description.private", fee = Fee.PRIVATE, min = 1, permissions = { "mywarp.warp.soc.private" })
    public void privatizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));
        if (!warp.isPublicAll()) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.private.isPrivate", "%warp%", warp.getName()));
        }
        CommandUtils.checkPrivateLimit(sender);

        warp.setPublicAll(false);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.private", "%warp%", warp.getName()));
    }

    @Command(aliases = { "public" }, usage = "<name>", desc = "cmd.description.public", fee = Fee.PUBLIC, min = 1, permissions = { "mywarp.warp.soc.public" })
    public void publicizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));
        if (warp.isPublicAll()) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.public.isPublic", "%warp%", warp.getName()));
        }
        CommandUtils.checkPublicLimit(sender);

        warp.setPublicAll(true);
        sender.sendMessage(MyWarp.inst().getLanguageManager()
                .getEffectiveString("warp.public", "%warp%", warp.getName()));
    }

    @Command(aliases = { "uninvite" }, usage = "<player> <name>", desc = "cmd.description.uninvite", fee = Fee.UNINVITE, min = 2, permissions = { "mywarp.warp.soc.uninvite" })
    public void uninviteFromWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        // uninvite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.uninvite.group");
            String inviteeName = args.getString(0).substring(2);

            if (!warp.groupIsInvited(inviteeName)) {
                throw new CommandException(MyWarp.inst().getLanguageManager()
                        .getEffectiveString("error.uninvite.notInvited.group", "%group%", inviteeName));
            }

            warp.uninviteGroup(inviteeName);

            if (warp.isPublicAll()) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("warp.uninvite.group.public", "%warp%", warp.getName(),
                                "%group%", inviteeName));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("warp.uninvite.group.private", "%warp%", warp.getName(),
                                "%group%", inviteeName));
            }
            return;
        }
        // uninvite player
        Player invitee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee.getName();

        if (!warp.playerIsInvited(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.uninvite.notInvited.player", "%player%", inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLanguageManager()
                    .getEffectiveString("error.uninvite.creator", "%player%", inviteeName));
        }

        warp.uninvite(inviteeName);

        if (warp.isPublicAll()) {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.uninvite.player.public", "%warp%", warp.getName(), "%player%",
                            inviteeName));
        } else {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.uninvite.player.private", "%warp%", warp.getName(), "%player%",
                            inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(MyWarp.inst().getLanguageManager().getEffectiveString("warp.uninvite.uninvited", "%warp%", warp.getName()));
        }
    }
}

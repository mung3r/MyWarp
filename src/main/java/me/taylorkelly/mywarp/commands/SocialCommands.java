package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.TempConcurrentHashMap;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class contains all commands that cover social tasks. They should be
 * included in the <code>mywarp.warp.soc.*</code> permission container.
 * 
 */
public class SocialCommands {

    private TempConcurrentHashMap<String, Warp> givenWarps = new TempConcurrentHashMap<String, Warp>();

    @Command(aliases = { "give" }, usage = "<player> <name>", desc = "commands.given.description", fee = Fee.GIVE, min = 2, flags = "df", permissions = { "mywarp.warp.soc.give" })
    public void giveWarp(CommandContext args, CommandSender sender) throws CommandException {
        // check flag permissions first because anything else depends on them
        if (args.hasFlag('d')) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.give.direct");
        }
        if (args.hasFlag('f')) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.give.force");
        }
        String giveeName = args.getString(0);
        Player givee = MyWarp.server().getPlayer(giveeName);

        if (givee == null) {
            if (!(args.hasFlag('d') && args.hasFlag('f'))) {
                // if the user does not want to give the warp directly while
                // ignoring limits, givee must be online.
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("commands.utils.player-offline", sender, giveeName));
            }
        } else {
            giveeName = givee.getName();
        }

        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));
        if (!args.hasFlag('f') && givee != null && !CommandUtils.checkPlayerLimits(givee, warp.isPublicAll())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.give.givee-limits", sender, givee.getName()));
        }

        if (warp.isCreator(giveeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.give.is-owner", sender, giveeName));
        }

        if (args.hasFlag('d')) {
            warp.setCreator(giveeName);
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.give.given-succesfull", sender, warp.getName(), giveeName));

            if (givee != null) {
                givee.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("commands.accept.accepted-succesfull", sender, warp.getName()));
            }
        } else {
            // ask givee if he wants to accept the warp
            givenWarps.put(giveeName, warp);
            givee.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.give.givee-message", sender, warp.getName(),
                            sender.getName(), warp.getName()));
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.give.asked-succesfull", sender, giveeName, warp.getName()));
        }
    }

    @Command(aliases = { "accept" }, usage = "", desc = "commands.accept.description", fee = Fee.ACCEPT, max = 0, permissions = { "mywarp.warp.soc.accept" })
    public void acceptGivenWarp(CommandContext args, CommandSender sender) throws CommandException {
        if (!givenWarps.containsKey(sender.getName())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.accept.nothing-to-accept", sender));
        }
        Warp warp = givenWarps.get(sender.getName());
        warp.setCreator(sender.getName());
        givenWarps.remove(sender.getName());

        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.accept.accepted-succesfull", sender, warp.getName()));
    }

    @Command(aliases = { "invite" }, usage = "<player> <name>", desc = "commands.invite.description", fee = Fee.INVITE, min = 2, permissions = { "mywarp.warp.soc.invite" })
    public void inviteToWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        // invite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.invite.group");

            String inviteeName = args.getString(0).substring(2);

            if (warp.isGroupInvited(inviteeName)) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("commands.invite.group.already-invited", sender, inviteeName));
            }

            warp.inviteGroup(inviteeName);

            if (warp.isPublicAll()) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("commands.invite.group.public", sender, warp.getName(),
                                inviteeName));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("commands.invite.group.private", sender, warp.getName(),
                                inviteeName));
            }
            return;
        }
        // invite player
        Player invitee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee.getName();

        if (warp.isInvited(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.invite.player.already-invited", sender, inviteeName));
        }

        if (warp.isCreator(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.invite.player.is-creator", sender, inviteeName));
        }

        warp.invite(inviteeName);
        if (warp.isPublicAll()) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.invite.player.public", sender, warp.getName(), inviteeName));
        } else {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.invite.player.private", sender, warp.getName(), inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.invite.player.player-invited", sender, warp.getName(),
                            sender.getName()));
        }

    }

    @Command(aliases = { "private" }, usage = "<name>", desc = "commands.private.description", fee = Fee.PRIVATE, min = 1, permissions = { "mywarp.warp.soc.private" })
    public void privatizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));
        if (!warp.isPublicAll()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.private.already-private", sender, warp.getName()));
        }
        CommandUtils.checkPrivateLimit(sender);

        warp.setPublicAll(false);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.private.privatized", sender, warp.getName()));
    }

    @Command(aliases = { "public" }, usage = "<name>", desc = "commands.public.description", fee = Fee.PUBLIC, min = 1, permissions = { "mywarp.warp.soc.public" })
    public void publicizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));
        if (warp.isPublicAll()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.public.already-public", sender, warp.getName()));
        }
        CommandUtils.checkPublicLimit(sender);

        warp.setPublicAll(true);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("commands.public.publicized", sender, warp.getName()));
    }

    @Command(aliases = { "uninvite" }, usage = "<player> <name>", desc = "commands.uninvite.description", fee = Fee.UNINVITE, min = 2, permissions = { "mywarp.warp.soc.uninvite" })
    public void uninviteFromWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        // uninvite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.uninvite.group");
            String inviteeName = args.getString(0).substring(2);

            if (!warp.isGroupInvited(inviteeName)) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("commands.uninvite.group.not-invited", sender, inviteeName));
            }

            warp.uninviteGroup(inviteeName);

            if (warp.isPublicAll()) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("commands.uninvite.group.public", sender, warp.getName(),
                                inviteeName));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("commands.uninvite.group.private", sender, warp.getName(),
                                inviteeName));
            }
            return;
        }
        // uninvite player
        Player invitee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee.getName();

        if (!warp.isInvited(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.uninvite.player.not-invited", sender, inviteeName));
        }

        if (warp.isCreator(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.uninvite.player.is-creator", sender, inviteeName));
        }

        warp.uninvite(inviteeName);

        if (warp.isPublicAll()) {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.uninvite.player.public", sender, warp.getName(),
                            inviteeName));
        } else {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("commands.uninvite.player.private", sender, warp.getName(),
                            inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("commands.uninvite.player.player-uninvited", sender, warp.getName()));
        }
    }
}

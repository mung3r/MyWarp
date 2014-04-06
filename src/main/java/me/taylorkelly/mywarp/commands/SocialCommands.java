package me.taylorkelly.mywarp.commands;

import java.util.UUID;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.data.Warp.Type;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.TempConcurrentHashMap;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class contains all commands that cover social tasks. They should be
 * included in the <code>mywarp.warp.soc.*</code> permission container.
 * 
 */
public class SocialCommands {

    private TempConcurrentHashMap<UUID, Warp> givenWarps = new TempConcurrentHashMap<UUID, Warp>();

    @Command(aliases = { "give" }, usage = "<player> <name>", desc = "commands.give.description", fee = Fee.GIVE, min = 2, flags = "df", permissions = { "mywarp.warp.soc.give" })
    public void giveWarp(CommandContext args, CommandSender sender) throws CommandException {
        // check flag permissions first because anything else depends on them
        if (args.hasFlag('d')) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.give.direct");
        }
        if (args.hasFlag('f')) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.give.force");
        }
        OfflinePlayer givee = MyWarp.server().getPlayer(args.getString(0));

        if (givee == null) {
            if (!(args.hasFlag('d') && args.hasFlag('f'))) {
                // if the user does not want to give the warp directly while
                // ignoring limits, givee must be online.
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.utils.player-offline", sender, args.getString(0)));
            }
            givee = MyWarp.server().getOfflinePlayer(args.getString(0));
        }

        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(1));
        if (!args.hasFlag('f') && givee.getPlayer() != null
                && !CommandUtils.checkPlayerLimits(givee.getPlayer(), warp.getWorld(), warp.getType())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.give.givee-limits", sender, givee.getName()));
        }

        if (warp.getCreatorId() == givee.getUniqueId()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.give.is-owner", sender, givee.getName()));
        }

        if (args.hasFlag('d')) {
            warp.setCreatorId(givee.getUniqueId());
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.give.given-successful", sender, warp.getName(), givee.getName()));

            if (givee.getPlayer() != null) {
                givee.getPlayer().sendMessage(
                        MyWarp.inst().getLocalizationManager()
                                .getString("commands.accept.accepted-successful", sender, warp.getName()));
            }
        } else {
            // ask givee if he wants to accept the warp
            givenWarps.put(givee.getUniqueId(), warp);
            givee.getPlayer().sendMessage(
                    MyWarp.inst()
                            .getLocalizationManager()
                            .getString("commands.give.givee-message", sender, warp.getName(),
                                    sender.getName(), warp.getName()));
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.give.asked-successful", sender, givee.getName(), warp.getName()));
        }
    }

    @Command(aliases = { "accept" }, usage = "", desc = "commands.accept.description", fee = Fee.ACCEPT, max = 0, permissions = { "mywarp.warp.soc.accept" })
    public void acceptGivenWarp(CommandContext args, Player sender) throws CommandException {
        if (!givenWarps.containsKey(sender.getName())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.accept.nothing-to-accept", sender));
        }
        Warp warp = givenWarps.get(sender.getUniqueId());
        warp.setCreatorId(sender.getUniqueId());
        givenWarps.remove(sender.getName());

        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.accept.accepted-successful", sender, warp.getName()));
    }

    @Command(aliases = { "invite" }, usage = "<player> <name>", desc = "commands.invite.description", fee = Fee.INVITE, min = 2, permissions = { "mywarp.warp.soc.invite" })
    public void inviteToWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(1));

        // invite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.invite.group");

            String group = args.getString(0).substring(2);

            if (warp.getInvitedGroups().contains(group)) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.invite.group.already-invited", sender, group));
            }

            warp.inviteGroup(group);

            switch (warp.getType()) {
            case PRIVATE:
                sender.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.invite.group.private", sender, group, warp.getName()));
                break;
            case PUBLIC:
                sender.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.invite.group.public", sender, group, warp.getName()));
                break;
            }
            return;

        }
        // invite player
        OfflinePlayer invitee = CommandUtils.matchOfflinePlayer(args.getString(0));

        if (warp.getInvitedPlayerIds().contains(invitee.getUniqueId())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invite.player.already-invited", sender, invitee.getName()));
        }

        if (warp.isCreator(invitee)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invite.player.is-creator", sender, invitee.getName()));
        }

        warp.invitePlayer(invitee.getUniqueId());
        switch (warp.getType()) {
        case PUBLIC:
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invite.player.public", sender, invitee.getName(), warp.getName()));
            break;
        case PRIVATE:
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.invite.player.private", sender, invitee.getName(), warp.getName()));
            break;
        }

        if (invitee.getPlayer() != null) {
            invitee.getPlayer().sendMessage(
                    MyWarp.inst()
                            .getLocalizationManager()
                            .getString("commands.invite.player.player-invited", sender, sender.getName(),
                                    warp.getName()));
        }

    }

    @Command(aliases = { "private" }, usage = "<name>", desc = "commands.private.description", fee = Fee.PRIVATE, min = 1, permissions = { "mywarp.warp.soc.private" })
    public void privatizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(0));
        if (warp.getType() == Type.PRIVATE) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.private.already-private", sender, warp.getName()));
        }
        CommandUtils.checkLimits(sender, false, Type.PRIVATE);

        warp.setType(Type.PRIVATE);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.private.privatized", sender, warp.getName()));
    }

    @Command(aliases = { "public" }, usage = "<name>", desc = "commands.public.description", fee = Fee.PUBLIC, min = 1, permissions = { "mywarp.warp.soc.public" })
    public void publicizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(0));
        if (warp.getType() == Type.PUBLIC) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.public.already-public", sender, warp.getName()));
        }
        CommandUtils.checkLimits(sender, false, Type.PUBLIC);

        warp.setType(Type.PUBLIC);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getString("commands.public.publicized", sender, warp.getName()));
    }

    @Command(aliases = { "uninvite" }, usage = "<player> <name>", desc = "commands.uninvite.description", fee = Fee.UNINVITE, min = 2, permissions = { "mywarp.warp.soc.uninvite" })
    public void uninviteFromWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getModifiableWarp(sender, args.getJoinedStrings(1));

        // uninvite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.uninvite.group");
            String group = args.getString(0).substring(2);

            if (!warp.getInvitedGroups().contains(group)) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getString("commands.uninvite.group.not-invited", sender, group));
            }

            warp.uninviteGroup(group);

            switch (warp.getType()) {
            case PUBLIC:
                sender.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.uninvite.group.public", sender, group, warp.getName()));
                break;
            case PRIVATE:
                sender.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getString("commands.uninvite.group.private", sender, group, warp.getName()));
                break;
            }
            return;
        }
        // uninvite player
        OfflinePlayer invitee = CommandUtils.matchOfflinePlayer(args.getString(0));

        if (!warp.getInvitedPlayerIds().contains(invitee.getUniqueId())) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.uninvite.player.not-invited", sender, invitee.getName()));
        }

        if (warp.getCreatorId() == invitee.getUniqueId()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getString("commands.uninvite.player.is-creator", sender, invitee.getName()));
        }

        warp.uninvitePlayer(invitee.getUniqueId());

        switch (warp.getType()) {
        case PUBLIC:
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.uninvite.player.public", sender, invitee.getName(), warp.getName()));
            break;
        case PRIVATE:
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("commands.uninvite.player.private", sender, invitee.getName(), warp.getName()));
            break;
        }

        if (invitee.getPlayer() != null) {
            invitee.getPlayer().sendMessage(
                    MyWarp.inst().getLocalizationManager()
                            .getString("commands.uninvite.player.player-uninvited", sender, warp.getName()));
        }
    }
}

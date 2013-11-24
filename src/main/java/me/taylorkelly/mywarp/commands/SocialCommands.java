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
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("error.player.offline", sender, "%player%", args.getString(0)));
            }
            giveeName = args.getString(0);
        } else {
            giveeName = givee.getName();
        }

        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        if (warp.playerIsCreator(giveeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.give.isOwner", sender, "%player%", giveeName));
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
                givee.sendMessage(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("warp.give.accept", sender, "%warp%", warp.getName()));
            }
        } else {
            givenWarps.put(giveeName, warp);
            givee.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("warp.give.asked", sender, "%warp%", warp.getName(), "%player%",
                            sender.getName()));
        }
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("warp.give.given", sender, "%warp%", warp.getName(), "%player%", giveeName));
    }

    @Command(aliases = { "accept" }, usage = "", desc = "cmd.description.accept", fee = Fee.ACCEPT, max = 0, permissions = { "mywarp.warp.soc.accept" })
    public void acceptGivenWarp(CommandContext args, CommandSender sender) throws CommandException {
        if (!givenWarps.containsKey(sender.getName())) {
            // TODO translate
            throw new CommandException(MyWarp.inst().getLocalizationManager().getString("error.accept.noWarp", sender));
        }
        Warp warp = givenWarps.get(sender.getName());
        warp.setCreator(sender.getName());
        givenWarps.remove(sender.getName());

        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("warp.give.accept", sender, "%warp%", warp.getName()));
    }

    @Command(aliases = { "invite" }, usage = "<player> <name>", desc = "cmd.description.invite", fee = Fee.INVITE, min = 2, permissions = { "mywarp.warp.soc.invite" })
    public void inviteToWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        // invite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.invite.group");

            String inviteeName = args.getString(0).substring(2);

            if (warp.groupIsInvited(inviteeName)) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("error.invite.invited.group", sender, "%group%", inviteeName));
            }

            warp.inviteGroup(inviteeName);

            if (warp.isPublicAll()) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("warp.invite.group.public", sender, "%warp%", warp.getName(), "%group%",
                                inviteeName));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("warp.invite.group.private", sender, "%warp%", warp.getName(), "%group%",
                                inviteeName));
            }
            return;
        }
        // invite player
        Player invitee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee.getName();

        if (warp.playerIsInvited(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.invite.invited.player", sender, "%player%", inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.invite.creator", sender, "%player%", inviteeName));
        }

        warp.invite(inviteeName);
        if (warp.isPublicAll()) {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("warp.invite.player.public", sender, "%warp%", warp.getName(), "%player%",
                            inviteeName));
        } else {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("warp.invite.player.private", sender, "%warp%", warp.getName(), "%player%",
                            inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("warp.invite.invited", sender, "%warp%", warp.getName(), "%player%",
                            sender.getName()));
        }

    }

    @Command(aliases = { "private" }, usage = "<name>", desc = "cmd.description.private", fee = Fee.PRIVATE, min = 1, permissions = { "mywarp.warp.soc.private" })
    public void privatizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));
        if (!warp.isPublicAll()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.private.isPrivate", sender, "%warp%", warp.getName()));
        }
        CommandUtils.checkPrivateLimit(sender);

        warp.setPublicAll(false);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("warp.private", sender, "%warp%", warp.getName()));
    }

    @Command(aliases = { "public" }, usage = "<name>", desc = "cmd.description.public", fee = Fee.PUBLIC, min = 1, permissions = { "mywarp.warp.soc.public" })
    public void publicizeWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(0));
        if (warp.isPublicAll()) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.public.isPublic", sender, "%warp%", warp.getName()));
        }
        CommandUtils.checkPublicLimit(sender);

        warp.setPublicAll(true);
        sender.sendMessage(MyWarp.inst().getLocalizationManager()
                .getEffectiveString("warp.public", sender, "%warp%", warp.getName()));
    }

    @Command(aliases = { "uninvite" }, usage = "<player> <name>", desc = "cmd.description.uninvite", fee = Fee.UNINVITE, min = 2, permissions = { "mywarp.warp.soc.uninvite" })
    public void uninviteFromWarp(CommandContext args, CommandSender sender) throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender, args.getJoinedStrings(1));

        // uninvite group
        if (args.getString(0).startsWith("g:")) {
            CommandUtils.checkPermissions(sender, "mywarp.warp.soc.uninvite.group");
            String inviteeName = args.getString(0).substring(2);

            if (!warp.groupIsInvited(inviteeName)) {
                throw new CommandException(MyWarp.inst().getLocalizationManager()
                        .getEffectiveString("error.uninvite.notInvited.group", sender, "%group%", inviteeName));
            }

            warp.uninviteGroup(inviteeName);

            if (warp.isPublicAll()) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("warp.uninvite.group.public", sender, "%warp%", warp.getName(),
                                "%group%", inviteeName));
            } else {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getEffectiveString("warp.uninvite.group.private", sender, "%warp%", warp.getName(),
                                "%group%", inviteeName));
            }
            return;
        }
        // uninvite player
        Player invitee = MyWarp.inst().getServer().getPlayer(args.getString(0));
        String inviteeName = (invitee == null) ? args.getString(0) : invitee.getName();

        if (!warp.playerIsInvited(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.uninvite.notInvited.player", sender, "%player%", inviteeName));
        }

        if (warp.playerIsCreator(inviteeName)) {
            throw new CommandException(MyWarp.inst().getLocalizationManager()
                    .getEffectiveString("error.uninvite.creator", sender, "%player%", inviteeName));
        }

        warp.uninvite(inviteeName);

        if (warp.isPublicAll()) {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("warp.uninvite.player.public", sender, "%warp%", warp.getName(), "%player%",
                            inviteeName));
        } else {
            sender.sendMessage(MyWarp
                    .inst()
                    .getLocalizationManager()
                    .getEffectiveString("warp.uninvite.player.private", sender, "%warp%", warp.getName(), "%player%",
                            inviteeName));
        }

        if (invitee != null) {
            invitee.sendMessage(MyWarp.inst().getLocalizationManager().getEffectiveString("warp.uninvite.uninvited", sender, "%warp%", warp.getName()));
        }
    }
}

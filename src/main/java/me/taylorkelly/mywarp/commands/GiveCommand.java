package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public GiveCommand(MyWarp plugin) {
        super("Give");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.give"));
        setUsage("<" + LanguageManager.getColorlessString("help.usage.player")
                + "> <" + LanguageManager.getColorlessString("help.usage.name")
                + ">");
        setArgumentRange(2, 255);
        setIdentifiers("give");
        setPermission("mywarp.warp.soc.give");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {

        Player givee = plugin.getServer().getPlayer(args[0]);
        String giveeName;

        // TODO simplify
        if (WarpSettings.useWarpLimits) {
            if (givee == null) {

                throw new CommandException(LanguageManager.getEffectiveString(
                        "error.player.offline", "%player%", args[0]));
            }
            giveeName = givee.getName();
        } else {
            giveeName = args[0];
        }
        Warp warp = CommandUtils.getWarpForModification(sender,
                CommandUtils.toWarpName(args, 1));

        if (warp.playerIsCreator(giveeName)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.give.isOwner", "%player%", giveeName));
        }
        CommandUtils.checkPlayerLimits(givee, warp.publicAll);

        plugin.getWarpList().give(warp, giveeName);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.give.given", "%warp%", warp.name, "%player%", giveeName));
        if (givee != null) {
            givee.sendMessage(LanguageManager.getEffectiveString(
                    "warp.give.received", "%warp%", warp.name, "%player%",
                    sender.getName()));
        }
    }
}

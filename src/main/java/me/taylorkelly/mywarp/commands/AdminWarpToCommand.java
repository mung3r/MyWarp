package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminWarpToCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public AdminWarpToCommand(MyWarp plugin) {
        super("WarpPlayer");
        this.plugin = plugin;
        setDescription(LanguageManager
                .getString("help.description.adminWarpTo"));
        setUsage("/warp player §8<"
                + LanguageManager.getColorlessString("help.usage.player")
                + "> §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(2, 255);
        setIdentifiers("player");
        setPermission("mywarp.admin.warpto");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Player invitee = CommandUtils.checkPlayer(args[0]);
        Warp warp = CommandUtils.getWarpForUsage(sender,
                CommandUtils.toWarpName(args, 1));

        plugin.getWarpList().warpTo(warp, invitee);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.warpto.player", "%player%", invitee.getName()));
    }
}

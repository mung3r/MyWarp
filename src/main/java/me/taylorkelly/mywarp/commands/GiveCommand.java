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
        setUsage("/warp give §8<"
                + LanguageManager.getColorlessString("help.usage.player")
                + "> §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(2, 255);
        setIdentifiers("give");
        setPermission("mywarp.warp.soc.give");
    }

    @Override
    public void execute(CommandSender sender, String identifier,
            String[] args) throws CommandException {

        Player givee = plugin.getServer().getPlayer(args[0]);
        String giveeName;
        
        //TODO simplify
        if (WarpSettings.useWarpLimits) {
            if (givee == null) {
                
                throw new CommandException(LanguageManager.getString(
                        "error.player.offline").replaceAll("%player%",
                        args[0]));
            }
            giveeName = givee.getName();
        } else {
            giveeName = args[0];
        }
        Warp warp = CommandUtils.getWarpForModification(sender, CommandUtils.toWarpName(args, 1));

        if (warp.playerIsCreator(giveeName)) {
            throw new CommandException(LanguageManager
                    .getString("error.give.isOwner").replaceAll("%player%",
                            giveeName));
        }
        CommandUtils.checkPlayerLimits(givee, warp.publicAll);

        plugin.getWarpList().give(warp, giveeName);
        sender.sendMessage(LanguageManager.getString("warp.give.given")
                .replaceAll("%warp%", warp.name)
                .replaceAll("%player%", giveeName));
        if (givee != null) {
            givee.sendMessage(LanguageManager.getString("warp.give.received")
                    .replaceAll("%warp%", warp.name)
                    .replaceAll("%player%", sender.getName()));
        }
    }
}

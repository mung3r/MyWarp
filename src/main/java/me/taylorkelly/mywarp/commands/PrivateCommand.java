package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;

public class PrivateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public PrivateCommand(MyWarp plugin) {
        super("Private");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.private"));
        setUsage("<" + LanguageManager.getColorlessString("help.usage.name")
                + ">");
        setArgumentRange(1, 255);
        setIdentifiers("private");
        setPermission("mywarp.warp.soc.private");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {

        Warp warp = CommandUtils.getWarpForModification(sender,
                CommandUtils.toWarpName(args));
        if (!warp.publicAll) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.private.isPrivate", "%warp%", warp.name));
        }
        CommandUtils.checkLimits(sender, false);

        plugin.getWarpList().privatize(warp);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.private",
                "%warp%", warp.name));
    }
}

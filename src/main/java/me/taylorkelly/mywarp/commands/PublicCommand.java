package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;

public class PublicCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public PublicCommand(MyWarp plugin) {
        super("Public");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.public"));
        setUsage("<" + LanguageManager.getColorlessString("help.usage.name")
                + ">");
        setArgumentRange(1, 255);
        setIdentifiers("public");
        setPermission("mywarp.warp.soc.public");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForModification(sender,
                CommandUtils.toWarpName(args));
        CommandUtils.checkLimits(sender, false);

        plugin.getWarpList().publicize(warp);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.public",
                "%warp%", warp.name));
    }
}

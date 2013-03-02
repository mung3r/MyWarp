package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;

public class DeleteCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public DeleteCommand(MyWarp plugin) {
        super("Delete");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.delete"));
        setUsage("<" + LanguageManager.getColorlessString("help.usage.name")
                + ">");
        setArgumentRange(1, 255);
        setIdentifiers("delete", "remove");
        setPermission("mywarp.warp.basic.delete");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {

        Warp warp = CommandUtils.getWarpForModification(sender,
                CommandUtils.toWarpName(args));

        plugin.getWarpList().deleteWarp(warp);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.delete",
                "%warp%", warp.name));
    }
}

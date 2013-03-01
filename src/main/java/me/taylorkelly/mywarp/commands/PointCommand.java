package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PointCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public PointCommand(MyWarp plugin) {
        super("Point");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.point"));
        setUsage("/warp point §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("point");
        setPermission("mywarp.warp.basic.compass");
        setPlayerOnly(true);
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Warp warp = CommandUtils.getWarpForUsage(sender,
                CommandUtils.toWarpName(args));

        plugin.getWarpList().point(warp, (Player) sender);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.point",
                "%warp%", warp.name));
    }
}

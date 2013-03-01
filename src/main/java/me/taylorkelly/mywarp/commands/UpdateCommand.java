package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public UpdateCommand(MyWarp plugin) {
        super("Update");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.update"));
        setUsage("/warp update §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("update");
        setPermission("mywarp.warp.basic.update");
        setPlayerOnly(true);
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Player player = (Player) sender;

        Warp warp = CommandUtils.getWarpForModification(sender,
                CommandUtils.toWarpName(args));

        plugin.getWarpList().updateLocation(warp, player);
        sender.sendMessage(LanguageManager.getEffectiveString("warp.update",
                "%warp%", warp.name));
    }
}

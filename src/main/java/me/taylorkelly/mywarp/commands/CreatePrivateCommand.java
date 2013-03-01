package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatePrivateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public CreatePrivateCommand(MyWarp plugin) {
        super("pcreate");
        this.plugin = plugin;
        setDescription(LanguageManager
                .getString("help.description.createPrivate"));
        setUsage("/warp pcreate §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("pcreate");
        setPermission("mywarp.warp.basic.createprivate");
        setPlayerOnly(true);
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Player player = (Player) sender;
        String name = CommandUtils.toWarpName(args);

        CommandUtils.checkLimits(sender, false);

        if (plugin.getWarpList().warpExists(name)) {
            throw new CommandException(LanguageManager.getEffectiveString(
                    "error.create.warpExists", "%warp%", name));
        }

        plugin.getWarpList().addWarpPrivate(name, player);
        sender.sendMessage(LanguageManager.getEffectiveString(
                "warp.create.private", "%warp%", name));
    }
}

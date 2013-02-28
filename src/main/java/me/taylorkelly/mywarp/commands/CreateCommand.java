package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public CreateCommand(MyWarp plugin) {
        super("Create");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.create"));
        setUsage("/warp create|set §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("create", "set");
        setPermission("mywarp.warp.basic.createpublic");
        setPlayerOnly(true);
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Player player = (Player) sender;
        String name = CommandUtils.toWarpName(args);

        CommandUtils.checkLimits(sender, true);

        if (plugin.getWarpList().warpExists(name)) {
            throw new CommandException(LanguageManager.getString(
                    "error.create.warpExists").replaceAll("%warp%", name));
        }

        plugin.getWarpList().addWarpPublic(name, player);
        player.sendMessage(LanguageManager.getString("warp.create.public")
                .replaceAll("%warp%", name));
    }
}

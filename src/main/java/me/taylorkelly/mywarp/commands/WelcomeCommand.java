package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WelcomeCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public WelcomeCommand(MyWarp plugin) {
        super("Welcome");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.welcome"));
        setUsage("/warp welcome §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("welcome");
        setPermission("mywarp.warp.basic.welcome");
        setPlayerOnly(true);
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args)
            throws CommandException {
        Player player = (Player) sender;

        Warp warp = CommandUtils.getWarpForModification(sender,
                CommandUtils.toWarpName(args));

        plugin.getWarpList().welcomeMessage(warp, player);
        player.sendMessage(LanguageManager.getString("warp.welcome.enter")
                .replaceAll("%warp%", warp.name));
    }
}

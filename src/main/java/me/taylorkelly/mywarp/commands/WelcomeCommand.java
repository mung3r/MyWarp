package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
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
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        if (executor instanceof Player) {
            Player player = (Player) executor;
            String name = plugin.getWarpList().getMatche(
                    StringUtils.join(args, ' '), player);

            if (!plugin.getWarpList().warpExists(name)) {
                player.sendMessage(LanguageManager
                        .getString("error.noSuchWarp").replaceAll("%warp%",
                                name));
                return true;
            }

            Warp warp = plugin.getWarpList().getWarp(name);

            if (!warp.playerCanModify(player)) {
                player.sendMessage(LanguageManager.getString(
                        "error.noPermission.update").replaceAll("%warp%", name));
                return true;
            }

            plugin.getWarpList().welcomeMessage(name, player);
            player.sendMessage(LanguageManager.getString("warp.welcome.enter")
                    .replaceAll("%warp%", name));
            return true;

        } else {
            executor.sendMessage(LanguageManager
                    .getString("error.consoleSender.welcome"));
            return true;
        }
    }
}

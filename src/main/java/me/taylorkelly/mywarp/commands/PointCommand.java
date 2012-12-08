package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
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

            if (!warp.playerCanWarp(player)) {
                player.sendMessage(LanguageManager.getString(
                        "error.noPermission.point").replaceAll("%warp%", name));
                return true;
            }

            plugin.getWarpList().point(StringUtils.join(args, ' '),
                    (Player) executor);
            player.sendMessage(LanguageManager.getString("warp.point")
                    .replaceAll("%warp%", name));
            return true;
        } else {
            executor.sendMessage(LanguageManager
                    .getString("error.consoleSender.point"));
            return true;
        }
    }
}

package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public UpdateCommand(MyWarp plugin) {
        super("Update");
        this.plugin = plugin;
        setDescription("Updates existing §9<name> with your current position");
        setUsage("/warp update §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("update");
        setPermission("mywarp.warp.basic.update");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        if (executor instanceof Player) {
            Player player = (Player) executor;
            String name = plugin.getWarpList().getMatche(StringUtils.join(args, ' '),
                    player);

            if (!plugin.getWarpList().warpExists(name)) {
                player.sendMessage(LanguageManager.getString("error.noSuchWarp").replaceAll("%warp%", name));
                return true;
            }

            Warp warp = plugin.getWarpList().getWarp(name);

            if (!warp.playerCanModify(player)) {
                player.sendMessage(LanguageManager.getString("error.noPermission.update").replaceAll("%warp%", name));
                return true;
            }

            plugin.getWarpList().updateLocation(name, player);
            player.sendMessage(LanguageManager.getString("warp.update").replaceAll("%warp%", name));
            return true;
        } else {
            executor.sendMessage(LanguageManager.getString("error.consoleSender.update"));
            return true;
        }
    }
}

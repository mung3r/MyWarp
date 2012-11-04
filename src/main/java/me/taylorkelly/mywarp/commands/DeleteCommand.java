package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public DeleteCommand(MyWarp plugin) {
        super("Delete");
        this.plugin = plugin;
        setDescription("Deletes the warp §9<name>");
        setUsage("/warp delete §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("delete", "remove");
        setPermission("mywarp.warp.basic.delete");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        String name = plugin.getWarpList()
                .getMatche(StringUtils.join(args, ' '), player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(LanguageManager.getString("error.noSuchWarp").replaceAll("%warp%", name));
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null ? !warp.playerCanModify(player) : false) {
            executor.sendMessage(LanguageManager.getString("error.noPermission.delete").replaceAll("%warp%", name));
            return true;
        }

        plugin.getWarpList().deleteWarp(name);
        executor.sendMessage(LanguageManager.getString("warp.delete").replaceAll("%warp%", name));
        return true;
    }
}

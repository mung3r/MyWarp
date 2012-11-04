package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListAllCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public ListAllCommand(MyWarp plugin) {
        super("ListAll");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.listAll"));
        setUsage("/warp slist");
        setArgumentRange(0, 0);
        setIdentifiers("slist");
        setPermission("mywarp.warp.basic.list");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        plugin.getWarpList().list(executor, player);
        return true;
    }
}

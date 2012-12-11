package me.taylorkelly.mywarp.commands;

import java.util.ArrayList;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

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
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }
        ArrayList<Warp> results = plugin.getWarpList().warpsInvitedTo(player);

        if (results.size() == 0) {
            executor.sendMessage(LanguageManager.getString("alist.noWarps"));
            return true;
        }
        executor.sendMessage(LanguageManager.getString("alist.list"));
        executor.sendMessage(results.toString().replace("[", "")
                .replace("]", ""));
        return true;
    }
}

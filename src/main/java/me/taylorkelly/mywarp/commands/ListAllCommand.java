package me.taylorkelly.mywarp.commands;

import java.util.TreeSet;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListAllCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public ListAllCommand(MyWarp plugin) {
        super("ListAll");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.listAll"));
        setUsage("");
        setArgumentRange(0, 0);
        setIdentifiers("slist");
        setPermission("mywarp.warp.basic.list");
    }

    @Override
    public void execute(CommandSender sender, String identifier, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        TreeSet<Warp> results = plugin.getWarpList().warpsInvitedTo(player);

        if (results.isEmpty()) {
            sender.sendMessage(LanguageManager.getString("listAll.noWarps"));
        }
        sender.sendMessage(LanguageManager.getString("listAll.list"));
        sender.sendMessage(StringUtils.join(results, ", "));
    }
}

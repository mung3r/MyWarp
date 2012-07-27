package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Lister;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends BasicCommand implements Command
{
    private MyWarp plugin;

    public ListCommand(MyWarp plugin)
    {
        super("List");
        this.plugin = plugin;
        setDescription("List the warps you can visit");
        setUsage("/warp list §8[page#]");
        setArgumentRange(0, 1);
        setIdentifiers("list");
        setPermission("mywarp.warp.basic.list");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args)
    {
        if (executor instanceof Player) {
            Lister lister = new Lister(plugin.getWarpList());
            lister.addPlayer((Player) executor);

            if (args.length > 0) {
                int page = Integer.parseInt(args[0]);
                if (page < 1) {
                    executor.sendMessage(ChatColor.RED + "Page number can't be below 1.");
                    return true;
                }
                else if (page > lister.getMaxPages((Player) executor)) {
                    executor.sendMessage(ChatColor.RED + "There are only " + lister.getMaxPages((Player) executor) + " pages of warps");
                    return true;
                }
                lister.setPage(page);
            }
            else {
                lister.setPage(1);
            }
            lister.list();
        }
        else {
            executor.sendMessage("Console cannot list warps for themselves!");
        }

        return true;
    }
}

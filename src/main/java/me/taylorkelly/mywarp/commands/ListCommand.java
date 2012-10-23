package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Lister;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public ListCommand(MyWarp plugin) {
        super("List");
        this.plugin = plugin;
        setDescription("List the warps you can visit");
        setUsage("/warp list §8[owner] §9[page#]");
        setArgumentRange(0, 2);
        setIdentifiers("list");
        setPermission("mywarp.warp.basic.list");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args) {
        Player player = null;
        if (executor instanceof Player) {
            player = (Player) executor;
        }
        Lister lister = new Lister(plugin.getWarpList());
        lister.addExecutor(executor, player);

        if (args.length == 0) {
            lister.setPage(1);
        } else if (args.length == 1) {
            if (args[0].matches("-?\\d+(\\.\\d+)?")) {
                int page = Integer.parseInt(args[0]);
                if (page < 1) {
                    executor.sendMessage(ChatColor.RED + "Page number can't be below 1.");
                    return true;
                } else if (page > lister.getMaxPages(player)) {
                    executor.sendMessage(ChatColor.RED + "There are only "
                            + lister.getMaxPages(player) + " pages of warps");
                    return true;
                }
                lister.setPage(page);
            } else {
                if (args[0].equals("own")) {
                    if (executor instanceof Player) {
                        lister.setWarpCreator(player.getName());
                    } else {
                        executor.sendMessage("Console does not own any warps.");
                        return true;
                    }

                } else {
                    lister.setWarpCreator(args[0]);
                }
                lister.setPage(1);
            }
        } else if (args.length == 2) {
            String creator = null;
            if (args[0].equals("own")) {
                if (executor instanceof Player) {
                    lister.setWarpCreator(player.getName());
                } else {
                    executor.sendMessage("Console does not own any warps.");
                    return true;
                }
            } else {
                creator = args[0];
            }
            lister.setWarpCreator(creator);
            int page = Integer.parseInt(args[1]);
            if (page < 1) {
                executor.sendMessage(ChatColor.RED + "Page number can't be below 1.");
                return true;
            } else if (page > lister.getMaxPagesPerCreator(player, creator)) {
                executor.sendMessage(ChatColor.RED + "There are only "
                        + lister.getMaxPagesPerCreator(player, creator)
                        + " pages of warps");
                return true;
            }
            lister.setPage(page);
        } else {
            return false;
        }
        lister.list();
        return true;
    }
}

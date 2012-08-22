package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateCommand extends BasicCommand implements Command
{
    private MyWarp plugin;

    public UpdateCommand(MyWarp plugin)
    {
        super("Update");
        this.plugin = plugin;
        setDescription("Updates existing §9<name> with your current position");
        setUsage("/warp update §9<name>");
        setArgumentRange(1, 255);
        setIdentifiers("update");
        setPermission("mywarp.warp.basic.update");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier, String[] args)
    {
        if (executor instanceof Player) {
            plugin.getWarpList().updateLocation(StringUtils.join(args, ' '), (Player) executor);
        }
        else {
            executor.sendMessage("Console cannot update warps for themselves!");
        }

        return true;
    }
}

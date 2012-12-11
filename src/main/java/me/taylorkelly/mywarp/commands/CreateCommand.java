package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public CreateCommand(MyWarp plugin) {
        super("Create");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.create"));
        setUsage("/warp create|set §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("create", "set");
        setPermission("mywarp.warp.basic.createpublic");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        if (executor instanceof Player) {
            Player player = (Player) executor;
            String name = StringUtils.join(args, ' ');

            if (WarpSettings.useWarpLimits) {
                if (!plugin.getWarpList().playerCanBuildWarp(player)) {
                    player.sendMessage(LanguageManager.getString(
                            "limit.total.reached").replaceAll(
                            "%maxTotal%",
                            Integer.toString(MyWarp.getWarpPermissions()
                                    .maxTotalWarps(player))));
                    return true;
                }

                if (!plugin.getWarpList().playerCanBuildPublicWarp(player)) {
                    player.sendMessage(LanguageManager.getString(
                            "limit.public.reached").replaceAll(
                            "%maxPublic%",
                            Integer.toString(MyWarp.getWarpPermissions()
                                    .maxPublicWarps(player))));
                    return true;
                }
            }

            if (plugin.getWarpList().warpExists(name)) {
                player.sendMessage(LanguageManager.getString(
                        "error.create.warpExists").replaceAll("%warp%", name));
                return true;
            }

            plugin.getWarpList().addWarpPublic(name, player);
            player.sendMessage(LanguageManager.getString("warp.create.public")
                    .replaceAll("%warp%", name));
            return true;
        } else {
            executor.sendMessage(LanguageManager
                    .getString("error.consoleSender.create"));
            return true;
        }
    }
}

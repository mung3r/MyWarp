package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatePrivateCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public CreatePrivateCommand(MyWarp plugin) {
        super("pcreate");
        this.plugin = plugin;
        setDescription(LanguageManager
                .getString("help.description.createPrivate"));
        setUsage("/warp pcreate §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("pcreate");
        setPermission("mywarp.warp.basic.createprivate");
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

                if (!plugin.getWarpList().playerCanBuildPrivateWarp(player)) {
                    player.sendMessage(LanguageManager.getString(
                            "limit.private.reached").replaceAll(
                            "%maxPrivate%",
                            Integer.toString(MyWarp.getWarpPermissions()
                                    .maxPrivateWarps(player))));
                    return true;
                }
            }

            if (plugin.getWarpList().warpExists(name)) {
                player.sendMessage(LanguageManager.getString(
                        "error.create.warpExists").replaceAll("%warp%", name));
                return true;
            }

            plugin.getWarpList().addWarpPrivate(name, player);
            player.sendMessage(LanguageManager.getString("warp.create.private")
                    .replaceAll("%warp%", name));
            return true;
        } else {
            executor.sendMessage(LanguageManager
                    .getString("error.consoleSender.warpto"));
            return true;
        }
    }
}

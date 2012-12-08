package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PublicCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public PublicCommand(MyWarp plugin) {
        super("Public");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.public"));
        setUsage("/warp public §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(1, 255);
        setIdentifiers("public");
        setPermission("mywarp.warp.soc.public");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }
        String name = plugin.getWarpList().getMatche(
                StringUtils.join(args, ' '), player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(LanguageManager.getString("error.noSuchWarp")
                    .replaceAll("%warp%", name));
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null && !warp.playerCanModify(player)) {
            executor.sendMessage(LanguageManager
                    .getString("error.noPermission.public"));
            return true;
        }

        if (player != null
                && (WarpSettings.useWarpLimits && !plugin.getWarpList()
                        .playerCanBuildPrivateWarp(player))) {
            executor.sendMessage(LanguageManager.getString(
                    "limit.public.reached").replaceAll(
                    "%maxPublic%",
                    Integer.toString(MyWarp.getWarpPermissions()
                            .maxPublicWarps(player))));
            return true;
        }

        plugin.getWarpList().publicize(name);
        executor.sendMessage(LanguageManager.getString("warp.public")
                .replaceAll("%warp%", name));
        return true;
    }
}

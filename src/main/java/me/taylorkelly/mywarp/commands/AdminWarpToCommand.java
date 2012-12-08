package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminWarpToCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public AdminWarpToCommand(MyWarp plugin) {
        super("WarpPlayer");
        this.plugin = plugin;
        setDescription(LanguageManager
                .getString("help.description.adminWarpTo"));
        setUsage("/warp player §8<"
                + LanguageManager.getColorlessString("help.usage.player")
                + "> §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(2, 255);
        setIdentifiers("player");
        setPermission("mywarp.admin.warpto");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        Player invitee = plugin.getServer().getPlayer(args[0]);
        String name = plugin.getWarpList().getMatche(
                StringUtils.join(Arrays.asList(args).subList(1, args.length),
                        ' '), player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(LanguageManager.getString("error.noSuchWarp")
                    .replaceAll("%warp%", name));
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null && !warp.playerCanWarp(player)) {
            executor.sendMessage(LanguageManager.getString(
                    "error.noPermission.warpto").replaceAll("%warp%", name));
            return true;
        }

        if (WarpSettings.worldAccess
                && player != null
                && !plugin.getWarpList().playerCanAccessWorld(player,
                        warp.world)) {
            player.sendMessage(LanguageManager.getString(
                    "error.noPermission.world").replaceAll("%world%",
                    warp.world));
            return true;
        }

        if (invitee == null) {
            executor.sendMessage(LanguageManager
                    .getString("error.playerOffline.warpto"));
            return true;
        }

        plugin.getWarpList().warpTo(name, invitee);
        executor.sendMessage(LanguageManager.getString("warp.warpto.player"));
        return true;
    }
}

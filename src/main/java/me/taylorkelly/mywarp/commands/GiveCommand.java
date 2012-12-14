package me.taylorkelly.mywarp.commands;

import java.util.Arrays;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public GiveCommand(MyWarp plugin) {
        super("Give");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.give"));
        setUsage("/warp give §8<"
                + LanguageManager.getColorlessString("help.usage.player")
                + "> §9<"
                + LanguageManager.getColorlessString("help.usage.name") + ">");
        setArgumentRange(2, 255);
        setIdentifiers("give");
        setPermission("mywarp.warp.soc.give");
    }

    @Override
    public boolean execute(CommandSender executor, String identifier,
            String[] args) {
        Player player = null;

        if (executor instanceof Player) {
            player = (Player) executor;
        }

        Player givee = plugin.getServer().getPlayer(args[0]);
        String giveeName;

        if (WarpSettings.useWarpLimits) {
            if (givee == null) {
                executor.sendMessage(LanguageManager.getString(
                        "error.playerOffline.give").replaceAll("%player%",
                        args[0]));
                return true;
            }
            giveeName = givee.getName();
        } else {
            giveeName = args[0];
        }

        String name = plugin.getWarpList().getMatche(
                StringUtils.join(Arrays.asList(args).subList(1, args.length),
                        ' '), player);

        if (!plugin.getWarpList().warpExists(name)) {
            executor.sendMessage(LanguageManager.getString("error.noSuchWarp")
                    .replaceAll("%warp%", name));
            return true;
        }

        Warp warp = plugin.getWarpList().getWarp(name);

        if (player != null && !warp.playerCanModify(player)) {
            executor.sendMessage(LanguageManager
                    .getString("error.noPermission.give")
                    .replaceAll("%warp%", name)
                    .replaceAll("%player%", giveeName));
            return true;
        }

        if (warp.playerIsCreator(giveeName)) {
            executor.sendMessage(LanguageManager
                    .getString("error.give.isOwner").replaceAll("%player%",
                            giveeName));
            return true;
        }

        if (WarpSettings.useWarpLimits) {
            if (!plugin.getWarpList().playerCanBuildWarp(givee)) {
                executor.sendMessage(LanguageManager.getString(
                        "limit.total.reached.player").replaceAll(
                        "%maxTotal%",
                        Integer.toString(
                                MyWarp.getWarpPermissions()
                                        .maxTotalWarps(givee)).replaceAll(
                                "%player%", giveeName)));
                return true;
            }
            if (warp.publicAll
                    && !plugin.getWarpList().playerCanBuildPublicWarp(givee)) {
                executor.sendMessage(LanguageManager.getString(
                        "limit.public.reached.player").replaceAll(
                        "%maxPublic%",
                        Integer.toString(
                                MyWarp.getWarpPermissions().maxPublicWarps(
                                        givee)).replaceAll("%player%",
                                giveeName)));
                return true;
            }
            if (!warp.publicAll
                    && !plugin.getWarpList().playerCanBuildPrivateWarp(givee)) {
                executor.sendMessage(LanguageManager.getString(
                        "limit.private.reached.player").replaceAll(
                        "%maxPrivate%",
                        Integer.toString(
                                MyWarp.getWarpPermissions().maxPrivateWarps(
                                        givee)).replaceAll("%player%",
                                giveeName)));
                return true;
            }
        }

        plugin.getWarpList().give(name, giveeName);
        executor.sendMessage(LanguageManager.getString("warp.give.given")
                .replaceAll("%warp%", name).replaceAll("%player%", giveeName));
        if (WarpSettings.useWarpLimits || givee != null) {
            givee.sendMessage(LanguageManager.getString("warp.give.received")
                    .replaceAll("%warp%", name)
                    .replaceAll("%player%", executor.getName()));
        }
        return true;
    }
}

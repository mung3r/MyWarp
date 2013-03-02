package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.timer.PlayerCooldown;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.CommandUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpToCommand extends BasicCommand implements Command {
    private MyWarp plugin;

    public WarpToCommand(MyWarp plugin) {
        super("WarpTo");
        this.plugin = plugin;
        setDescription(LanguageManager.getString("help.description.warpTo"));
        setUsage("<" + LanguageManager.getColorlessString("help.usage.name")
                + ">");
        setArgumentRange(1, 255);
        setIdentifiers("warp", "mywarp", "mw");
        setPermission("mywarp.warp.basic.warp");
        setPlayerOnly(true);
    }

    @Override
    public void execute(final CommandSender sender, String identifier,
            final String[] args) throws CommandException {
        Player player = (Player) sender;

        Warp warp = CommandUtils.getWarpForUsage(sender,
                CommandUtils.toWarpName(args));
        if (WarpSettings.useTimers) {
            Time cooldown = MyWarp.getWarpPermissions().getCooldown(player);
            Time warmup = MyWarp.getWarpPermissions().getWarmup(player);

            if (PlayerCooldown.isActive(player.getName())) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "timer.cooldown.cooling", "%seconds%",
                        Integer.toString(PlayerCooldown
                                .getRemainingCooldown(player.getName()))));
            }

            if (PlayerWarmup.isActive(player.getName())) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "timer.warmup.warming", "%seconds%", Integer
                                .toString(PlayerWarmup
                                        .getRemainingWarmup(player.getName()))));
            }

            if (MyWarp.getWarpPermissions().disobeyWarmup(player)) {
                plugin.getWarpList().warpTo(warp, player);

                if (!MyWarp.getWarpPermissions().disobeyCooldown(player)) {
                    new PlayerCooldown(plugin, player, cooldown);
                }
                return;
            }

            new PlayerWarmup(plugin, player, warmup, warp, cooldown);

            if (WarpSettings.warmUpNotify) {
                player.sendMessage(LanguageManager.getEffectiveString(
                        "timer.warmup.warming", "%warp%", warp.name,
                        "%seconds%", Integer.toString(warmup.getInt())));
            }

        } else {
            plugin.getWarpList().warpTo(warp, player);
        }
    }
}

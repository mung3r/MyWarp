package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.timer.PlayerCooldown;
import me.taylorkelly.mywarp.timer.PlayerWarmup;
import me.taylorkelly.mywarp.timer.Time;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.NestedCommand;

import org.bukkit.entity.Player;

/**
 * This class contains all root-level commands annotated by the
 * {@link NestedCommand} annotation
 * 
 */
public class RootCommands {

    private MyWarp plugin;

    public RootCommands(MyWarp plugin) {
        this.plugin = plugin;
    }

    @NestedCommand({ AdminCommands.class, BasicCommands.class,
            SocialCommands.class })
    @Command(aliases = { "warp", "mv", "mywarp" }, usage = "<name>", desc = "Warps you to <name>", fee = Fee.WARP_TO, min = 1, permissions = { "mywarp.warp.basic.warp" })
    public void warpTo(CommandContext args, Player sender)
            throws CommandException {

        Warp warp = CommandUtils.getWarpForUsage(sender,
                args.getJoinedStrings(0));
        if (WarpSettings.useTimers) {
            Time cooldown = MyWarp.warpPermissions.getCooldown(sender);
            Time warmup = MyWarp.warpPermissions.getWarmup(sender);

            if (PlayerCooldown.isActive(sender.getName())) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "timer.cooldown.cooling", "%seconds%",
                        Integer.toString(PlayerCooldown
                                .getRemainingCooldown(sender.getName()))));
            }

            if (PlayerWarmup.isActive(sender.getName())) {
                throw new CommandException(LanguageManager.getEffectiveString(
                        "timer.warmup.warming", "%seconds%", Integer
                                .toString(PlayerWarmup
                                        .getRemainingWarmup(sender.getName()))));
            }

            if (MyWarp.warpPermissions.disobeyWarmup(sender)) {
                plugin.getWarpList().warpTo(warp, sender);

                if (!MyWarp.warpPermissions.disobeyCooldown(sender)) {
                    new PlayerCooldown(plugin, sender, cooldown);
                }
                return;
            }

            new PlayerWarmup(plugin, sender, warmup, warp, cooldown);

            if (WarpSettings.warmUpNotify) {
                sender.sendMessage(LanguageManager.getEffectiveString(
                        "timer.warmup.warming", "%warp%", warp.name,
                        "%seconds%", Integer.toString(warmup.getInt())));
            }

        } else {
            plugin.getWarpList().warpTo(warp, sender);
        }
    }

}

package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
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

    @NestedCommand({ AdminCommands.class, BasicCommands.class, SocialCommands.class })
    @Command(aliases = { "warp", "mv", "mywarp" }, usage = "<name>", desc = "cmd.description.warpTo", min = 1, permissions = { "mywarp.warp.basic.warp" })
    public void warpTo(CommandContext args, Player sender) throws CommandException {
        // first check the economy
        if (MyWarp.inst().getWarpSettings().economyEnabled) {
            double fee = MyWarp.inst().getPermissionsManager().getEconomyPrices(sender).getFee(Fee.WARP_TO);

            if (!MyWarp.inst().getEconomyLink().canAfford(sender, fee)) {
                throw new CommandException(MyWarp.inst().getLanguageManager()
                        .getEffectiveString("error.economy.cannotAfford", "%amount%", Double.toString(fee)));
            }
        }

        Warp warp = CommandUtils.getWarpForUsage(sender, args.getJoinedStrings(0));
        if (MyWarp.inst().getWarpSettings().timersEnabled) {
            Time cooldown = MyWarp.inst().getPermissionsManager().getCooldown(sender);
            Time warmup = MyWarp.inst().getPermissionsManager().getWarmup(sender);

            if (PlayerCooldown.isActive(sender.getName())) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("timer.cooldown.cooling", "%seconds%",
                                Integer.toString(PlayerCooldown.getRemainingCooldown(sender.getName()))));
            }

            if (PlayerWarmup.isActive(sender.getName())) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("timer.warmup.warming", "%seconds%",
                                Integer.toString(PlayerWarmup.getRemainingWarmup(sender.getName()))));
            }

            if (MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.warmup.disobey")) {
                warp.warp(sender, true);

                if (!MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.cooldown.disobey")) {
                    new PlayerCooldown(sender, cooldown);
                }
                return;
            }

            new PlayerWarmup(sender, warmup, warp, cooldown);

            if (MyWarp.inst().getWarpSettings().timersWarmupNotify) {
                sender.sendMessage(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString("timer.warmup.warming", "%warp%", warp.getName(), "%seconds%",
                                Integer.toString(warmup.getInt())));
            }

        } else {
            warp.warp(sender, true);
        }
    }

}

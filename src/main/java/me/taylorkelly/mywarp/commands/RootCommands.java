package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.timer.WarpCooldown;
import me.taylorkelly.mywarp.timer.WarpWarmup;
import me.taylorkelly.mywarp.utils.CommandUtils;
import me.taylorkelly.mywarp.utils.commands.Command;
import me.taylorkelly.mywarp.utils.commands.CommandContext;
import me.taylorkelly.mywarp.utils.commands.CommandException;
import me.taylorkelly.mywarp.utils.commands.NestedCommand;

import org.bukkit.entity.Player;

/**
 * This class contains all root-level commands annotated by the
 * {@link NestedCommand} annotation
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
                        .getEffectiveString("error.economy.cannotAfford", sender, "%amount%", Double.toString(fee)));
            }
        }

        Warp warp = CommandUtils.getWarpForUsage(sender, args.getJoinedStrings(0));
        if (MyWarp.inst().getWarpSettings().timersEnabled) {
            if (MyWarp.inst().getTimerFactory().hasRunningTimer(sender.getName(), WarpCooldown.class)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString(
                                "timer.cooldown.cooling", sender,
                                "%seconds%",
                                Integer.toString(MyWarp.inst().getTimerFactory()
                                        .getRemainingSeconds(sender.getName(), WarpCooldown.class))));
            }

            if (MyWarp.inst().getTimerFactory().hasRunningTimer(sender.getName(), WarpWarmup.class)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLanguageManager()
                        .getEffectiveString(
                                "timer.warmup.warming", sender,
                                "%seconds%",
                                Integer.toString(MyWarp.inst().getTimerFactory()
                                        .getRemainingSeconds(sender.getName(), WarpWarmup.class))));
            }

            if (MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.warmup.disobey")) {
                warp.warp(sender, true);

                if (!MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.cooldown.disobey")) {
                    MyWarp.inst()
                            .getTimerFactory()
                            .registerNewTimer(
                                    new WarpCooldown(MyWarp.inst().getTimerFactory(), sender, MyWarp.inst()
                                            .getPermissionsManager().getCooldown(sender)));
                }
                return;
            }

            MyWarp.inst()
                    .getTimerFactory()
                    .registerNewTimer(
                            new WarpWarmup(MyWarp.inst().getTimerFactory(), sender, warp, MyWarp.inst()
                                    .getPermissionsManager().getWarmup(sender)));

        } else {
            warp.warp(sender, true);
        }
    }
}

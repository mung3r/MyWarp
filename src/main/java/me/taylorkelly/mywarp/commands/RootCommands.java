package me.taylorkelly.mywarp.commands;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.data.Warp;
import me.taylorkelly.mywarp.economy.FeeBundle;
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
    @Command(aliases = { "warp", "mv", "mywarp" }, usage = "<name>", desc = "commands.warp-to.description", min = 1, permissions = { "mywarp.warp.basic.warp" })
    public void warpTo(CommandContext args, Player sender) throws CommandException {
        // first check the economy
        if (MyWarp.inst().isEconomySetup()) {
            FeeBundle fees = MyWarp.inst().getPermissionsManager().getFeeBundleManager().getBundle(sender);

            if (!fees.hasAtLeast(sender, FeeBundle.Fee.WARP_TO)) {
                return;
            }
        }

        Warp warp = CommandUtils.getUsableWarp(sender, args.getJoinedStrings(0));
        if (MyWarp.inst().getSettings().isTimersEnabled()
                && !MyWarp.inst().getTimerManager().canDisobey(sender)) {
            if (MyWarp.inst().getTimerManager().hasRunningTimer(sender.getUniqueId(), WarpCooldown.class)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString(
                                "commands.warp-to.cooldown.active",
                                sender,
                                MyWarp.inst().getTimerManager()
                                        .getRemainingSeconds(sender.getUniqueId(), WarpCooldown.class)));
            }

            if (MyWarp.inst().getTimerManager().hasRunningTimer(sender.getUniqueId(), WarpWarmup.class)) {
                throw new CommandException(MyWarp
                        .inst()
                        .getLocalizationManager()
                        .getString(
                                "commands.warp-to.warmup.active",
                                sender,
                                MyWarp.inst().getTimerManager()
                                        .getRemainingSeconds(sender.getUniqueId(), WarpWarmup.class)));
            }

            MyWarp.inst().getTimerManager().registerNewTimer(new WarpWarmup(sender, warp));

        } else {
            warp.teleport(sender, FeeBundle.Fee.WARP_TO);
        }
    }
}

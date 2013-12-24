package me.taylorkelly.mywarp.economy;

import me.taylorkelly.mywarp.MyWarp;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * This link represents a connection with Vault to handle economy tasks.
 */
public class VaultLink implements EconomyLink {

    /**
     * Represents Vault's economy API
     */
    private final Economy economy;

    /**
     * Initializes the economy-link
     * 
     * @param economyProvider
     *            the economy provider - represents Vault
     */
    public VaultLink(RegisteredServiceProvider<Economy> economyProvider) {
        economy = economyProvider.getProvider();
    }

    /**
     * Returns whether the sender can disobey fees or not. This method should be
     * checked before calling the economy implementation.
     * 
     * @param sender
     *            the sender
     * @param amount
     *            the amount that should be charged
     * @return true if the player can dispbey fees, false if not
     */
    private boolean canDisobeyFees(CommandSender sender, double amount) {
        return !(sender instanceof Player)
                || MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.economy.free")
                || amount <= 0;
    }

    @Override
    public boolean canAfford(CommandSender sender, double amount) {
        if (canDisobeyFees(sender, amount)) {
            return true;
        }
        return economy.has(sender.getName(), ((Player) sender).getWorld().getName(), amount);
    }

    @Override
    public void withdrawSender(CommandSender sender, double amount) {
        // non players cannot be withdrawn
        if (canDisobeyFees(sender, amount)) {
            return;
        }
        EconomyResponse response = economy.withdrawPlayer(sender.getName(), ((Player) sender).getWorld()
                .getName(), amount);

        if (!response.transactionSuccess()) {
            MyWarp.logger().severe("Could not withdraw " + sender.getName() + ", " + response.errorMessage);
            sender.sendMessage(ChatColor.RED
                    + MyWarp.inst().getLocalizationManager().getString("economy.unknown-exception", sender));
        } else if (MyWarp.inst().getWarpSettings().economyInformAfterTransaction) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("economy.transaction-complete", sender, amount));
        }
    }
}

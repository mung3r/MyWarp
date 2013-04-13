package me.taylorkelly.mywarp.economy;

import me.taylorkelly.mywarp.LanguageManager;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.WarpSettings;
import me.taylorkelly.mywarp.utils.WarpLogger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * This link represents a connection with Vault to handle economy tasks.
 * 
 */
public class VaultLink implements EconomyLink {

    private final Economy economy;

    public VaultLink(MyWarp plugin) throws NoClassDefFoundError {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer()
                .getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        economy = economyProvider.getProvider();
    }

    @Override
    public boolean canAfford(CommandSender sender, double amount) {
        // non players can always afford a transaction as we escape them when
        // doing the transaction
        if (!(sender instanceof Player)) {
            return true;
        }
        return economy.has(sender.getName(), ((Player) sender).getWorld()
                .getName(), amount);
    }

    @Override
    public void withdrawSender(CommandSender sender, double amount) {
        // non players cannot be withdrawn so we return here
        if (!(sender instanceof Player)) {
            return;
        }
        EconomyResponse response = economy.withdrawPlayer(sender.getName(),
                ((Player) sender).getWorld().getName(), amount);

        if (!response.transactionSuccess()) {
            WarpLogger.severe("Could not withdraw " + sender.getName() + ", "
                    + response.errorMessage);
            sender.sendMessage(ChatColor.RED
                    + LanguageManager.getString("error.economy.unknown"));
        } else if (WarpSettings.informAfterTransaction) {
            sender.sendMessage(LanguageManager.getEffectiveString(
                    "economy.transaction.complete", "%amount%",
                    Double.toString(amount)));
        }
    }
}

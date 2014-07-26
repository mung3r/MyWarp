package me.taylorkelly.mywarp.economy;

import me.taylorkelly.mywarp.MyWarp;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * A link to Vault's economy API.
 */
public class VaultLink extends ManagedLink {

    /**
     * Vault's economy API
     */
    private final Economy economy;

    /**
     * Initializes this economy-link
     * 
     * @param economyProvider
     *            the economy service provider from Vault
     */
    public VaultLink(RegisteredServiceProvider<Economy> economyProvider) {
        economy = economyProvider.getProvider();
    }

    @Override
    protected boolean hasAtLeast(Player player, double amount) {
        return economy.has(player, player.getWorld().getName(), amount);
    }

    @Override
    protected boolean withdraw(Player player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, player.getWorld().getName(), amount);

        if (!response.transactionSuccess()) {
            MyWarp.logger().severe("Could not withdraw " + player.getName() + ", " + response.errorMessage);
            return false;
        }
        return true;
    }
}

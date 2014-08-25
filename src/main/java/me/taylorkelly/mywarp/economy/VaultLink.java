/**
 * Copyright (C) 2011 - 2014, MyWarp team and contributors
 *
 * This file is part of MyWarp.
 *
 * MyWarp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWarp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWarp. If not, see <http://www.gnu.org/licenses/>.
 */
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
     * Vault's economy API.
     */
    private final Economy economy;

    /**
     * Initializes this economy-link.
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

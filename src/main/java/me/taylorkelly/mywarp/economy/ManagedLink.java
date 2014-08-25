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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * An economy link that informs command-senders on transactions and provides an
 * abstraction layer to check for Players only.
 */
public abstract class ManagedLink implements EconomyLink {

    /**
     * Returns whether the given command-sender can disobey fees.
     * 
     * @param sender
     *            the command-sender
     * @param amount
     *            the amount
     * @return true if the player can disobey fees
     */
    private boolean canDisobeyFees(CommandSender sender, double amount) {
        return !(sender instanceof Player)
                || MyWarp.inst().getPermissionsManager().hasPermission(sender, "mywarp.economy.disobey")
                || amount <= 0;
    }

    @Override
    public boolean hasAtLeast(CommandSender sender, double amount) {
        if (canDisobeyFees(sender, amount)) {
            return true;
        }
        if (hasAtLeast((Player) sender, amount)) {
            return true;
        }
        sender.sendMessage(ChatColor.RED
                + MyWarp.inst().getLocalizationManager().getString("economy.cannot-afford", sender, amount));
        return false;
    }

    /**
     * Checks if the given player has at least the given amount.
     * 
     * @param player
     *            the player to check
     * @param amount
     *            the amount to check
     * @return true if the player has at least the given amount
     */
    protected abstract boolean hasAtLeast(Player player, double amount);

    @Override
    public boolean withdraw(CommandSender sender, double amount) {
        if (canDisobeyFees(sender, amount)) {
            return true;
        }
        if (!withdraw((Player) sender, amount)) {
            sender.sendMessage(ChatColor.RED
                    + MyWarp.inst().getLocalizationManager().getString("economy.unknown-exception", sender));
            return false;
        }
        if (MyWarp.inst().getSettings().isEconomyInformAfterTransaction()) {
            sender.sendMessage(MyWarp.inst().getLocalizationManager()
                    .getString("economy.transaction-complete", sender, amount));
        }
        return true;
    }

    /**
     * Withdraw the given player with the given amount.
     * 
     * @param player
     *            the player to check
     * @param amount
     *            the amount to withdraw
     * @return true if transaction completed as expected
     */
    protected abstract boolean withdraw(Player player, double amount);

}

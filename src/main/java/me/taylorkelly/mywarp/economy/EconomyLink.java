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

import org.bukkit.command.CommandSender;

/**
 * A link to an economy provider.
 */
public interface EconomyLink {

    /**
     * Checks if the given command-sender has at least the given amount.
     * 
     * @param sender
     *            the sender to check
     * @param amount
     *            the amount to check
     * @return true if the sender has at least the given amount
     */
    boolean hasAtLeast(CommandSender sender, double amount);

    /**
     * Withdraw the given command-sender with the given amount.
     * 
     * @param sender
     *            the sender
     * @param amount
     *            the amount to withdraw
     * @return true if transaction completed as expected
     */
    boolean withdraw(CommandSender sender, double amount);
}

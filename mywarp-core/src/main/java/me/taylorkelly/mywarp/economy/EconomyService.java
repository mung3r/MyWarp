/*
 * Copyright (C) 2011 - 2015, MyWarp team and contributors
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

import me.taylorkelly.mywarp.LocalPlayer;

/**
 * Represents a service that provides basic economy logic.
 */
public interface EconomyService {

    /**
     * Checks if the given LocalPlayer has at least the given amount.
     * 
     * @param player
     *            the LocalPlayer to check
     * @param amount
     *            the amount to check
     * @return true if the LocalPlayer has at least the given amount
     */
    boolean hasAtLeast(LocalPlayer player, double amount);

    /**
     * Withdraws the given LocalPlayer with the given amount.
     * 
     * @param player
     *            the LocalPlayer to check
     * @param amount
     *            the amount to withdraw
     */
    void withdraw(LocalPlayer player, double amount);
}

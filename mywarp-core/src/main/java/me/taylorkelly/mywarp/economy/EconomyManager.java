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
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;

/**
 * 
 */
public interface EconomyManager {

    /**
     * Returns whether the given LocalPlayer has at least the amount identified
     * by the given Fee and informs the player if he has not.
     * 
     * @param player
     *            the player
     * @param identifier
     *            the identifier
     * @return true if the player has at least the given amount
     */
    boolean informativeHasAtLeast(LocalPlayer player, FeeType identifier);

    /**
     * Returns whether the given LocalPlayer has at least the given amount and
     * informs the player if he has not.
     * 
     * @param player
     *            the player
     * @param amount
     *            the amount
     * @return true if the player has at least the given amount
     * @throws IllegalArgumentException
     *             if the given {@code amount} is not greater than 0
     */
    boolean informativeHasAtLeast(LocalPlayer player, double amount) throws IllegalArgumentException;

    /**
     * Withdraws the given LocalPlayer with the amount identified by the given
     * Fee and informs the player accordingly.
     * 
     * @param player
     *            the player
     * @param identifier
     *            the identifier
     */
    void informativeWithdraw(LocalPlayer player, FeeType identifier);

    /**
     * Withdraws the given LocalPlayer with the given amount and informs the
     * player accordingly.
     * 
     * @param player
     *            the player
     * @param amount
     *            the amount
     * @throws IllegalArgumentException
     *             if the given {@code amount} is not greater than 0
     */
    void informativeWithdraw(LocalPlayer player, double amount) throws IllegalArgumentException;

}

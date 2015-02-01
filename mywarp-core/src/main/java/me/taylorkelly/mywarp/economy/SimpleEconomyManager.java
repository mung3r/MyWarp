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
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.FeeProvider.FeeType;
import me.taylorkelly.mywarp.util.i18n.DynamicMessages;

import com.google.common.base.Preconditions;

/**
 * Manages the economy tasks.
 * <p>
 * The SimpleEconomyManager operates on a {@link FeeProvider} that provides the
 * actual fees and a {@link EconomyService} that provides a connection with the
 * actual economy of the plattform.
 * </p>
 */
public class SimpleEconomyManager implements EconomyManager {

    private static final DynamicMessages MESSAGES = new DynamicMessages("me.taylorkelly.mywarp.lang.Economy"); // NON-NLS

    private final FeeProvider provider;
    private final EconomyService service;

    /**
     * Initializes this SimpleEconomyManager, acting on the given EconomyService
     * and FeeProvider.
     * 
     * @param provider
     *            the FeeProvider
     * @param service
     *            the EconomyService
     */
    public SimpleEconomyManager(FeeProvider provider, EconomyService service) {
        this.provider = provider;
        this.service = service;
    }

    /**
     * Returns whether the given LocalPlayer can disobey fees.
     * 
     * @param player
     *            the LocalPlayer
     * @return true if the Player can disobey fees
     */
    private boolean canDisobeyFees(LocalPlayer player) {
        return player.hasPermission("mywarp.economy.disobey"); // NON-NLS
    }

    @Override
    public boolean informativeHasAtLeast(LocalPlayer player, FeeType identifier) {
        return informativeHasAtLeast(player, provider.getFee(player, identifier));
    }

    @Override
    public boolean informativeHasAtLeast(LocalPlayer player, double amount) throws IllegalArgumentException {
        Preconditions.checkArgument(amount > 0, "The amount must be greater than 0."); // NON-NLS
        if (canDisobeyFees(player)) {
            return true;
        }
        if (service.hasAtLeast(player, amount)) {
            return true;
        }
        player.sendError(MESSAGES.getString("cannot-afford", amount));
        return false;
    }

    @Override
    public void informativeWithdraw(LocalPlayer player, FeeType identifier) {
        informativeWithdraw(player, provider.getFee(player, identifier));
    }

    @Override
    public void informativeWithdraw(LocalPlayer player, double amount) throws IllegalArgumentException {
        Preconditions.checkArgument(amount > 0, "The amount must be greater than 0."); // NON-NLS
        if (canDisobeyFees(player)) {
            return;
        }
        service.withdraw(player, amount);
        if (MyWarp.getInstance().getSettings().isEconomyInformAfterTransaction()) {
            // TODO color in aqua
            player.sendMessage(MESSAGES.getString("transaction-complete", amount));
        }
    }

}

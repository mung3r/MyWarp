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
    public boolean hasAtLeast(CommandSender sender, double amount);

    /**
     * Withdraw the given command-sender with the given amount.
     * 
     * @param sender
     *            the sender
     * @param amount
     *            the amount to withdraw
     * @return true if transaction completed as expected
     */
    public boolean withdraw(CommandSender sender, double amount);
}

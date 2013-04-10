package me.taylorkelly.mywarp.economy;

import org.bukkit.command.CommandSender;

/**
 * This interface needs to be implemented by all economy links as it contains
 * all methods that are used elsewhere in the plugin to handle economy-related
 * tasks.
 * 
 */
public interface EconomyLink {

    /**
     * Checks if the given sender can afford a transaction of the given amount
     * 
     * @param sender the sender to check
     * @param the amount that the sender needs to hold
     * @return true if the sender has the given amount
     */
    public boolean canAfford(CommandSender sender, double amount);

    /**
     * Withdraw the given sender with the given amount
     * 
     * @param sender the sender 
     * @param amount the amount
     */
    public void withdrawSender(CommandSender sender, double amount);
}

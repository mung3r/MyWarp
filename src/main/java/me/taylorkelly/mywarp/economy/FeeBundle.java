package me.taylorkelly.mywarp.economy;

import java.util.EnumMap;
import org.bukkit.command.CommandSender;
import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.permissions.valuebundles.AbstractValueBundle;

/**
 * A bundle that stores economy-fees.
 */
public class FeeBundle extends AbstractValueBundle {

    /**
     * The different types of fees.
     */
    public enum Fee {
        ACCEPT, ASSETS, CREATE, CREATE_PRIVATE, DELETE, GIVE, HELP, INFO, INVITE, LIST, NONE, POINT, PRIVATE, PUBLIC, SEARCH, UNINVITE, UPDATE, WARP_PLAYER, WARP_SIGN_CREATE, WARP_SIGN_USE, WARP_TO, WELCOME
    }

    /**
     * Stores the fees under their identifier
     */
    private EnumMap<Fee, Double> fees = new EnumMap<Fee, Double>(Fee.class);

    /**
     * Initializes this fee-bundle.
     * 
     * @param identifier
     *            the unique identifier
     * @param acceptFee
     *            used when accepting a given warp
     * @param assetsFee
     *            used when listing a player's warps with limits
     * @param createFee
     *            used when creating a public warp
     * @param createPrivateFee
     *            used when creating a private warp
     * @param deleteFee
     *            used when a warp is deleted
     * @param giveFee
     *            used when a warp is given to other users
     * @param helpFee
     *            used when accessing the help-command
     * @param infoFee
     *            used when using the info-command
     * @param inviteFee
     *            used when inviting a user or a group
     * @param listFee
     *            used when warps are listed via /warp list
     * @param pointFee
     *            used when the compass is pointed to a warp
     * @param privatizeFee
     *            used when a warp is publicized
     * @param publicizeFee
     *            used when a warp is privatized
     * @param searchFee
     *            used when the search-command is accessed
     * @param uninviteFee
     *            used when uninviting users or groups
     * @param updateFee
     *            used when a warp's location is updated
     * @param warpPlayerFee
     *            used when a player is warped (/warp player)
     * @param warpSignCreateFee
     *            used upon warp sign creation
     * @param warpSignUseFee
     *            used upon warp sign usage
     * @param warpFee
     *            used when a users warps to a warp
     * @param welcomeFee
     *            used when the welcome message is changed
     */
    public FeeBundle(String identifier, double acceptFee, double assetsFee, double createFee,
            double createPrivateFee, double deleteFee, double giveFee, double helpFee, double infoFee,
            double inviteFee, double listFee, double pointFee, double privatizeFee, double publicizeFee,
            double searchFee, double uninviteFee, double updateFee, double warpPlayerFee,
            double warpSignCreateFee, double warpSignUseFee, double warpFee, double welcomeFee) {
        super(identifier);

        fees.put(Fee.ACCEPT, acceptFee);
        fees.put(Fee.ASSETS, assetsFee);
        fees.put(Fee.CREATE, createFee);
        fees.put(Fee.CREATE_PRIVATE, createPrivateFee);
        fees.put(Fee.DELETE, deleteFee);
        fees.put(Fee.GIVE, giveFee);
        fees.put(Fee.HELP, helpFee);
        fees.put(Fee.INFO, infoFee);
        fees.put(Fee.INVITE, inviteFee);
        fees.put(Fee.LIST, listFee);
        fees.put(Fee.POINT, pointFee);
        fees.put(Fee.PRIVATE, privatizeFee);
        fees.put(Fee.PUBLIC, publicizeFee);
        fees.put(Fee.SEARCH, searchFee);
        fees.put(Fee.UNINVITE, uninviteFee);
        fees.put(Fee.UPDATE, updateFee);
        fees.put(Fee.WARP_PLAYER, warpPlayerFee);
        fees.put(Fee.WARP_SIGN_CREATE, warpSignCreateFee);
        fees.put(Fee.WARP_SIGN_USE, warpSignUseFee);
        fees.put(Fee.WARP_TO, warpFee);
        fees.put(Fee.WELCOME, welcomeFee);
    }

    /**
     * Returns the fee that referenced by the given identifier.
     * 
     * @param identifier
     *            the identifier
     * @return the actual fee referenced by the identifier
     */
    public double getFee(Fee identifier) {
        return fees.get(identifier);
    }

    /**
     * Checks if the given command-sender has at least the amount referenced by
     * the given fee.
     * 
     * @param sender
     *            the sender to check
     * @param identifier
     *            the identifier
     * @return true if the sender has at least the amount
     */
    public boolean hasAtLeast(CommandSender sender, Fee identifier) {
        return MyWarp.inst().getEconomyLink().hasAtLeast(sender, fees.get(identifier));
    }

    /**
     * Withdraw the given command-sender with the given amount.
     * 
     * @param sender
     *            the sender
     * @param identifier
     *            the identifier
     * @return true if transaction completed as expected
     */
    public boolean withdraw(CommandSender sender, Fee identifier) {
        return MyWarp.inst().getEconomyLink().withdraw(sender, fees.get(identifier));
    }

    @Override
    protected String getBasePermission() {
        return "mywarp.economy";
    }

    @Override
    public String toString() {
        return "FeeBundle [getIdentifier()=" + getIdentifier() + ", fees=" + fees + "]";
    }

}

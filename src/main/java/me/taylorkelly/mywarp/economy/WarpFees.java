package me.taylorkelly.mywarp.economy;

import java.util.EnumMap;

import me.taylorkelly.mywarp.utils.ValuePermissionContainer;

/**
 * A storage object for all possible fees used if economy support is enabled.
 * 
 */
public class WarpFees extends ValuePermissionContainer {

    private EnumMap<Fee, Double> prices = new EnumMap<Fee, Double>(Fee.class);

    // CREATE, CREATE_PRIVATE, DELETE, GIVE, HELP, INVITE, LIST, LISTALL, NONE,
    // POINT, PRIVATE, PUBLIC, SEARCH, UNINVITE, UPDATE, WARP_PLAYER,
    // WARP_SIGN_CREATE, WARP_SIGN_USE, WARP_TO, WELCOME

    /**
     * Initializes the WarpFees. The given fees are stored internally.
     * 
     * @param name
     *            the name used on permission lookup
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
     * @param inviteFee
     *            used when inviting a user or a group
     * @param listFee
     *            used when warps are listed via /warp list
     * @param listAllFee
     *            used when all warps are listed at once (/warp alist)
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
    public WarpFees(String name, double createFee, double createPrivateFee,
            double deleteFee, double giveFee, double helpFee, double inviteFee,
            double listFee, double listAllFee, double pointFee,
            double privatizeFee, double publicizeFee, double searchFee,
            double uninviteFee, double updateFee, double warpPlayerFee,
            double warpSignCreateFee, double warpSignUseFee, double warpFee,
            double welcomeFee) {
        super(name);

        prices.put(Fee.CREATE, createFee);
        prices.put(Fee.CREATE_PRIVATE, createPrivateFee);
        prices.put(Fee.DELETE, deleteFee);
        prices.put(Fee.GIVE, giveFee);
        prices.put(Fee.HELP, helpFee);
        prices.put(Fee.INVITE, inviteFee);
        prices.put(Fee.LIST, listFee);
        prices.put(Fee.LISTALL, listAllFee);
        prices.put(Fee.POINT, pointFee);
        prices.put(Fee.PRIVATE, privatizeFee);
        prices.put(Fee.PUBLIC, publicizeFee);
        prices.put(Fee.SEARCH, searchFee);
        prices.put(Fee.UNINVITE, uninviteFee);
        prices.put(Fee.UPDATE, updateFee);
        prices.put(Fee.WARP_PLAYER, warpPlayerFee);
        prices.put(Fee.WARP_SIGN_CREATE, warpSignCreateFee);
        prices.put(Fee.WARP_SIGN_USE, warpSignUseFee);
        prices.put(Fee.WARP_TO, warpFee);
        prices.put(Fee.WELCOME, welcomeFee);
    }

    /**
     * Returns the fee that referenced by the given identifier. Will return null
     * if there is no reference.
     * 
     * @param identifier
     *            the identifier
     * @return the actual fee referenced by the identifier
     */
    public double getFee(Fee identifier) {
        return prices.get(identifier);
    }

}

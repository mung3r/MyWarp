package me.taylorkelly.mywarp.data;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import me.taylorkelly.mywarp.MyWarp;
import me.taylorkelly.mywarp.economy.Fee;
import me.taylorkelly.mywarp.utils.MatchList;
import me.taylorkelly.mywarp.utils.TempConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * This class manages all warps in the network. It provides all methods that are
 * needed when making changes on the warp-network.
 * 
 */
public class WarpManager {

    /**
     * This map represents the warp network. It stores all warps using their
     * name as key.
     */
    private Map<String, Warp> warpMap;

    /**
     * Self-cleaning storage for welcome messages
     */
    private ConcurrentHashMap<String, Warp> welcomeMessage;

    public WarpManager() {
        welcomeMessage = new TempConcurrentHashMap<String, Warp>();
        warpMap = MyWarp.inst().getConnectionManager().getMap();
        MyWarp.logger().info(getSize() + " warps loaded");
    }

    /**
     * Adds the given warp to the network and adds markers, if needed.
     * 
     * @param name
     *            the name of the warp
     * @param warp
     *            the warp
     */
    public void addWarp(String name, Warp warp) {
        warpMap.put(name, warp);
        MyWarp.inst().getConnectionManager().addWarp(warp);

        if (MyWarp.inst().getWarpSettings().useDynmap) {
            MyWarp.inst().getMarkers().addWarp(warp);
        }
    }

    /**
     * Creates a private warp owned by the given player at the his position and
     * calls {@link #addWarp(String, Warp)} to add it to the network
     * 
     * @param name
     *            the name of the warp
     * @param player
     *            the player
     */
    public void addWarpPrivate(String name, Player player) {
        Warp warp = new Warp(name, player, false);
        addWarp(name, warp);
    }

    /**
     * Creates a public warp owned by the given player at the his position and
     * calls {@link #addWarp(String, Warp)} to add it to the network
     * 
     * @param name
     *            the name of the warp
     * @param player
     *            the player
     */
    public void addWarpPublic(String name, Player player) {
        Warp warp = new Warp(name, player);
        addWarp(name, warp);
    }

    /**
     * Removes the given warp from the network. Will also remove the marker if
     * needed.
     * 
     * @param warp
     *            the warp
     */
    public void deleteWarp(Warp warp) {
        warpMap.remove(warp.getName());
        MyWarp.inst().getConnectionManager().deleteWarp(warp);

        if (MyWarp.inst().getWarpSettings().useDynmap) {
            MyWarp.inst().getMarkers().deleteWarp(warp);
        }
    }

    /**
     * Gets a {@link MatchList} with all warps accessible by the given player
     * and matching the given name (exactly or partly). Optionally a comparator
     * can be specified to control the internal sorting of the match-list
     * elements.
     * 
     * @param name
     *            the name of the warp
     * @param player
     *            the player
     * @param comperator
     *            the comparator or null for default sorting
     * @return a match list with warps matching the given criteria
     */
    public MatchList getMatches(String name, Player player,
            Comparator<Warp> comperator) {
        TreeSet<Warp> exactMatches = new TreeSet<Warp>(comperator);
        TreeSet<Warp> matches = new TreeSet<Warp>(comperator);

        for (Warp warp : warpMap.values()) {
            if (!isWarpAccessible(warp, player)) {
                continue;
            }
            if (warp.getName().equalsIgnoreCase(name)) {
                exactMatches.add(warp);
            } else if (warp.getName().toLowerCase()
                    .contains(name.toLowerCase())) {
                matches.add(warp);
            }
        }
        if (exactMatches.size() > 1) {
            Iterator<Warp> iterator = exactMatches.iterator();
            while (iterator.hasNext()) {
                Warp warp = iterator.next();
                if (!warp.getName().equals(name)) {
                    matches.add(warp);
                    iterator.remove();
                }
            }
        }
        return new MatchList(exactMatches, matches);
    }

    /**
     * Attempts to match the given creator string to a creator who owns warps in
     * the network. This method will always return the full name of the searched
     * creator or null if there either no or more than on match(es)
     * 
     * @param player
     *            the player
     * @param creator
     *            the (part of) the searched creator
     * @return the an exactly matching creator existing in the network or an
     *         null if not exact match exist
     */
    public String getMatchingCreator(Player player, String creator) {
        String match = null;

        for (Warp warp : warpMap.values()) {
            if (!isWarpAccessible(warp, player)) {
                continue;
            }
            String warpCreator = warp.getCreator();

            // minecraft usernames are case insensitive
            if (warpCreator.equalsIgnoreCase(creator)) {
                return warpCreator;
            }
            if (!StringUtils.containsIgnoreCase(warpCreator, creator)) {
                continue;
            }
            if (match != null && !match.equals(warpCreator)) {
                return null;
            }
            match = warpCreator;
        }
        return match;
    }

    /**
     * Gets a sorted set with all public warps in the network.
     * 
     * @return a set with all existing public warps
     */
    public TreeSet<Warp> getPublicWarps() {
        TreeSet<Warp> ret = new TreeSet<Warp>();

        for (Warp warp : warpMap.values()) {
            if (warp.isPublicAll()) {
                ret.add(warp);
            }
        }
        return ret;
    }

    /**
     * gets the number of all warps in the network
     * 
     * @return the total number of warps
     */
    public int getSize() {
        return warpMap.size();
    }

    /**
     * gets the warp of the given name from the warp network. Will return null
     * if the warp does not exist.
     * 
     * @param name
     *            the warp's name
     * @return the warp with the given name
     */
    public Warp getWarp(String name) {
        return warpMap.get(name);
    }

    /**
     * Changes the creator of the given warp to the given new player
     * 
     * @param warp
     *            the warp
     * @param giveeName
     *            the name of the new creator
     */
    public void give(Warp warp, String giveeName) {
        warp.setCreator(giveeName);
        MyWarp.inst().getConnectionManager().updateCreator(warp);

        if (MyWarp.inst().getWarpSettings().useDynmap) {
            MyWarp.inst().getMarkers().updateWarp(warp);
        }
    }

    /**
     * Invites the group of the given name to the given name, making it usable
     * for all players in this group
     * 
     * @param warp
     *            the warp
     * @param inviteeName
     *            the group's name
     */
    public void inviteGroup(Warp warp, String inviteeName) {
        warp.inviteGroup(inviteeName);
        MyWarp.inst().getConnectionManager().updateGroupPermissions(warp);
    }

    /**
     * Invites the player of the given name to the given name, making it usable
     * for him
     * 
     * @param warp
     *            the warp
     * @param inviteeName
     *            the player's name
     */
    public void invitePlayer(Warp warp, String inviteeName) {
        warp.invite(inviteeName);
        MyWarp.inst().getConnectionManager().updatePermissions(warp);
    }

    /**
     * Indicates that the given player is not longer expected to send a welcome
     * message. Threadsafe.
     * 
     * @param player
     *            the player
     */
    public void notWaiting(Player player) {
        welcomeMessage.remove(player.getName());
    }

    /**
     * Gets the total number of all existing private warps created and owned by
     * this player
     * 
     * @param player
     *            the player
     * @return the number of all private warps owned by this player
     */
    private int numPrivateWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpMap.values()) {
            boolean privateAll = !warp.isPublicAll();
            String creator = warp.getCreator();
            if (creator.equals(player.getName()) && privateAll) {
                size++;
            }
        }
        return size;
    }

    /**
     * Gets the total number of all existing public warps created and owned by
     * this player
     * 
     * @param player
     *            the player
     * @return the number of all public warps owned by this player
     */
    private int numPublicWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpMap.values()) {
            boolean publicAll = warp.isPublicAll();
            String creator = warp.getCreator();
            if (creator.equals(player.getName()) && publicAll) {
                size++;
            }
        }
        return size;
    }

    /**
     * Gets the total number of all existing warps created and owned by this
     * player
     * 
     * @param player
     *            the player
     * @return the number of all warps owned by this player
     */
    private int numWarpsPlayer(Player player) {
        int size = 0;
        for (Warp warp : warpMap.values()) {
            String creator = warp.getCreator();
            if (creator.equals(player.getName())) {
                size++;
            }
        }
        return size;
    }

    /**
     * Checks if the given player may build additional private warps using his
     * private-limit. This method does not take into account if limits are
     * enabled ot not!
     * 
     * @param player
     *            the player
     * @return true if the player can build additional private warps, false if
     *         not
     */
    public boolean playerCanBuildPrivateWarp(Player player) {
        if (MyWarp.inst().getPermissionsManager()
                .hasPermission(player, "mywarp.limit.private.unlimited")) {
            return true;
        }
        return numPrivateWarpsPlayer(player) < MyWarp.inst()
                .getPermissionsManager().maxPrivateWarps(player);
    }

    /**
     * Checks if the given player may build additional warps using his
     * public-limit. This method does not take into account if limits are
     * enabled ot not!
     * 
     * @param player
     *            the player
     * @return true if the player can build additional public warps, false if
     *         not
     */
    public boolean playerCanBuildPublicWarp(Player player) {
        if (MyWarp.inst().getPermissionsManager()
                .hasPermission(player, "mywarp.limit.public.unlimited")) {
            return true;
        }
        return numPublicWarpsPlayer(player) < MyWarp.inst()
                .getPermissionsManager().maxPublicWarps(player);
    }

    /**
     * Checks if the given player may build additional warps using his
     * total-limit. This method does not take into account if limits are enabled
     * ot not!
     * 
     * @param player
     *            the player
     * @return true if the player can build additional warps, false if not
     */
    public boolean playerCanBuildWarp(Player player) {
        if (MyWarp.inst().getPermissionsManager()
                .hasPermission(player, "mywarp.limit.total.unlimited")) {
            return true;
        }
        return numWarpsPlayer(player) < MyWarp.inst().getPermissionsManager()
                .maxTotalWarps(player);
    }

    /**
     * Sets the compass target of the given player to the location of the given
     * warp
     * 
     * @param warp
     *            the warp
     * @param player
     *            the player
     */
    public void point(Warp warp, Player player) {
        player.setCompassTarget(warp.getLocation());
    }

    /**
     * Privatizes the given warp
     * 
     * @param warp
     *            the warp
     */
    public void privatize(Warp warp) {
        warp.setPublicAll(false);
        MyWarp.inst().getConnectionManager().publicizeWarp(warp, false);

        if (MyWarp.inst().getWarpSettings().useDynmap) {
            MyWarp.inst().getMarkers().deleteWarp(warp);
        }
    }

    /**
     * Publicizes the given warp
     * 
     * @param warp
     *            the warp
     */
    public void publicize(Warp warp) {
        warp.setPublicAll(true);
        MyWarp.inst().getConnectionManager().publicizeWarp(warp, true);

        if (MyWarp.inst().getWarpSettings().useDynmap) {
            MyWarp.inst().getMarkers().addWarp(warp);
        }
    }

    /**
     * Sets the welcome message to the given message for the warp that is stored
     * under the given player in the welcomeMessages-Map. Threadsafe.
     * 
     * @param player
     *            the player
     * @param message
     *            the message
     */
    public void setWelcomeMessage(Player player, String message) {
        if (welcomeMessage.containsKey(player.getName())) {
            Warp warp = welcomeMessage.get(player.getName());

            // this method is almost always called asnyc so the warp needs to be
            // locked for changes
            synchronized (warp) {
                warp.setWelcomeMessage(message);
            }
            MyWarp.inst().getConnectionManager().updateWelcomeMessage(warp);

            // sendMessage is threadsafe
            player.sendMessage(MyWarp
                    .inst()
                    .getLanguageManager()
                    .getEffectiveString("warp.welcome.received", "%warp%",
                            warp.getName()));
            player.sendMessage(ChatColor.AQUA + message);
        }
    }

    /**
     * uninvites the group with the given name from the given warp
     * 
     * @param warp
     *            the warp
     * @param inviteeName
     *            the name of the group
     */
    public void uninviteGroup(Warp warp, String inviteeName) {
        warp.uninviteGroup(inviteeName);
        MyWarp.inst().getConnectionManager().updateGroupPermissions(warp);
    }

    /**
     * Uninvites the player with the given name from the given warp
     * 
     * @param warp
     *            the warp
     * @param inviteeName
     *            the name of the player
     */
    public void uninvitePlayer(Warp warp, String inviteeName) {
        warp.uninvite(inviteeName);
        MyWarp.inst().getConnectionManager().updatePermissions(warp);
    }

    /**
     * Updates the location of the warp with those of the given player. Will
     * also update the marker if needed
     * 
     * @param warp
     *            the warp
     * @param player
     *            the player
     */
    public void updateLocation(Warp warp, Player player) {
        warp.setLocation(player.getLocation());
        MyWarp.inst().getConnectionManager().updateLocation(warp);

        if (MyWarp.inst().getWarpSettings().useDynmap) {
            MyWarp.inst().getMarkers().updateWarp(warp);
        }
    }

    /**
     * Checks the given player needs to send a welcome message for a certain
     * warp. Threadsafe.
     * 
     * @param player
     *            the player
     * @return true if this player still needs to send a welcome message, false
     *         if not
     */
    public boolean waitingForWelcome(Player player) {
        return welcomeMessage.containsKey(player.getName());
    }

    /**
     * Checks if a warp with the given name exist
     * 
     * @param name
     *            the name
     * @return true if a warp with this name exists, false if not
     */
    public boolean warpExists(String name) {
        return warpMap.containsKey(name);
    }

    /**
     * Returns a sorted set with all warps, the given player has access to.
     * Optionally, this warps must be all created by the given creator or exist
     * in a world of the given name. The sorting of the warps can be controlled
     * by giving a custom comperator.
     * 
     * @param player
     *            the player
     * @param creator
     *            the creator's name or null for all creators
     * @param world
     *            the world's name or null for all worlds
     * @param comperator
     *            the comperator or null for the default sorting
     * @return a sorted list with all warps matching the given criteria
     */
    public TreeSet<Warp> warpsInvitedTo(Player player, String creator,
            String world, Comparator<Warp> comperator) {
        TreeSet<Warp> results = new TreeSet<Warp>(comperator);

        if (creator != null) {
            creator = getMatchingCreator(player, creator);

            // unable to find a matching creator
            if (creator == null) {
                return results;
            }
        }

        for (Warp warp : warpMap.values()) {
            if (!isWarpAccessible(warp, player)) {
                continue;
            }
            if (creator != null && !warp.getCreator().equals(creator)) {
                continue;
            }
            if (world != null && !warp.getWorld().equals(world)) {
                continue;
            }
            results.add(warp);
        }
        return results;
    }

    /**
     * Warps the given player to the given warp. Visit-counter and welcome-message are handled as needed.
     * 
     * @param warp
     *            the warp
     * @param player
     *            the player
     */
    public void warpTo(Warp warp, Player player) {
        switch (warp.warp(player)) {
        case NONE:
            break;
        case ORIGINAL_LOC:
            player.sendMessage(ChatColor.AQUA
                    + warp.getSpecificWelcomeMessage(player));
        case SAFE_LOC:
            warp.visit();
            MyWarp.inst().getConnectionManager().updateVisits(warp);
            
            if (MyWarp.inst().getWarpSettings().useEconomy) {
                MyWarp.inst()
                        .getEconomyLink()
                        .withdrawSender(
                                player,
                                MyWarp.inst().getPermissionsManager()
                                        .getEconomyPrices(player).getFee(Fee.WARP_TO));
            }
            break;
        }
    }

    /**
     * Indicates that the given player is expected to send a new welcome message
     * for the given warp. Threadsafe.
     * 
     * @param warp
     *            the warp
     * @param player
     *            the player
     */
    public void welcomeMessage(Warp warp, final Player player) {
        welcomeMessage.put(player.getName(), warp);
    }

    /**
     * Checks if the given warp is accessible for the given player. This method
     * bundles all accessible checks (generally usability, per world etc.), that
     * are ALWAYS used. Method specific checks should be handled in the specific
     * methods.
     * 
     * @param warp
     *            the warp to check
     * @param player
     *            the player
     * @return true if the warp is accessible, false if not
     */
    private boolean isWarpAccessible(Warp warp, Player player) {
        if (player != null && !warp.playerCanWarp(player)) {
            return false;
        }
        if (MyWarp.inst().getWarpSettings().worldAccess
                && player != null
                && !MyWarp.inst().getPermissionsManager()
                        .playerCanAccessWorld(player, warp.getWorld())) {
            return false;
        }
        return true;
    }
}
